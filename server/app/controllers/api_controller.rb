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
  AFFIRMATIVE_ANSWER_CODE = "2"
  NEGATIVE_ANSWER_CODE    = "1"

  def register
    unless params["apiVersion"].to_s == "2"
      render text: "Unsupported client version - please upgrade your client", status: 400
      return
    end
    ActiveRecord::Base.transaction do
      reported_location_code = params["deviceInfo"]["location"].to_s
      location = Location.from_geo_id reported_location_code
      Rails.logger.error "Location #{reported_location_code} not found" unless location

      d = Office.create public_key: params["publicKey"],\
                        reported_organization_name: params["deviceInfo"]["organization"],\
                        reported_location_code: reported_location_code,\
                        location: location,\
                        supervisor_name: params["deviceInfo"]["supervisorName"],\
                        supervisor_phone_number: params["deviceInfo"]["supervisorNumber"]

      if d.save
        Office.init_sync_path(d.guid)
        
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
    _case = Case.joins(:office)
                .select("cases.*, offices.guid as office_guid")
                .find_by_id(params[:id])
    
    unless (_case.present? && !params[:sick].nil?)
      render nothing: true, status: 400
      return
    end

    # assume the call is completed if no status reported
    # as Verboice isn't reporting the status at the moment
    call_status = params.delete(:call_status) || "completed"

    unless call_status == "completed"
      CallRecord.failed! _case: _case, reported_status: call_status
      render nothing: true, status: 200
      return
    end

    symptoms = {}
    known_symptoms = []

    patient_sick = false
    family_sick = false
    community_sick = false

    # As Verboice/Hub are reporting random answers for unasked questions, we only
    # pay attention to the answers we expect the user to have been asked
    if (params[:sick] == AFFIRMATIVE_ANSWER_CODE)
      patient_sick = true
      known_symptoms = ["diarreah_individual", "headache_individual", "hemorrhage_individual", "nausea_vomiting_individual", "rash_individual", "sorethroat_individual", "weakness_pain_individual", "individual_fever"]
    elsif (params[:family_sick] == AFFIRMATIVE_ANSWER_CODE)
      family_sick = true
      known_symptoms = ["diarreah_family", "fever_family", "headache_family", "hemorrhage_family", "nausea_vomiting_family", "rash_family", "sorethroat_family", "weakness_pain_family"]
    elsif (params[:community_sick] == AFFIRMATIVE_ANSWER_CODE)
      community_sick = true
      known_symptoms = ["diarreah_community", "fever_community", "headache_community", "hemorrhage_community", "nausea_vomiting_community", "rash_community", "sorethroat_community", "weakness_pain_community"]
    end
    
    params.each { |key, value|
      symptoms[key] = value == AFFIRMATIVE_ANSWER_CODE if known_symptoms.include? key and !value.blank?
    }

    CallRecord.create! _case: _case, sick: patient_sick, family_sick: family_sick, community_sick: community_sick, symptoms: symptoms

    _case.reload

    Office.sync_sick_status(_case.office_guid, _case.guid, _case.sick)
    
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
