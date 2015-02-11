module Geo

  class Feature

    attr_accessor :name, :level, :location_id, :center, :parent_id, :type, :coordinates

    def as_location_shape_attributes
      {
        geo_id: location_id.to_s,
        geo_type: type,
        geo_shape: coordinates
      }
    end

  end

end
