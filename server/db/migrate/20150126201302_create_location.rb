class CreateLocation < ActiveRecord::Migration
  def change
    create_table :locations do |t|
      t.string  :code
      t.text :parents
    end
  end
end
