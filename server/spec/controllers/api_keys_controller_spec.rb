require 'rails_helper'

describe ApiKeysController, type: :controller do

  context "as admin" do

    before(:each) { sign_in_admin }

    it "allows to create new api keys" do
      expect{
        post :create
      }.to change(ApiKey, :count).by(1)
      expect(response).to be_redirect
    end

    it "allows to delete api keys" do
      key = ApiKey.create!
      expect {
        delete :destroy, id: key.id
      }.to change(ApiKey, :count).by(-1)
      expect(response).to be_redirect
    end

  end

  context "as organization user" do

    before(:each) { sign_in_user }

    it "does not allow to list api keys" do
      get :index
      expect(response).to be_unauthorized
    end

    it "does not allow to create api keys" do
      post :create
      expect(response).to be_unauthorized
    end

  end

end