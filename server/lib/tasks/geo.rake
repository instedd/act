desc "Imports all locations from /etc/shapes and regenerates the index"
task :geo do
  Rake::Task['geo:all'].invoke
end

namespace :geo do

  desc "Downloads all locations file into the shapefiles folder"
  task :download => :logger do
    filenames = Settings.remote_shapefiles
    puts "Downloading #{filenames}..."
    Geo::Downloader.new(filenames).download!
  end

  desc "Imports all locations from the shapefiles folder"
  task :import => :logger do
    filenames = Dir["#{Rails.root}/etc/shapes/**/GINLBRSLE_adm?.json"]
    puts "Importing #{filenames}..."
    Geo::Importer.new(filenames).import!
  end

  desc "Deletes existing locations index and reindexes all locations from DB"
  task :reindex => :logger do
    puts "Indexing..."
    Geo::Indexer.new.reindex_all!
  end

  desc "Validates if the center of all locations is contained within their polygon"
  task :validate => :logger do
    puts "Validating..."
    Geo::Indexer.new.validate_center(Location.includes(:shape).all)
  end

  desc "Fails if the locations' index already exists"
  task :fail_if_index_exist => :logger do
    raise "Index already exists" if Geo::Indexer.new.index_exist?
  end

  desc "Downloads all locations into /etc/shapes and regenerates the index as needed"
  task :all, [:force] do |t, args|
    begin
      Rake::Task['geo:fail_if_index_exist'].invoke unless args[:force]
    rescue => exception
      puts "Skipping geo operations due to: #{exception.to_s}"
      exit
    end
    Rake::Task['geo:download'].invoke
    Rake::Task['geo:import'].invoke
    Rake::Task['geo:reindex'].invoke
    Rake::Task['geo:validate'].invoke
    puts "Geo information correctly imported!"
  end

  desc "Downloads all locations into /etc/shapes and regenerates the index, overwriting everything"
  task :all! do
    Rake::Task['geo:all'].invoke(true)
  end

  task :logger => :environment do
    if Rails.env.development?
      logger       = Logger.new(STDOUT)
      logger.level = Logger::INFO
      Rails.logger = logger
    end
  end

end
