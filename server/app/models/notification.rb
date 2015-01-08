class Notification < ActiveRecord::Base

  validates_presence_of :notification_type

  def as_json_for_api
    self.as_json.select do |k|
      [ "id", "notification_type", "metadata" ].include? k
    end
  end

  def self.case_confirmed_sick!(c)
    Notification.create notification_type: :case_confirmed_sick,\
                        metadata: c.as_json_for_notification_api
  end

end