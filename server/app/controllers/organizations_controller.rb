class OrganizationsController < AuthenticatedController

  def index
    @organizations = Organization.all
  end

  def new
    @organization = Organization.new
  end

  def create
    if @organization = Organization.create(organization_params)
      redirect_to organizations_path
    else
      #TODO
    end
  end

  private

  def organization_params
    params.require(:organization).permit(:name)
  end

end