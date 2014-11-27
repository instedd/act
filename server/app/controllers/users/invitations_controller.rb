class Users::InvitationsController < Devise::InvitationsController

  before_filter :authorize_invitation_creation, only: [:new, :create]
  before_filter :configure_permitted_parameters

  private

  def authorize_invitation_creation
    unless current_user.can? :create, :invitations
      raise CanCan::AccessDenied.new("Not authorized", :create, :invitations)
    end
  end

  def configure_permitted_parameters
    devise_parameter_sanitizer.for(:invite).concat [:organization_id]
  end

end