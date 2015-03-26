class CasesController < AuthenticatedController

  load_and_authorize_resource

  def index
    @cases = @cases.order("updated_at DESC")
  end

  def show
  end

  private

  def update_params
    params.require(:organization).permit(:confirmed)
  end

end
