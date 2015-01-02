#! /bin/bash

set -e

if (bundle exec rake db:version &> /dev/null);
  then bundle exec rake db:migrate;
  else bundle exec rake db:setup;
fi;

echo "Geo :: Downloading..."
bundle exec rake geo:download
echo "Geo :: Downloaded! Importing..."
bundle exec rake geo:import
echo "Geo :: Imported! Indexing..."
bundle exec rake geo:reindex
echo "Geo :: Indexed! Validating..."
bundle exec rake geo:validate
echo "Geo :: Validated!"

scripts/00--adapt_polygons.sh
