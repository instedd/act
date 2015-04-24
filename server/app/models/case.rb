require 'elasticsearch/model'

class Case < ActiveRecord::Base
  include Elasticsearch::Model
  include Elasticsearch::Model::Callbacks

  belongs_to :office
  has_many :call_records

  after_save :update_index

  validates_presence_of :guid, :office, :report_time, :patient_phone_number

  delegate :organization,
           :organization_id,
           :supervisor_name,
           :supervisor_phone_number,
           to: :office

  settings do
    mappings do
      indexes :age_group, type: 'string', index: 'not_analyzed'
      indexes :gender, type: 'string', index: 'not_analyzed'
      indexes :sick, type: 'string', index: 'not_analyzed'
      indexes :dialect, type: 'string', index: 'not_analyzed'
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

  def self.save_from_sync_file(office_guid, file_content)
    json = JSON.parse file_content
    office_id = Office.where(guid: office_guid).pluck(:id)[0]

    if office_id.blank?
      error_msg = "Trying to create case for inexistent office guid #{office_guid}. Posted content: #{file_content}"
      Rails.logger.warn error_msg
      raise error_msg
    end

    Case.create! office_id: office_id,\
                 guid: json["guid"],\
                 patient_name: json["name"],\
                 patient_phone_number: json["phone_number"],\
                 patient_age: json["age"].to_i,\
                 patient_gender: json["gender"],\
                 dialect_code: json["dialect_code"],\
                 symptoms: json["symptoms"],\
                 note: json["note"],\
                 report_time: json["report_time"]
  end

  def successful_calls
    call_records.select &:successful
  end

  def calls_report
    successful_calls = self.successful_calls
    report = successful_calls.inject({sick: nil, family_sick: nil, community_sick: nil, symptoms: []}) { | result, call |
      {
        sick: result[:sick] | call.sick,
        family_sick: result[:family_sick] | call.family_sick,
        community_sick: result[:community_sick] | call.community_sick,
        symptoms: result[:symptoms] | call.symptoms.select { |key, value| value }.keys
      }
    }
    if successful_calls.empty? then
      report[:who_is_sick] = "Not contacted yet"
    else
      report[:who_is_sick] = []
      report[:who_is_sick] << "Patient sick" if report[:sick]
      report[:who_is_sick] << "Family member sick" if report[:family_sick]
      report[:who_is_sick] << "Community member sick" if report[:community_sick]
      report[:who_is_sick] = report[:who_is_sick].join ", "
    end

    report[:anyone_sick] = report[:sick] || report[:family_sick] || report[:community_sick]

    report.with_indifferent_access
  end

  def sick
    calls_report[:sick]
  end

  alias_method :sick?, :sick

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
        "report_time",
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
      office_uuid: office.guid,
      institution_id: office.organization_id,
      gender: gender,
      created_at: created_at,
      updated_at: updated_at,
      start_time: report_time || created_at,
      assay_name: 'ebola',
      result: anyone_sick,
      age_group: age_group,
      location_id: office.location.geo_id,
      parent_locations: office.location.hierarchy,
      symptoms: symptoms_for_index,
      dialect: dialect_code,
      location: office.location.detailed_hierarchy
    }
  end

  def all_symptoms
    # original symptoms + the ones reported during calls
    symptoms | calls_report[:symptoms].map { |symptom | CallRecord.reports_to_symptoms[symptom] }
  end

  def symptoms_for_index
    # TODO: standardize symptoms (see #82)
    all_symptoms.map { |symptom| symptom.parameterize.underscore } 
  end

  def symptoms_joined
    all_symptoms.join "\n"
  end

  def sick_status
    case sick?
    when true
      'sick'
    when false
      'not_sick'
    else
      'not_reported'
    end
  end

  def anyone_sick
    case calls_report[:anyone_sick]
    when true
      'sick'
    when false
      'not_sick'
    else
      'not_reported'
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
