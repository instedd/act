class OrganizationsController < AuthenticatedController

  load_and_authorize_resource

  def index
  end

  def new
  end

  def create
    if @organization.save
      redirect_to organizations_path
    else
      #TODO
    end
  end

  private

  def create_params
    params.require(:organization).permit(:name)
  end

end