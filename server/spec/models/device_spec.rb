require 'rails_helper'

describe Device do

  describe "creation" do

    let(:minimum_attributes) {
      {
        public_key: "PK123",
        organization_name: "instedd",
        location_id: 123,
        supervisor_name: "John Doe",
        supervisor_phone_number: "123"
      }
    }

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
  end



end