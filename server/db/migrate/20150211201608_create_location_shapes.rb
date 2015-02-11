class CreateLocationShapes < ActiveRecord::Migration
  def change
    create_table :location_shapes do |t|
      t.references :location
      t.string :geo_id
      t.text :geo_shape, limit: 16_777_215
      t.string :geo_type

      t.timestamps
    end

    add_index :location_shapes, :location_id
  end
end
