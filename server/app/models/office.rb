class Office < ActiveRecord::Base

  has_many   :cases
  belongs_to :organization

  validate :check_public_key

  belongs_to :location

  validates_presence_of [
    :public_key,
    :reported_organization_name,
    :reported_location_code,
    :location,
    :supervisor_name,
    :supervisor_phone_number
  ]

  before_save do |office|
    office.guid ||= SecureRandom.uuid
    true
  end

  def check_public_key
    match = /\Assh-rsa AAAA[0-9A-Za-z\+\/]+[=]{0,3} (.+@.+)\Z/.match public_key
    if match.nil?
      errors.add(:public_key, "is not valid")
    end
  end

  
  def self.sync_sick_status(office_guid, case_guid, sick_condition)
    document_name = "case-#{case_guid}.json"
    document_content = {sick: sick_condition}

    outbox_path = outbox(office_guid)
    file = File.open(File.join(outbox_path, document_name), "w")
    file.puts document_content.to_json
    file.close
  end

  def self.init_sync_path(office_guid)
    office_sync_path = sync_path(office_guid)

    unless Dir.exists? office_sync_path
      Dir.mkdir office_sync_path
      Dir.mkdir outbox(office_guid)
      Dir.mkdir inbox(office_guid)
    end
  end

  private

  def self.sync_path(office_guid)
    File.expand_path File.join(Settings.sync_directory, office_guid)
  end

  def self.outbox(office_guid)
    File.join(sync_path(office_guid), "outbox")
  end

  def self.inbox(office_guid)
    File.join(sync_path(office_guid), "inbox")
  end

end