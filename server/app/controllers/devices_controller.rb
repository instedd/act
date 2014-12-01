class DevicesController < AuthenticatedController

  load_and_authorize_resource

  def index
    confirmed_devices = @devices.where(confirmed: true)
    pending_devices   = @devices.where(confirmed: false)

    @view_pending = current_user.admin? && (params[:pending] || (pending_devices.any? && confirmed_devices.empty?))

    if @view_pending
      @confirmed_devices_count = confirmed_devices.count
      @devices = pending_devices
    else
      @pending_devices_count = pending_devices.count
      @devices = confirmed_devices
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

  def destroy
    @device.destroy!
    flash[:notice] = "Device deleted"
    redirect_to action: :index
  end

  private

  def update_params
    params.require(:organization).permit(:confirmed)
  end

end