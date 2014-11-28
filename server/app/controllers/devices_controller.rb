class DevicesController < AuthenticatedController

  load_and_authorize_resource

  def index
    @devices = @devices.includes(:organization)
    if current_user.can? :approve, Device
      @confirmed_devices = @devices.where(confirmed: true)
      @pending_devices   = @devices.where(confirmed: false)
    else
      @confirmed_devices = @devices
      @pending_devices   = []
    end
    
  end

  def update
    organization = Organization.find_by_id(params[:device][:organization_id]) rescue nil
    confirmed = params[:device][:confirmed]
    unless organization and confirmed
      return render nothing: true, status: 400
    end

    device = Device.find_by_id(params[:id])
    device.confirmed = true
    device.organization = organization
    device.save
    
    flash[:notice] = "Device confirmed"
    redirect_to action: :index
  end

  private

  def update_params
    params.require(:organization).permit(:confirmed)
  end

end