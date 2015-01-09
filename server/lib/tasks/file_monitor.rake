namespace :act do

  desc "Monitor new synchronized documents and save them in our database"

  task file_monitor: :environment do
    Rails.logger = Logger.new(STDOUT)
    
    @jobs = Queue.new
    @sync_directory = Settings.sync_directory
    @watch_expression = "#{@sync_directory}/*/inbox/*"
    @document_store_directory = "#{@sync_directory}/documents/"
    @errored_store_directory = "#{@sync_directory}/errored/"

    init_sync_directory
    enqueue_preexisting_files
    start_monitoring

    Rails.logger.info "Monitoring files in #{@sync_directory}..."
    while true
      next_file = @jobs.pop
      ActiveRecord::Base.transaction { process_file(next_file) }
    end
  end

  def init_sync_directory
    FileUtils.mkdir_p @sync_directory unless Dir.exists? @sync_directory
    FileUtils.mkdir_p @document_store_directory unless Dir.exists? @document_store_directory
    FileUtils.mkdir_p @errored_store_directory unless Dir.exists? @errored_store_directory
  end

  def enqueue_preexisting_files
    preexisting_files = Dir[@watch_expression]
    if preexisting_files.any?
      Rails.logger.info "Enqueuing #{preexisting_files.size} preexisting #{'file'.pluralize(preexisting_files.size)}."
      preexisting_files.each do |f|
        @jobs << f
      end
    end
  end

  def start_monitoring
    Thread.start do
      FileWatcher.new(@sync_directory).watch() do | path, event |
        if event == :new and File.fnmatch(@watch_expression, path)
          Rails.logger.info "New file detected: #{path}."
          @jobs << path
        end
      end
    end
  end

  def process_file(path)
    parts = path.split(File::SEPARATOR).reverse
    filename = parts[0]
    device_guid = parts[2]

    if filename.match(/case.*/)
      file_content = File.read(path)
      Case.save_from_sync_file(device_guid, file_content)
      File.delete(path)
    else
      file_guid = filename[0..35]
      unless file_guid =~ /[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i
        # ensure there's a file guid if client didn't send it
        notify_errored_file(device_guid, path, reason: "Cases file #{filename} doesn't include GUID", filename: filename)
        return
      end
      begin
        spreadsheet = Roo::Spreadsheet.open(path)
      rescue
        notify_errored_file(device_guid, path, reason: "Unrecognized file", guid: file_guid, filename: filename)
        return
      end
      rows = spreadsheet_rows(spreadsheet)
      storage_path = "#{@document_store_directory}#{device_guid}-#{filename}"
      File.rename(path, storage_path)
      device_id = Device.where(guid: device_guid).pluck(:id)[0]
      cases_file = CasesFile.new device_id: device_id, file: storage_path, guid: file_guid
      cases_file.save!
      process_rows(rows, cases_file)
      notify_parsed_cases(cases_file)
    end
  end

  def spreadsheet_rows(spreadsheet)
    spreadsheet.parse(:header_search => [])
  end

  def process_rows(rows, cases_file)
    header = rows.first
    rows = rows.drop(1) # skip header

    name_key = find_key(header, 'name')
    phone_key = find_key(header, 'phone')
    gender_key = find_key(header, 'gender')
    age_key = find_key(header, 'age')
    notes_key = find_key(header, 'note')
    dialect_key = find_key(header, 'dialect')

    reasons_key = find_key(header, 'reason')

    if reasons_key then
      rows.each do | row |
        phone = row[phone_key]
        phone = phone.to_i if phone.is_a? Numeric

        # TODO: reference the originating file
        export_new_case Case.save_from_sync_json(cases_file.device.guid, {
          "guid" => SecureRandom.uuid,
          "name" => row[name_key],
          "phone_number" => phone,
          "age" => row[age_key],
          "gender" => row[gender_key],
          "dialect_code" => row[dialect_key],
          "symptoms" => row[reasons_key],
          "note" => row[notes_key]
        }, cases_file)
      end
    else
      fever_key = find_key(header, 'fever')

      if fever_key then
        reasons = {
          "fever" => 'Fever',
          "headache" => 'Severe headache',
          "muscle" => 'Muscle pain',
          "weaknes" => 'Weakness',
          "fatigue" => 'Fatigue',
          "diarrhea" => 'Diarrhea',
          "vomit" => 'Vomiting',
          "abdominal" => 'Abdominal (stomach) pain',
          "hemorrhage" => 'Unexplained hemorrhage (bleeding or bruising)'
        }

        reasons_titles = {}
        reasons.each do | code, reason |
          key = find_key(header, code)
          if key then
            reasons_titles[code] = key
          end
        end


        rows.each do | row |
          symptoms = []

          reasons.each do | code, reason |
            reason_key = reasons_titles[code]
            if reason_key then
              symptoms << reason if !row[reason_key].blank?
            end
          end

          phone = row[phone_key]
          phone = phone.to_i if phone.is_a? Numeric

          export_new_case Case.save_from_sync_json(cases_file.device.guid, {
            "guid" => SecureRandom.uuid,
            "name" => row[name_key],
            "phone_number" => phone,
            "age" => row[age_key],
            "gender" => row[gender_key],
            "dialect_code" => row[dialect_key],
            "symptoms" => symptoms.join(","),
            "note" => row[notes_key]
          }, cases_file)
        end
      end
    end
  end

  def find_key(hash, target_key)
    hash.keys.find { |key| key.downcase.include? target_key }
  end

  def notify_errored_file(device_guid, path, opts)
    Rails.logger.warn opts[:reason]
    File.rename path, "#{@errored_store_directory}#{device_guid}-#{opts[:filename]}"
    Device.sync_errored_cases_file(device_guid, opts)
  end

  def notify_parsed_cases(cases_file)
    Device.sync_cases_file(cases_file.device.guid, cases_file.guid, cases_file.cases)
  end

  def export_new_case(_case)
    Device.sync_new_case(_case.device.guid, _case.guid, _case.as_json_for_api())
  end

end
