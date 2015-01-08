class Case < ActiveRecord::Base

  belongs_to :device
  belongs_to :cases_file
  
  validates_presence_of :guid
  validates_presence_of :device

  delegate :organization,
           :organization_id,
           :supervisor_name,
           :supervisor_phone_number,
           to: :device

  def self.save_from_sync_file(device_guid, file_content)
    json = JSON.parse file_content
    save_from_sync_json(device_guid, json)
  end

  def self.save_from_sync_json(device_guid, json, cases_file = nil)
    device_id = Device.where(guid: device_guid).pluck(:id)[0]

    if device_id.blank?
      error_msg = "Trying to create case for inexistent device guid #{device_guid}. Posted content: #{file_content}"
      Rails.logger.warn error_msg
      raise error_msg
    end

    newCase = Case.create! device_id: device_id,\
                 guid: json["guid"],\
                 patient_name: json["name"],\
                 patient_phone_number: json["phone_number"],\
                 patient_age: json["age"].to_i,\
                 patient_gender: json["gender"],\
                 dialect_code: json["dialect_code"],\
                 symptoms: json["symptoms"],\
                 note: json["note"]

    newCase.cases_file = cases_file if cases_file

    newCase.save!

    newCase
  end

  def follow_up_not_sick!
    self.sick = false
    self.save
  end

  def follow_up_sick!
    previously_sick = self.sick
    self.sick = true
    Notification.case_confirmed_sick! self unless previously_sick
    self.save
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

  def as_json_for_notification_api
    ret = self.as_json(methods: %w(supervisor_name supervisor_phone_number)).select do |k|
      [
        "dialect_code",
        "guid",
        "id",
        "patient_name",
        "patient_phone_number",
        "supervisor_name",
        "supervisor_phone_number",
        "symptoms"
      ].include? k
    end
  end

end