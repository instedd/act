class Cdx::V1::EventsController < AuthenticatedController

  skip_before_filter :verify_authenticity_token
  before_filter :allow_cross_domain_access if Rails.env.development?

  def index
    params.delete :event # FIXME: this delete shouldn't be necessary
    params['institution'] = current_user.organization_id unless current_user.admin?
    query = Cdx::Api::Elasticsearch::Query.new(params)
    results = query.execute
    render json: results
  end

  def schema
    # FIXME: cache this?
    user_organizations = Organization.accessible_by current_ability
    user_organizations_ids = user_organizations.map(&:id)
    user_organizations_values = Hash[user_organizations.map { |org| [ org.id, { name: org.name} ] }]
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
    "institution": {
      "title": "Organization",
      "type": "string",
      "enum": #{ user_organizations_ids.to_json },
      "values": #{ user_organizations_values.to_json }
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
        "not_reported"
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
        "not_reported": {
          "name": "N/A",
          "kind": "negative"
        }
      }
    },
    "symptoms": {
      "title": "Symptoms",
      "type": "String",
      "enum": [
        "fever",
        "severe_headache",
        "muscle_pain",
        "weakness",
        "fatigue",
        "diarrhea",
        "vomiting",
        "abdominal_stomach_pain",
        "unexplained_hemorrhage_bleeding_or_bruising",
        "sore_throat",
        "rash"
      ],
      "values": {
        "fever": {
          "name": "Fever"
        },
        "severe_headache": {
          "name": "Severe headache"
        },
        "muscle_pain": {
          "name": "Muscle pain"
        },
        "weakness": {
          "name": "Weakness"
        },
        "fatigue": {
          "name": "Fatigue"
        },
        "diarrhea": {
          "name": "Diarrhea"
        },
        "vomiting": {
          "name": "Vomiting"
        },
        "abdominal_stomach_pain": {
          "name": "Abdominal stomach pain"
        },
        "unexplained_hemorrhage_bleeding_or_bruising": {
          "name": "Unexplained hemorrhage"
        },
        "sorethroat": {
          "name": "Sore throat"
        },
        "rash": {
          "name": "Rash"
        }
      }
    },
    "dialect": {
      "title": "Dialect",
      "type": "String",
      "enum": [
        "Afrikaans",
        "English",
        "French",
        "Kiswahili"
      ]
    },
    "location": {
      "title": "Office Location",
      "description": "Location of the office reporting the case",
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
