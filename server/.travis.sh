#!/bin/bash
set -ev
cd server
bundle exec rake db:create db:schema:load
bundle exec rspec