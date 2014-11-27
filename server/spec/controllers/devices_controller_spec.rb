require 'rails_helper'

describe DevicesController, type: :controller do

  describe "confirming devices" do

    before(:each) { sign_in_admin }
    let(:device)  { FactoryGirl.create :non_approved_device }

    it "allows ssh access when device is confirmed" do
      put :update, id: device.id, confirmed: true

      expect(device.reload).to be_confirmed
    end

    it "doesn't allow to un-confirm devices" do
      device.update_attributes(confirmed: true)
      put :update, id: device.id, confirmed: false

      expect(response).not_to be_successful      
      expect(device.reload).to be_confirmed
    end

    it "doesn't update other attributes" do
      expect {
        put :update, id: device.id, confirmed: true, supervisor_name: "OTHER_NAME"
      }.not_to change { device.reload.supervisor_name }
      
      expect(device.reload).to be_confirmed
    end

  end

  describe "authorization" do

    let(:non_approved_device) { FactoryGirl.create :non_approved_device }

    context "as admin" do

      before(:each) { sign_in_admin }

      it "allows access to unconfirmed devices" do
        get :index
        expect(response).to be_successful
      end

      it "allows confirming devices" do
        put :update, id: non_approved_device.id, confirmed: true
        expect(response).not_to be_unauthorized
      end

    end

    context "as organization user" do

     let(:user) { FactoryGirl.create :user }
     before(:each) { sign_in user }
     let(:approved_device_same_organization)  { FactoryGirl.create :approved_device, organization: user.organization }
     let(:approved_device_other_organization) { FactoryGirl.create :approved_device }

     # at the moment accessing the device index means the user
     # is able to confirm devices.
     it "allows to list confirmed devices for his organization" do
       get :index
       expect(response).to be_successful
       expect(assigns(:pending_devices)).to be_empty
       expect(assigns(:confirmed_devices)).to eq([approved_device_same_organization])
     end

     it "does not allow confirming devices" do
       put :update, id: non_approved_device.id, confirmed: true
       expect(response).to be_unauthorized
     end

    end

  end

end