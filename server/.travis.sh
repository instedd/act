#!/bin/bash
set -ev
cd server
bundle exec rake db:setup
bundle exec rspec --require rails_helper