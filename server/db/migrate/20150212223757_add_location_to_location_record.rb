class AddLocationToLocationRecord < ActiveRecord::Migration
  def change
    add_column :location_records, :location_id, :integer
  end
end
