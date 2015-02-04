require 'elasticsearch/model'
require 'elasticsearch/transport'

Elasticsearch::Model.client = Elasticsearch::Client.new url: Settings.elasticsearch_url if Settings.elasticsearch_url
