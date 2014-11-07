class ApiController < ApplicationController
  
  def cases
    cases = Case.order(:created_at)
    cases = cases.where("id > ?", params[:since_id]) if params[:since_id].present?
    
    render json: cases.map(&:as_json_for_api)
  end

end
