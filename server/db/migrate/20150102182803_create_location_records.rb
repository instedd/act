class CreateLocationRecords < ActiveRecord::Migration
  def change
    create_table :location_records do |t|
      t.references :case, index: true,  null: false
      t.datetime   :created_at,         null: false
      t.float :lat,                     null: false
      t.float :lng,                     null: false
    end
  end
end
