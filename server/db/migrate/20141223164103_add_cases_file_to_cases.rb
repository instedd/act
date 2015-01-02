class AddCasesFileToCases < ActiveRecord::Migration
  def change
    add_column :cases, :cases_file_id, :integer, null: true
  end
end
