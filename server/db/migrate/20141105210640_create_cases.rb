class CreateCases < ActiveRecord::Migration
  def change
    create_table :cases do |t|
      t.integer :device_id
      t.string  :patient_name
      t.string  :patient_phone_number
      t.string  :patient_age
      t.string  :patient_gender
      t.string  :dialect_code
      t.text    :symptoms
      t.text    :note
      t.timestamps
    end
  end
end
