class AddSupervisorNameToDevices < ActiveRecord::Migration
  def change
    add_column :devices, :supervisor_name, :string
  end
end
