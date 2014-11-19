class AddPublicKeyAndConfirmedFlagToDevices < ActiveRecord::Migration
  def change
    add_column :devices, :public_key, :text
    add_column :devices, :confirmed,  :boolean, default: false
  end
end
