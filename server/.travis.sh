#!/bin/bash
set -ev
cd server
bundle exec rake db:schema:load
bundle exec rspec