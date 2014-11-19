module AuthorizedKeys

  def self.add(public_key)
    File.open(Settings.authorized_keys_file, "a+") do |authorized_keys|
      authorized_keys << public_key
    end
  end

end