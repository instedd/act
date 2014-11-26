class UsersController < AuthenticatedController

  def index
    @users = User.order("email ASC")
  end

end