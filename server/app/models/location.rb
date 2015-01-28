class Location < ActiveRecord::Base

  serialize :parents

  def hierarchy
    [code] + parents
  end

  def detailed_hierarchy
    Hash[
      hierarchy.reverse.each_with_index.map do |location_code, index|
        ["admin_level_#{index}", location_code]
      end
    ]
  end

  def self.from_code location_code
    self.where(code: location_code).first
  end

  def self.load_hierarchy locations
    locations.each do |location_code, parents|
      Location.create! code: location_code, parents: parents
    end
  end

end
