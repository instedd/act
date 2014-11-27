require 'rails_helper'

describe OrganizationsController, type: :controller do

  context "as admin" do

    before(:each) { sign_in_admin }

    it "allows listing organizations" do
      get :index
      expect(response).to be_successful
    end

    it "allows access to new organization form" do
      get :new
      expect(response).to be_successful
    end

    it "allows to create organizations" do
      expect {
        post :create, organization: FactoryGirl.attributes_for(:organization)
      }.to change(Organization, :count).by(1)
      expect(response).not_to be_unauthorized
    end

  end

end