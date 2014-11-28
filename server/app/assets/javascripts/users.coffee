ACT.invitations = ->

ACT.invitations["new"] = ->

  role_input = $('#role-input')
  organization_input = $('#organization-input')

  is_admin = () -> role_input.val() == "admin"

  update_inputs = () ->
    if (is_admin())
      organization_input.val ""

    organization_input.prop('disabled', is_admin());

  role_input.bind 'change', update_inputs

  update_inputs()