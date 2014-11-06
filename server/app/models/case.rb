class Case < ActiveRecord::Base

  belongs_to :device
  
  validates_presence_of :guid
  validates_presence_of :device

  def self.save_from_sync_file(device_guid, file_content)
    json = JSON.parse file_content
    device_id = Device.where(guid: device_guid).pluck(:id)[0]

    if device_id.blank?
      device_id = (Device.create! guid: device_guid).id
    end

    Case.create! device_id: device_id,\
                 guid: json["guid"],\
                 patient_name: json["name"],\
                 patient_phone_number: json["phone_number"],\
                 patient_age: json["age"].to_i,\
                 patient_gender: json["gender"],\
                 dialect_code: json["dialect_code"],\
                 symptoms: json["symptoms"],\
                 note: json["note"]
  end

end