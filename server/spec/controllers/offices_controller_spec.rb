require 'rails_helper'

describe OfficesController, type: :controller do

  let(:organization) { FactoryGirl.create :organization }
  let(:office)       { FactoryGirl.create :non_approved_office }

  describe "confirming offices" do

    before(:each) { sign_in_admin }

    it "sets office organization when confirmed" do
      put :update, id: office.id, office: { confirmed: true, organization_id: organization.id }

      expect(office.reload).to be_confirmed
      expect(office.organization).to eq(organization)
    end

    it "doesn't allow to un-confirm offices" do
      office.update_attributes(confirmed: true)
      put :update, id: office.id, office: { confirmed: false, organization_id: organization.id }

      expect(response).not_to be_successful      
      expect(office.reload).to be_confirmed
    end

    it "doesn't update other attributes" do
      expect {
        put :update, id: office.id, office: { confirmed: true, organization_id: organization.id, supervisor_name: "OTHER_NAME" }
      }.not_to change { office.reload.supervisor_name }
      
      expect(office.reload).to be_confirmed
    end

    it "deletes unconfirmed offices on destroy action" do
      office.save
      expect {
        delete :destroy, id: office.id
      }.to change(Office, :count).by(-1)
    end

    it "fails to delete confirmed offices on destroy action" do
      unconfirmed_office = FactoryGirl.create :approved_office
      expect {
        delete :destroy, id: unconfirmed_office.id
      }.not_to change(Office, :count)
      expect(response).to be_unauthorized
    end

  end

  describe "authorization" do

    context "as admin" do

      before(:each) { sign_in_admin }

      it "allows access to unconfirmed offices" do
        get :index
        expect(response).to be_successful
      end

      it "allows confirming offices" do
        put :update, id: office.id, office: { confirmed: true, organization_id: organization.id }
        expect(response).not_to be_unauthorized
      end

    end

    context "as organization user" do

     let(:user) { FactoryGirl.create :user }
     before(:each) { sign_in user }
     let(:approved_office_same_organization)  { FactoryGirl.create :approved_office, organization: user.organization }
     let(:approved_office_other_organization) { FactoryGirl.create :approved_office }

     # at the moment accessing the office index means the user
     # is able to confirm offices.
     it "allows to list confirmed offices for his organization" do
       get :index
       expect(response).to be_successful
       expect(assigns(:offices)).to eq([approved_office_same_organization])
     end

     it "does not allow confirming offices" do
       put :update, id: office.id, office: { confirmed: true, organization_id: organization.id }
       expect(response).to be_unauthorized
     end

     it "does not allow deleting offices" do
       delete :destroy, id: office.id
       expect(response).to be_unauthorized
     end

    end

  end

end