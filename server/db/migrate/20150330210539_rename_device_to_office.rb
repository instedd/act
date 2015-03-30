class RenameDeviceToOffice < ActiveRecord::Migration
  def change
    rename_table :devices, :offices
    rename_column :cases, :device_id, :office_id
  end
end
