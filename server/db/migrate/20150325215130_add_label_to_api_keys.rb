class AddLabelToApiKeys < ActiveRecord::Migration
  def change
    add_column :api_keys, :label, :string
  end
end
