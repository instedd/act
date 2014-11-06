class Device < ActiveRecord::Base

  has_many :cases

  validates_presence_of :guid

  def self.save_from_sync_file(device_guid, file_content)
    device = Device.find_by_guid device_guid
    json = JSON.parse file_content
    
    if device.present?
      device.update_attributes organization_name: json["organization"],\
                               location_id: json["location"].to_i
    else
      Device.create! guid: device_guid,\
                     organization_name: json["organization"],\
                     location_id: json["location"].to_i
    end
  end

end