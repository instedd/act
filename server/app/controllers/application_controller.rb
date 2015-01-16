class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception

  rescue_from CanCan::AccessDenied do |exception|
    # redirect_to main_app.root_url, :alert => exception.message
    render :file => "#{Rails.root}/public/403.html", :status => 403, :layout => false
  end

  before_action :configure_user_edit_parameters, if: :devise_controller?

  protected

  def configure_user_edit_parameters
    devise_parameter_sanitizer.for(:account_update) { |u| u.permit([:password, :password_confirmation, :current_password])}
  end
end
