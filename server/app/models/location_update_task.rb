require 'rexml/document'

class LocationUpdateTask

  @queue = :location_update

  def self.perform(case_id, number)
    # TO-DO
  end

  def self.perform_async(case_id, number)
    Resque.enqueue_in(30.seconds, self, case_id, number)
  end

end