class CreateDevices < ActiveRecord::Migration
  def change
    create_table :devices do |t|
      t.string  :guid
      t.string  :organization_name
      t.integer :location_id
      t.string  :supervisor_phone_number
      t.timestamps
    end
  end
end
