require 'rails_helper'

describe Device do

  describe "creation" do

    let(:minimum_attributes) do
      attrs = [:reported_organization_name, :location_id, :supervisor_name, :supervisor_phone_number, :public_key]
      FactoryGirl.attributes_for(:device).slice(*attrs)
    end

    it "validates presence of required fields" do
      minimum_attributes.keys.each do |attribute_name|
        invalid_attributes = minimum_attributes.dup
        invalid_attributes.delete attribute_name
        expect {
          Device.create! invalid_attributes
        }.to raise_error
      end
    end

    it "can be created with only required fields" do
      expect { Device.create! minimum_attributes }.to change(Device, :count).by(1)
    end

    it "is saved with generated GUID" do
      device = Device.create! minimum_attributes
      expect(device.guid).not_to be_blank
    end

    it "is not confirmed when just created" do
      device = Device.create! minimum_attributes
      expect(device).not_to be_confirmed
    end

    it "is not allowed for public key authentication when just created" do
      device = Device.create! minimum_attributes
      expect(device).not_to be_public_key_allowed
    end

  end

end