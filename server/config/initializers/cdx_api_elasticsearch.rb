require "cdx/api/elasticsearch"

Cdx::Api.setup do |config|
  format = ActDocumentFormat.new
  
  config.document_format = format
  format.override_fields(config.searchable_fields)

  if Rails.env.test?
    config.index_name_pattern = "testing_cases"
  else
    config.index_name_pattern = "cases"
  end

  config.log = !Rails.env.test?

  config.elasticsearch_url = Settings.elasticsearch_url if Settings.elasticsearch_url
end
