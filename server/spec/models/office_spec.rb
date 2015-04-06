require 'rails_helper'

describe Office do

  describe "creation" do

    let(:minimum_attributes) do
      attrs = [:reported_organization_name, :reported_location_code, :supervisor_name, :supervisor_phone_number, :public_key]
      sliced = FactoryGirl.attributes_for(:office).slice(*attrs)
      location = FactoryGirl.create :location, geo_id: sliced[:reported_location_code]
      sliced[:location] = location
      sliced
    end

    it "validates presence of required fields" do
      minimum_attributes.keys.each do |attribute_name|
        invalid_attributes = minimum_attributes.dup
        invalid_attributes.delete attribute_name
        expect {
          Office.create! invalid_attributes
        }.to raise_error
      end
    end

    it "can be created with only required fields" do
      expect { Office.create! minimum_attributes }.to change(Office, :count).by(1)
    end

    it "is saved with generated GUID" do
      office = Office.create! minimum_attributes
      expect(office.guid).not_to be_blank
    end

    it "is not confirmed when just created" do
      office = Office.create! minimum_attributes
      expect(office).not_to be_confirmed
    end

    it "is not allowed for public key authentication when just created" do
      office = Office.create! minimum_attributes
      expect(office).not_to be_public_key_allowed
    end

  end

end