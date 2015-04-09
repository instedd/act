class AddReportedStatusToCallRecords < ActiveRecord::Migration
  def change
    add_column :call_records, :successful, :boolean, default: true
    add_column :call_records, :reported_status, :string
  end
end
