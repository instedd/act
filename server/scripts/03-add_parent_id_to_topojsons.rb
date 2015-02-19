#!/usr/bin/env ruby
require 'json'

def parent_id loc_id
  parts = loc_id.split "_"
  parts[0..-2].join "_"
end

Dir.glob('etc/shapes/GINLBRSLE_adm?\.topo.json') do |topojson_path|
  topojson = JSON.parse(File.read(topojson_path, external_encoding: 'utf-8'))
  geometries = topojson["objects"].values.first["geometries"]
  geometries.each do |geometry|
    geometry["properties"] = {
      ID: geometry["id"],
      PARENT_ID: parent_id(geometry["id"]),
      NAME: geometry["properties"]["NAME"]
    }
  end
  File.new(topojson_path.gsub(".topo.json", "_id.topo.json"), 'w').write(topojson.to_json)
  File.unlink(topojson_path)
end
