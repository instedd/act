namespace :act do
  namespace :locations do
    desc "Populate locations hierarchy"
    task :hierarchy, [:force] => :environment do |task, args|
      next if Location.count > 0 and !args[:force]
      file_content = Rails.root.join('db', 'seeds', 'locations-hierarchy.json').read
      Location.load_hierarchy(JSON.parse file_content)
    end
  end
end
