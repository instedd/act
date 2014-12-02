class CreateNotifications < ActiveRecord::Migration
  def change
    create_table :notifications do |t|
      t.string :notification_type, null: false
      t.hstore :metadata

      t.timestamps
    end
  end
end
