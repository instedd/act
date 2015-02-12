require 'elasticsearch/model'

class Location < ActiveRecord::Base
  include Elasticsearch::Model

  acts_as_nested_set dependent: :destroy

  has_one :shape, class_name: 'LocationShape', dependent: :destroy

  validates_uniqueness_of :geo_id, allow_nil: false

  validates :lat, :numericality => {:greater_than_or_equal_to => -90, :less_than_or_equal_to => 90}, :unless => Proc.new { lat.blank? }
  validates :lng, :numericality => {:greater_than_or_equal_to => -180, :less_than_or_equal_to => 180}, :unless => Proc.new { lng.blank? }

  # Monkeypatch for awesome nested set, which is not rebuilding depth
  def self.rebuild!
    items = self.where(:parent_id => nil)
    level = 0
    while items.any?
      items.update_all :depth => level
      items = self.where(:parent_id => items.map(&:id))
      level += 1
    end
    super
  end

  # Monkeypatch for awesome nested set, which is not properly sorting each_with_level
  def self.custom_sorted_each_with_level(objects, order)

    node_class = Struct.new(:value, :parent, :level, :children)

    current = node_class.new(nil, nil, -1, [])                       # node whose children we are collecting
    tree = [current]                                                 # location tree root

    parent_scope = objects.first && objects.first.parent_id || nil   # invariant: path.last is the parent id of the node being iterated.
                                                                     #            set nil if building tree for top level locations, or
                                                                     #            the parent of the first if all locations are scoped.

    path = [parent_scope]                                            # path in the tree until the location being iterated


    objects.each do |o|
      if o.parent_id != path.last
        # We are on a new level
        if path.include?(o.parent_id)
          # if we ascended, remove wrong tailing paths elements and update current obj
          while path.last != o.parent_id
            path.pop
            current = current.parent
          end
        else
          # if we descended, append parent to descending path and set current to new child
          path << o.parent_id
          current = current.children.last
        end
      end
      current.children << node_class.new(o, current, path.length-1, [])
    end

    show_children = Proc.new { |node|
      node.children.sort_by!{|n| n.value.send(order)}
      node.children.each do |child|
        yield(child.value, child.level)
        show_children.call(child)
      end
    }

    show_children.call(tree.first)
  end

  def descents_from?(another_location)
    self.lft >= another_location.lft && self.lft <= another_location.rgt
  end

  def strictly_descents_from?(another_location)
    self != another_location && descents_from?(another_location)
  end

  def self_and_ancestors_and_descendants
    (self.self_and_ancestors + self.descendants).uniq
  end

  def coordinates
    {lat: lat, lng: lng, depth: depth}
  end

  def center
    [lng, lat]
  end

  def hierarchy
    location_path = geo_id.split "_"
    hierarch = location_path.length.times.map do |time|
      location_path[0..time].join("_")
    end
    hierarch.reverse
  end

  def detailed_hierarchy
    Hash[
      hierarchy.reverse.each_with_index.map do |location_geo_id, index|
        ["admin_level_#{index}", location_geo_id]
      end
    ]
  end

  def self.from_geo_id location_geo_id
    self.where(geo_id: location_geo_id.to_s).first
  end

  def self.from_coordinates lat, lng
    results = Location.search(<<-QUERY
      {
        "_source": {
          "exclude": [
            "shape"
          ]
        },
        "query": {
          "filtered": {
            "query": {
              "match_all": {}
            },
            "filter": {
              "geo_shape": {
                "shape": {
                  "shape": {
                    "type": "point",
                    "coordinates": [
                      #{lat},
                      #{lng}
                    ]
                  }
                }
              }
            }
          }
        }
      }
    QUERY
    ).results
    location_id = results.max_by(&:level).location_id
    self.find(location_id)
  end

end
