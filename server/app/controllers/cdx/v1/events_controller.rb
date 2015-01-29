class Cdx::V1::EventsController < ApplicationController

  skip_before_filter :verify_authenticity_token
  before_filter :allow_cross_domain_access if Rails.env.development?

  def index
    params.delete :event # FIXME: this delete shouldn't be necessary
    query = Cdx::Api::Elasticsearch::Query.new(params)
    results = query.execute
    render json: results
  end

  def schema
    # FIXME: cache this?
    schema_start = <<-JSON
{
  "type": "object",
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "ebola.en",
  "properties": {
    "assay_name": {
      "title": "Assay name",
      "type": "string",
      "enum": [
        "ebola"
      ],
      "values": {
        "ebola": {
          "name": "Ebola"
        }
      }
    },
    "start_time": {
      "title": "Date",
      "type": "string",
      "format": "date-time",
      "resolution": "day"
    },
    "gender": {
      "title": "Gender",
      "type": "string",
      "enum": [
        "M",
        "F"
      ],
      "values": {
        "M": {
          "name": "Male"
        },
        "F": {
          "name": "Female"
        }
      }
    },
    "age_group": {
      "title": "Age group",
      "type": "string",
      "enum": [
        "0-2",
        "2-4",
        "5-8",
        "9-17",
        "18-24",
        "25-49",
        "50-64",
        "65-74",
        "75-84",
        "85+"
      ],
      "values": {
        "0-2": {
          "name": "< 2 years"
        },
        "2-4": {
          "name": "2 years - 4 years"
        },
        "5-8": {
          "name": "5 years - 8 years"
        },
        "9-17": {
          "name": "9 years - 17 years"
        },
        "18-24": {
          "name": "18 years - 24 years"
        },
        "25-49": {
          "name": "25 years - 49 years"
        },
        "50-64": {
          "name": "50 years - 64 years"
        },
        "65-74": {
          "name": "65 years - 74 years"
        },
        "75-84": {
          "name": "75 years - 84 years"
        },
        "85+": {
          "name": "85+ years"
        }
      }
    },
    "result": {
      "title": "Result",
      "type": "string",
      "enum": [
        "sick",
        "not_sick",
        "unknown"
      ],
      "values": {
        "sick": {
          "name": "Sick",
          "kind": "positive"
        },
        "not_sick": {
          "name": "Not sick",
          "kind": "negative"
        },
        "unknown": {
          "name": "N/A",
          "kind": "negative"
        }
      }
    },
    "location": {
      "title": "Device Location",
      "description": "Location of the device reporting the case",
      "type": "location",
      "locations":
        JSON

      schema_end = <<-JSON

    }
  }
}
    JSON

    locations = File.read(Rails.public_path.join("cdx-locations-packed.json"))

    render text: "#{schema_start}#{locations}#{schema_end}"
  end

  private

  def allow_cross_domain_access
    headers['Access-Control-Allow-Origin'] = '*'
    headers['Access-Control-Allow-Methods'] = 'GET, POST, PUT, DELETE, OPTIONS'
    headers['Access-Control-Allow-Headers'] = %w{Origin Accept Content-Type X-Requested-With X-CSRF-Token}.join(',')
    headers['Access-Control-Max-Age'] = '1728000'
  end

end