class AddExtraResponsesToCallRecords < ActiveRecord::Migration
  def change
    add_column :call_records, :family_sick, :boolean
    add_column :call_records, :community_sick, :boolean
    add_column :call_records, :symptoms, :text
  end
end
