class Device < ActiveRecord::Base

  has_many :cases

  validates_presence_of [
    :public_key,
    :organization_name,
    :location_id,
    :supervisor_name,
    :supervisor_phone_number
  ]

  before_save do |device|
    device.guid ||= SecureRandom.hex(16).upcase
    true
  end

  
  def self.sync_sick_status(device_guid, case_guid, sick_condition)
    document_name = "case-#{case_guid}.json"
    document_content = {sick: sick_condition}

    outbox_path = outbox(device_guid)
    file = File.open(File.join(outbox_path, document_name), "w")
    file.puts document_content.to_json
    file.close
  end

  def self.init_sync_path(device_guid)
    device_sync_path = sync_path(device_guid)

    unless Dir.exists? device_sync_path
      Dir.mkdir device_sync_path
      Dir.mkdir outbox(device_guid)
      Dir.mkdir inbox(device_guid)
    end
  end

  private

  def self.sync_path(device_guid)
    File.expand_path File.join(Settings.sync_directory, device_guid)
  end

  def self.outbox(device_guid)
    File.join(sync_path(device_guid), "outbox")
  end

  def self.inbox(device_guid)
    File.join(sync_path(device_guid), "inbox")
  end

end