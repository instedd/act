class LocationRecord < ActiveRecord::Base

  belongs_to :case
  validates_presence_of :case, :lat, :lng
end