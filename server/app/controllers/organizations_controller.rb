class OrganizationsController < AuthenticatedController

  load_and_authorize_resource

  def index
  end

  def new
  end

  def create
    if @organization.save
      flash[:notice] = "#{@organization.name} organization created"
      redirect_to organizations_path
    else
      #TODO
    end
  end

  def destroy
    @organization.destroy.destroy
    flash[:notice] = "#{@organization.name} organization deleted"
    redirect_to action: :index
  end

  private

  def create_params
    params.require(:organization).permit(:name)
  end

end