ACT.offices = ->

ACT.offices["index"] = ->

  $('.organization-confirmation').bind 'change', (e) ->
    organization_id = e.target.value
    confirm_button = $(e.target).closest("td").find("[type=submit]")

    confirm_button.prop 'disabled', !organization_id

  $('.organization-confirmation').trigger 'change'