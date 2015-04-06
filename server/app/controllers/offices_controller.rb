class OfficesController < AuthenticatedController

  load_and_authorize_resource

  def index
    confirmed_offices = @offices.where(confirmed: true)
    pending_offices   = @offices.where(confirmed: false)

    @view_pending = current_user.admin? && (params[:pending] || (pending_offices.any? && confirmed_offices.empty?))

    if @view_pending
      @confirmed_offices_count = confirmed_offices.count
      @offices = pending_offices
    else
      @pending_offices_count = pending_offices.count
      @offices = confirmed_offices
    end
  end

  def update
    organization = Organization.find_by_id(params[:office][:organization_id]) rescue nil
    confirmed = params[:office][:confirmed]
    unless organization and confirmed
      return render nothing: true, status: 400
    end

    office = Office.find_by_id(params[:id])
    office.confirmed = true
    office.organization = organization
    office.save
    
    flash[:notice] = "Office confirmed"
    redirect_to action: :index
  end

  def destroy
    @office.destroy!
    flash[:notice] = "Office deleted"
    redirect_to action: :index
  end

  private

  def update_params
    params.require(:organization).permit(:confirmed)
  end

end