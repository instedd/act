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

    it "allows to delete organizations" do
      organization = FactoryGirl.create :organization
      expect {
        delete :destroy, id: organization.id
      }.to change(Organization, :count).by(-1)
      expect(response).not_to be_unauthorized
    end

  end

  context "as organization user" do

    before(:each) { sign_in_user }

    it "does not allow to list organizations" do
      get :index
      expect(response).to be_unauthorized
    end

    it "does not allow to access new organization form" do
      get :new
      expect(response).to be_unauthorized
    end

    it "does not allow to create organizations" do
      post :create, organization: FactoryGirl.attributes_for(:organization)
      expect(response).to be_unauthorized
    end

    it "does not allow to delete organizations" do
      organization = FactoryGirl.create :organization
      expect {
        delete :destroy, id: organization.id
      }.not_to change(Organization, :count)
      expect(response).to be_unauthorized
    end

  end

end