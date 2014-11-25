#! /bin/bash

set -e

if (bundle exec rake db:version &> /dev/null);
  then bundle exec rake db:migrate;
  else bundle exec rake db:setup;
fi;