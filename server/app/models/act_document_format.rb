#
# Used to map CDX queries and results.
#
class ActDocumentFormat < CDPDocumentFormat

  # receives an event in the format used in ES and
  # translates it into a CDP compliant response
  def translate_event(event)
    mapped_event = event

    if uuid = mapped_event.delete("guid")
      mapped_event['uuid'] = uuid
      mapped_event['event_id'] = uuid
    end

    # show results as nested object of the CDX event
    # (only if this is not a group_by result)
    if mapped_event.include?("result") and !mapped_event.include?("count")
      mapped_event["results"] = [
        {
          "assay_name" => mapped_event["assay"],
          "result"    => mapped_event.delete("result")
        }
      ]
    end

    mapped_event
  end

  #
  # Since we do not have nested results but only one condition/result
  # per test, redefine field definitions to build query for non nested
  # fields while keeping the same query parameter names.
  #
  def override_fields(field_definitions)
    results_field = field_definitions.detect { |f| f.name == "results" }

    field_definitions.push *results_field.sub_fields
    field_definitions.delete results_field

    field_definitions.push Cdx::Api::Elasticsearch::IndexedField.new({name: "age_group"}, self)
    field_definitions.push Cdx::Api::Elasticsearch::IndexedField.new({name: "sick"}, self)

    field_definitions
  end
end
