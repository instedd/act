class DevicesController < AuthenticatedController

  def index
    @devices = Device.where(confirmed: false)
  end

  def update
    return render nothing: true, status: 400 unless params[:confirmed]

    device = Device.find_by_id(params[:id])
    device.confirmed = true
    device.save
    
    redirect_to action: :index
  end

end