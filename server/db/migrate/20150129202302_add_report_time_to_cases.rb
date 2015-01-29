class AddReportTimeToCases < ActiveRecord::Migration
  def change
    add_column :cases, :report_time, :datetime
  end
end
