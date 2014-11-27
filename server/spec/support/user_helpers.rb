module UserHelpers

  def sign_in_user
    sign_in FactoryGirl.create :user
  end

  def sign_in_admin
    sign_in FactoryGirl.create :admin_user
  end

end