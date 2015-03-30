module AuthorizedKeys

  def self.add(office_guid, public_key)
    office_sandbox = Office.sync_path(office_guid)
    ssh_command = "#{Settings.rrsync_location} #{office_sandbox}"
    
    authorized_keys_entry = "\ncommand=\"#{ssh_command}\",no-agent-forwarding,no-port-forwarding,no-pty,no-user-rc,no-X11-forwarding #{public_key}"
    File.open(Settings.authorized_keys_file, "a+") do |authorized_keys|
      authorized_keys << authorized_keys_entry
    end
  end

end