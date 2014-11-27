class Case < ActiveRecord::Base

  belongs_to :device
  
  validates_presence_of :guid
  validates_presence_of :device

  delegate :organization, :organization_id, to: :device

  def self.save_from_sync_file(device_guid, file_content)
    json = JSON.parse file_content
    device_id = Device.where(guid: device_guid).pluck(:id)[0]

    if device_id.blank?
      error_msg = "Trying to create case for inexisten device guid #{device_guid}. Posted content: #{file_content}"
      Rails.logger.warn error_msg
      raise error_msg
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

  def as_json_for_api
    ret = self.as_json.select do |k|
      [
        "id",
        "guid",
        "patient_name",
        "patient_phone_number",
        "patient_age",
        "patient_gender",
        "dialect_code",
        "symptoms",
        "note"
      ].include? k
    end
  end

end