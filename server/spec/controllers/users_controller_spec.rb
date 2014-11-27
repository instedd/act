require 'rails_helper'

describe UsersController, type: :controller do

  it "allows admins to list users" do
    sign_in_admin
    get :index
    expect(response).to be_successful
  end

  it "does not allow organization users to list users" do
    sign_in_user
    get :index
    expect(response).to be_unauthorized
  end

end