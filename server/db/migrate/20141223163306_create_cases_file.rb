class CreateCasesFile < ActiveRecord::Migration
  def change
    create_table :cases_files do |t|
      t.string :guid
      t.string :file, null: false
      t.integer :device_id
      t.string :status

      t.timestamps
    end
  end
end
