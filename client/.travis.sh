#!/bin/bash
set -ev
cd client
TERM=dumb ./gradlew build
