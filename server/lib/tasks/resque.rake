require "resque/tasks"
require 'resque/scheduler/tasks'

namespace :resque do
  task :setup => :environment do
    Resque.redis = "#{Settings.redis.host}:#{Settings.redis.port}"
    Resque.schedule = YAML.load_file(Rails.root.join("config/resque_schedule.yml"))
  end
end
