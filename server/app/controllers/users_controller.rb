class UsersController < AuthenticatedController

  load_and_authorize_resource

  def index
    @users = @users.order("email ASC")
  end

end