class AddReportedLocationCodeToDevice < ActiveRecord::Migration
  def change
    remove_column :devices, :location_code, :integer
    add_column :devices, :reported_location_code, :string
    add_column :devices, :location_id, :integer
  end
end
