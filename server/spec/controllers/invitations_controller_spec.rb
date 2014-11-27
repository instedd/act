require 'rails_helper'

describe Users::InvitationsController, type: :controller do

  before(:each) { @request.env["devise.mapping"] = Devise.mappings[:user] }

  context "as admin" do

    before(:each) { sign_in_admin }

    it "allows access to new invitation form" do
      get :new
      expect(response).to be_successful
    end

  end

end