class DevicesController < AuthenticatedController

  load_and_authorize_resource

  def index
    if current_user.can? :approve, Device
      @confirmed_devices = @devices.where(confirmed: true)
      @pending_devices   = @devices.where(confirmed: false)
    else
      @confirmed_devices = @devices
      @pending_devices   = []
    end
    
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