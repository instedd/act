class LocationRecord < ActiveRecord::Base

  belongs_to :case
  belongs_to :location
  validates_presence_of :case, :lat, :lng
end