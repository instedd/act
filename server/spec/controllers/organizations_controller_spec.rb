require 'rails_helper'

describe OrganizationsController, type: :controller do

  let(:manas) { FactoryGirl.create :organization, name: "Manas" }
  let(:instedd) { FactoryGirl.create :organization, name: "Instedd" }

  context "as admin" do

    before(:each) { sign_in_admin }

    it "allows listing organizations" do
      get :index
      expect(response).to be_successful
    end

    it "lists all organizations" do
      get :index
      expect(assigns(:organizations)).to eq([manas, instedd])
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

    it "allows to list organizations" do
      get :index
      expect(response).to be_successful
    end

    it "lists only the user's organization" do
      get :index
      expect(assigns(:organizations)).to eq([subject.current_user.organization])
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
