class RenameDevicesLocationIdToLocationCode < ActiveRecord::Migration
  def change
    rename_column :devices, :location_id, :location_code
  end
end
