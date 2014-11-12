class AddSickToCase < ActiveRecord::Migration
  def change
    add_column :cases, :sick, :boolean
  end
end
