require 'elasticsearch/model'

class Case < ActiveRecord::Base
  include Elasticsearch::Model
  include Elasticsearch::Model::Callbacks

  belongs_to :device

  after_save :update_index

  validates_presence_of :guid
  validates_presence_of :device

  delegate :organization,
           :organization_id,
           :supervisor_name,
           :supervisor_phone_number,
           to: :device

  settings do
    mappings do
      indexes :age_group, type: 'string', index: 'not_analyzed'
      indexes :gender, type: 'string', index: 'not_analyzed'
      indexes :sick, type: 'string', index: 'not_analyzed'
      indexes :location, type: 'nested' do
        indexes :admin_level_0, type: 'string', index: 'not_analyzed'
        indexes :admin_level_1, type: 'string', index: 'not_analyzed'
        indexes :admin_level_2, type: 'string', index: 'not_analyzed'
        indexes :admin_level_3, type: 'string', index: 'not_analyzed'
        indexes :admin_level_4, type: 'string', index: 'not_analyzed'
        indexes :admin_level_5, type: 'string', index: 'not_analyzed'
        indexes :admin_level_6, type: 'string', index: 'not_analyzed'
        indexes :admin_level_7, type: 'string', index: 'not_analyzed'
        indexes :admin_level_8, type: 'string', index: 'not_analyzed'
        indexes :admin_level_9, type: 'string', index: 'not_analyzed'
      end
    end
  end

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

  def as_indexed_json(options={})
    {
      uuid: guid,
      device_uuid: device.guid,
      institution_id: device.organization_id,
      gender: gender,
      created_at: created_at,
      updated_at: updated_at,
      start_time: created_at, # FIXME: receive and use test start time
      assay_name: 'ebola',
      result: 'positive',
      sick: sick_status,
      age_group: age_group,
      location_id: device.location_code,
      parent_locations: device.location.hierarchy,
      location: device.location.detailed_hierarchy
    }
  end

  def sick_status
    case sick
    when true
      'sick'
    when false
      'not_sick'
    else
      'unknown'
    end
  end

  def gender
    if patient_gender.downcase.starts_with? 'f'
      'F'
    elsif patient_gender.downcase.starts_with? 'm'
      'M'
    else
      'U'
    end
  end

  def age_group
    case patient_age
    when 0..1
      "0-2"
    when 2..4
      "2-4"
    when 5..8
      "5-8"
    when 9..17
      "9-17"
    when 18..24
      "18-24"
    when 25..49
      "25-49"
    when 50..64
      "50-64"
    when 65..74
      "65-74"
    when 75..84
      "75-84"
    else
      patient_age >= 85 ? "85+" : ""
    end
  end

  def update_index
    self.__elasticsearch__.index_document
  end

end

Case.__elasticsearch__.create_index!
Case.import
