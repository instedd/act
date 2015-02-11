class LocationShape < ActiveRecord::Base
  belongs_to :location, inverse_of: :shape
  # attr_accessible :geo_id, :geo_shape, :geo_type, :location_id

  serialize :geo_shape, JSON
end
