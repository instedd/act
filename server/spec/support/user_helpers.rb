module UserHelpers

  def sign_in_user
    sign_in FactoryGirl.create :user
  end

end