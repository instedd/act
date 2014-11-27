class DevicesController < AuthenticatedController

  load_and_authorize_resource

  def index
    unless current_user.can? :approve, Device
      raise CanCan::AccessDenied.new("Not authorized", :create, :invitations)
    end
    
    @devices = @devices.where(confirmed: false)
  end

  def update
    return render nothing: true, status: 400 unless params[:confirmed]

    device = Device.find_by_id(params[:id])
    device.confirmed = true
    device.save
    
    redirect_to action: :index
  end

  private

  def update_params
    params.require(:organization).permit(:confirmed)
  end

end