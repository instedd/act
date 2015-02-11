desc "Imports all locations from /etc/shapes and regenerates the index"
task :geo do
  Rake::Task['geo:all'].invoke
end

namespace :geo do

  desc "Downloads all locations file into the shapefiles folder"
  task :download => :logger do
    filenames = Settings.remote_shapefiles
    Geo::Downloader.new(filenames).download!
  end

  desc "Imports all locations from the shapefiles folder"
  task :import => :logger do
    filenames = Dir["#{Rails.root}/etc/shapes/**/GINLBRSLE_adm?.json"]
    Geo::Importer.new(filenames).import!
  end

  desc "Deletes existing locations index and reindexes all locations from DB"
  task :reindex => :logger do
    Geo::Indexer.new.reindex_all!
  end

  desc "Validates if the center of all locations is contained within their polygon"
  task :validate => :logger do
    Geo::Indexer.new.validate_center(Location.includes(:shape).all)
  end

  desc "Downloads all locations into /etc/shapes and regenerates the index"
  task :all do
    Rake::Task['geo:download'].invoke
    Rake::Task['geo:import'].invoke
    Rake::Task['geo:reindex'].invoke
    Rake::Task['geo:validate'].invoke
  end

  task :logger => :environment do
    if Rails.env.development?
      logger       = Logger.new(STDOUT)
      logger.level = Logger::INFO
      Rails.logger = logger
    end
  end

end
