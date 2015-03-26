class ApiKeysController < AuthenticatedController

  load_and_authorize_resource

  def index
    @api_keys = ApiKey.order created_at: :desc
  end

  def edit
  end

  def update
    label_params = params.require(:api_key).permit(:label)
    @api_key.update! label_params
    flash[:notice] = "API key renamed successfully"
    redirect_to action: :index
  end

  def create
    ApiKey.create!
    flash[:notice] = "API key successfully created"
    redirect_to action: :index
  end

  def destroy
    @api_key.destroy!
    flash[:notice] = "API key deleted"
    redirect_to action: :index
  end

end