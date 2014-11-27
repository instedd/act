class Users::InvitationsController < Devise::InvitationsController

  before_filter :configure_permitted_parameters

  private

  def configure_permitted_parameters
    devise_parameter_sanitizer.for(:invite).concat [:organization_id]
  end

end