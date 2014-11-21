namespace :act do

  desc "Adds new confirmed devices' public keys to the authorized_keys file"

  task keys_import: :environment do
    Rails.logger = Logger.new(STDOUT)
    Rails.logger.info "Monitoring new public keys for confirmed devices"
    while true
      import_new_keys
      sleep 5.seconds
    end
  end

  def import_new_keys
    keys_to_add = Device.where(confirmed: true, public_key_allowed: false)
                        .pluck(:id, :guid, :public_key)

    keys_to_add.each do |id, guid, public_key|
      begin
        Rails.logger.info "Adding key for device #{guid}"
        AuthorizedKeys.add(guid, public_key)
        Device.update id, public_key_allowed: true
      rescue => ex
        Rails.logger.error "An error occurred adding key for device #{guid}: "\
                           "#{ex}\n#{ex.backtrace.take(15).join("\n")}"
      end
    end
  end

end
