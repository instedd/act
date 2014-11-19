class Device < ActiveRecord::Base

  has_many :cases

  validates_presence_of [
    :guid,
    :public_key,
    :organization_name,
    :location_id,
    :supervisor_name,
    :supervisor_phone_number
  ]


  def self.save_from_sync_file(device_guid, file_content)
    device = Device.find_by_guid device_guid
    json = JSON.parse file_content
    
    if device.present?
      device.update_attributes organization_name: json["organization"],\
                               location_id: json["location"].to_i
    else
      Device.create! guid: device_guid,\
                     organization_name: json["organization"],\
                     location_id: json["location"].to_i,\
                     supervisor_name: json["supervisorName"],\
                     supervisor_phone_number: json["supervisorNumber"]
    end
  end
  
  def self.sync_sick_status(device_guid, case_guid, sick_condition)
    document_name = "case-#{case_guid}.json"
    document_content = {sick: sick_condition}

    outbox_path = mkdir_device_outbox(device_guid)
    file = File.open(File.join(outbox_path, document_name), "w")
    file.puts document_content.to_json
    file.close
  end

  private

  def self.mkdir_device_outbox(device_guid)
    outbox_path = device_outbox(device_guid)
    unless Dir.exists? outbox_path
      Dir.mkdir outbox_path
    end
    outbox_path
  end

  def self.device_outbox(device_guid)
    File.join(Settings.sync_directory_outbox, device_guid)
  end

end