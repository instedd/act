module AuthorizedKeys

  def self.add(device_guid, public_key)
    device_sandbox = Device.sync_path(device_guid)
    ssh_command = "#{Settings.rrsync_location} #{device_sandbox}"
    
    authorized_keys_entry = "\ncommand=\"#{ssh_command}\",no-agent-forwarding,no-port-forwarding,no-pty,no-user-rc,no-X11-forwarding #{public_key}"
    File.open(Settings.authorized_keys_file, "a+") do |authorized_keys|
      authorized_keys << authorized_keys_entry
    end
  end

end