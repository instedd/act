class AddPublicKeyToDevices < ActiveRecord::Migration
  def change
    add_column :devices, :public_key, :text
  end
end
