class AddOrganizationToDevices < ActiveRecord::Migration

  def up
    add_column    :devices, :organization_id, :integer
    rename_column :devices, :organization_name, :reported_organization_name
    add_index     :devices, :organization_id
  end

  def down
    remove_column :devices, :organization_id
    rename_column :devices, :reported_organization_name, :organization_name
  end

end
