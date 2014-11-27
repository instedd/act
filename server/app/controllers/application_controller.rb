class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception

  rescue_from CanCan::AccessDenied do |exception|
    # redirect_to main_app.root_url, :alert => exception.message
    render :file => "#{Rails.root}/public/403.html", :status => 403, :layout => false
  end
end
