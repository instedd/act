class AddGuidToCases < ActiveRecord::Migration
  def change
    add_column :cases, :guid, :string
  end
end
