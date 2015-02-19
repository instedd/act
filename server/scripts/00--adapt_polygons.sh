#!/bin/sh
set -e
scripts/01-add_id_to_geojsons.rb

for file in etc/shapes/GINLBRSLE_adm?.plus_json
do
  topojson "$file" -o "${file%.plus_json}.topo.json" --id-property ID --properties && rm "$file"
done

scripts/03-add_parent_id_to_topojsons.rb
