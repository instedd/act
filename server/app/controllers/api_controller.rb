class ApiController < ApplicationController

  protect_from_forgery with: :null_session

  def register
    d = Device.create! public_key: params["publicKey"],\
                       organization_name: params["deviceInfo"]["organization"],\
                       location_id: params["deviceInfo"]["location"].to_i,\
                       supervisor_name: params["deviceInfo"]["supervisorName"],\
                       supervisor_phone_number: params["deviceInfo"]["supervisorNumber"]

    Device.init_sync_path(d.guid)
    AuthorizedKeys.add(d.guid, d.public_key)

    render nothing: true, status: 200
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

    _case.sick = params[:sick]
    _case.save!

    Device.sync_sick_status(_case.device_guid, _case.guid, _case.sick)
    
    render nothing: true, status: 200
  end

end
