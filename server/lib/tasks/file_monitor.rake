namespace :file_monitor do
  
  desc "Monitor new sync'ed documents and save them in our database"

  task start: :environment do
    puts "Monitoring '#{Settings.sync_directory}' for new documents..."
    FileWatcher.new(Settings.sync_directory).watch() do | path, event |
      if event == :new
        ActiveRecord::Base.transaction { process_new_file(path) }
      end
    end
  end

  def process_new_file(path)
    puts "New file detected: #{path}"
    file_content = File.read(path)
    parts = path.split(File::SEPARATOR).reverse
    filename = parts[0]
    device_id = parts[1]

    case
    when filename.match(/user.*/)
      Device.save_from_sync_file(device_id, file_content)
    when filename.match(/case.*/)
      Case.save_from_sync_file(device_id, file_content)
    end
  end

end
