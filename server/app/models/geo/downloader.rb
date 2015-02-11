require 'open-uri'
require 'fileutils'
require 'zip'

module Geo

  class Downloader

    def initialize(files)
      @files = files
    end

    def download!
      @files.each do |remote|
        download_file(remote)
      end
    end

    def local_dir
      Rails.root.join 'etc', 'shapes'
    end

    def local_file remote
      local_dir.join URI(remote).path.split('/').last
    end

    def unzip_file zipfile
      Zip::File.open(zipfile) do |zip_file|
         zip_file.each do |f|
           f_path = local_dir.join f.name
           FileUtils.mkdir_p(File.dirname(f_path))
           zip_file.extract(f, f_path) unless File.exist?(f_path)
        end
      end
    end

    def download_file(remote)
      uri = URI(remote)
      dirname = local_dir
      local = local_file remote

      unless File.exist? local

        Rails.logger.info("Download file #{remote} into #{local}")

        FileUtils::mkdir_p dirname unless File.exist? dirname

        File.open(local, 'wb') do |fo|
          fo.write open(uri).read
        end
      end

      unzip_file(local) if local.extname == ".zip"
    end

  end

end
