require 'rails_helper'

describe DevicesController, type: :controller do

  describe "confirming devices" do

    let(:device) { FactoryGirl.create :device, confirmed: false }

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

end