class AddOrganizationToUsers < ActiveRecord::Migration

  def up
      add_column :devices, :public_key_allowed, :boolean, default: false
      execute "UPDATE devices SET public_key_allowed = true"
    end

    def down
      remove_column :devices, :public_key_allowed
    end

  def change
    add_column :users, :organization_id, :integer
  end
end
