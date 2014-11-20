require 'rails_helper'

describe Device do

  describe "creation from synchronized json content" do
    let(:device) {
      FactoryGirl.create :device, guid: "GUID",\
                                  organization_name: "instedd",\
                                  location_id: 123,\
                                  supervisor_name: "John Doe",\
                                  supervisor_phone_number: "123"
    }

    let(:sample_case_info) do
     {
       guid: "CASE123",
       name: "John Doe",
       phone_number: "1111111",
       age: "21",
       gender: "M",
       dialect_code: "D123",
       symptoms: [ :fever, :vomiting ],
       note: "Nothing in particular.",
     }
    end

    it "creates new cases" do
     Case.save_from_sync_file(device.guid, sample_case_info.to_json)
     
     expect(Case.count).to eq(1)
     c = Case.first
     
     expect(c.device).to eq(device)
     expect(c.guid).to eq(sample_case_info[:guid])
     expect(c.patient_name).to eq(sample_case_info[:name])
     expect(c.patient_phone_number).to eq(sample_case_info[:phone_number])
     expect(c.patient_age).to eq(sample_case_info[:age].to_i)
     expect(c.patient_gender).to eq(sample_case_info[:gender])
     expect(c.dialect_code).to eq(sample_case_info[:dialect_code])
     expect(c.symptoms).to eq(sample_case_info[:symptoms].map(&:to_s))
     expect(c.note).to eq(sample_case_info[:note])
    end

    it "fails if device doesn't exist" do
     expect { Case.save_from_sync_file("inexistent_device", sample_case_info.to_json) }.to raise_error
    end
  end

end