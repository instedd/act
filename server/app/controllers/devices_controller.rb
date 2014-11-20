class DevicesController < ApplicationController

  def index
    @devices = Device.where(confirmed: false)
  end

  def update
    device = Device.find_by_id(params[:id])
    device.confirmed = true
    device.save
    
    redirect_to action: :index
  end

end