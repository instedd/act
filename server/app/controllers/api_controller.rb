class ApiController < ApplicationController
  
  def cases
    cases = Case.order(:created_at)
    cases = cases.where("created_at > ?", params[:since_date]) if params[:since_date].present?
    
    render json: cases.map(&:as_json_for_api)
  end

end
