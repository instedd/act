class UpdateLocationsModel < ActiveRecord::Migration
  def change
    remove_column :locations, :parents, :text
    remove_column :locations, :code, :string

    add_column :locations, :parent_id, :integer
    add_column :locations, :name, :string
    add_column :locations, :lft, :integer
    add_column :locations, :rgt, :integer
    add_column :locations, :lat, :float
    add_column :locations, :lng, :float
    add_column :locations, :depth, :integer
    add_column :locations, :geo_id, :string
    add_column :locations, :created_at, :datetime
    add_column :locations, :updated_at, :datetime
  end
end
