namespace :file_monitor do

  desc "Monitor new sync'ed documents and save them in our database"

  task start: :environment do
    Rails.logger = Logger.new(STDOUT)
    
    @jobs = Queue.new
    @sync_directory = Settings.sync_directory
    @watch_expression = "#{@sync_directory}/*/inbox/*"

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
    unless Dir.exists? @sync_directory
      FileUtils.mkdir_p @sync_directory
    end
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
    file_content = File.read(path)
    parts = path.split(File::SEPARATOR).reverse
    filename = parts[0]
    device_id = parts[2]

    if filename.match(/case.*/)
      Case.save_from_sync_file(device_id, file_content)
      File.delete(path)
    else
      Rails.logger.warn "Unrecognized file was synchronized by client: #{filename}"      
    end
  end

end
