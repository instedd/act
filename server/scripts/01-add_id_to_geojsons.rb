#!/usr/bin/env ruby
require 'json'

Dir.glob('etc/shapes/GINLBRSLE_adm?\.json') do |geojson_path|
  geojson = JSON.parse(File.read(geojson_path, external_encoding: "utf-8"))
  geojson["features"].each do |feature|
    props = feature["properties"]
    new_id = []
    ["ID_0", "ID_1", "ID_2", "ID_3"].each do |id_field|
      new_id << props[id_field] if props.has_key? id_field
    end
    props["ID"] = new_id.join "_"

    ["NAME_3", "NAME_2", "NAME_1", "NAME_FAO"].each do |name_field|
      props["NAME"] ||= props[name_field]
    end

  end
  File.new(geojson_path.gsub(".json", ".plus_json"), 'w').write(geojson.to_json)
end
