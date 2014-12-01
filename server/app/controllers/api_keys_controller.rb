class ApiKeysController < AuthenticatedController

  load_and_authorize_resource

  def index
    @api_keys = ApiKey.all
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