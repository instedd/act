require 'rails_helper'

describe Office do

  describe "creation from synchronized json content" do
    let(:office) {
      FactoryGirl.create :office, guid: "GUID",\
                                  reported_organization_name: "instedd",\
                                  reported_location_code: 123,\
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
       report_time: "2015-03-31T19:04:19.750Z",
       note: "Nothing in particular.",
     }
    end

    it "creates new cases" do
     Case.save_from_sync_file(office.guid, sample_case_info.to_json)
     
     expect(Case.count).to eq(1)
     c = Case.first
     
     expect(c.office).to eq(office)
     expect(c.guid).to eq(sample_case_info[:guid])
     expect(c.patient_name).to eq(sample_case_info[:name])
     expect(c.patient_phone_number).to eq(sample_case_info[:phone_number])
     expect(c.patient_age).to eq(sample_case_info[:age].to_i)
     expect(c.patient_gender).to eq(sample_case_info[:gender])
     expect(c.dialect_code).to eq(sample_case_info[:dialect_code])
     expect(c.symptoms).to eq(sample_case_info[:symptoms].map(&:to_s))
     expect(c.note).to eq(sample_case_info[:note])
    end

    it "fails if office doesn't exist" do
     expect { Case.save_from_sync_file("inexistent_office", sample_case_info.to_json) }.to raise_error
    end
  end

  describe "sick status updates" do

    it "updates sick flag when confirmed sick" do
      c = FactoryGirl.create :case
      
      CallRecord.create! _case: c, sick: true, symptoms: {}

      c.reload

      expect(c.sick).to be(true)
    end

    it "creates a notification when confirmed sick" do
      c = FactoryGirl.create :case

      expect {
        CallRecord.create! _case: c, sick: true, symptoms: {}
      }.to change(Notification, :count).by(1)

      notification = Notification.last
      expect(notification.notification_type).to eq("case_confirmed_sick")

      expect(notification.metadata["id"].to_i).to eq(c.id)
      expect(notification.metadata["patient_name"]).to eq(c.patient_name)
      expect(notification.metadata["patient_phone_number"]).to eq(c.patient_phone_number)
      expect(notification.metadata["symptoms"]).to eq(c.symptoms.to_json)
      expect(notification.metadata["supervisor_name"]).to eq(c.supervisor_name)
      expect(notification.metadata["supervisor_phone_number"]).to eq(c.supervisor_phone_number)
      expect(notification.metadata["dialect_code"]).to eq(c.dialect_code)
    end

    it "does not create a notification if previously confirmed sick" do
      _case = FactoryGirl.create :case
      FactoryGirl.create :call_sick, _case: _case

      _case.reload

      expect { CallRecord.create! _case: _case, sick: true, symptoms: {} }.not_to change(Notification, :count)
    end

  end


end