class CreateCallRecords < ActiveRecord::Migration
  def change
    create_table :call_records do |t|
      t.integer :case_id
      t.boolean :sick
      t.timestamps null: false
    end

    remove_column :cases, :sick, :boolean
  end
end
