class ApiController < ApplicationController

  before_filter :restrict_access, except: [:register]
  protect_from_forgery with: :null_session

  #
  # We use these codes instead of boolean values to simplify
  # interaction with Verboice (the only current method of
  # recollection of follow up information).
  #
  # Ideally, Verboice would be in charge of interpreting the
  # values entered by the user.
  #
  AFFIRMATIVE_ANSWER_CODE = "1"
  NEGATIVE_ANSWER_CODE    = "2"

  def register
    unless params["apiVersion"].to_s == "2"
      render text: "Unsupported client version - please upgrade your client", status: 400
      return
    end
    ActiveRecord::Base.transaction do
      reported_location_code = params["deviceInfo"]["location"].to_s
      location = Location.from_geo_id reported_location_code
      Rails.logger.error "Location #{reported_location_code} not found" unless location

      d = Device.create public_key: params["publicKey"],\
                        reported_organization_name: params["deviceInfo"]["organization"],\
                        reported_location_code: reported_location_code,\
                        location: location,\
                        supervisor_name: params["deviceInfo"]["supervisorName"],\
                        supervisor_phone_number: params["deviceInfo"]["supervisorNumber"]

      if d.save
        Device.init_sync_path(d.guid)
        
        render nothing: true, status: 200
      elsif d.errors.any?
        render text: d.errors.full_messages.to_s, status: 400
      end
    end

  end
  
  def cases
    cases = Case.order(:created_at)
    cases = cases.where("id > ?", params[:since_id]) if params[:since_id].present?
    
    render json: cases.map(&:as_json_for_api)
  end

  def update_case
    _case = Case.joins(:device)
                .select("cases.*, devices.guid as device_guid")
                .find_by_id(params[:id])
    
    unless (_case.present? && !params[:sick].nil?)
      render nothing: true, status: 400
      return
    end

    case params[:sick]
    when AFFIRMATIVE_ANSWER_CODE
      _case.follow_up_sick!
    when NEGATIVE_ANSWER_CODE
      _case.follow_up_not_sick!
    end

    Device.sync_sick_status(_case.device_guid, _case.guid, _case.sick)
    
    render nothing: true, status: 200
  end

  def notifications
    notifications = Notification.order(:created_at)
    
    if params[:since_id].present?
      notifications = notifications.where("id > ?", params[:since_id])
    end

    if params[:notification_type].present?
      notifications = notifications.where("notification_type = ?", params[:notification_type])
    end

    render json: notifications.map(&:as_json_for_api)
  end

  private

  def restrict_access
    authenticate_or_request_with_http_token do |token, other_options|
      ApiKey.exists?(access_token: token)
    end
  end

end
