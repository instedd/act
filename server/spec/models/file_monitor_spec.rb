require 'rails_helper'

describe "creation of entities based on sync'ed files" do

  let(:sample_case_info) do
    {
      name: "John Doe",
      phone_number: "1111111",
      age: "21",
      gender: "M",
      dialect_code: "D123",
      symptoms: [ :fever, :vomiting ],
      note: "Nothing in particular.",
    }
  end

  it "creates new devices" do
    Device.save_from_sync_file("d1", '{"organization":"instedd", "location":2597326}')

    expect(Device.count).to eq(1)

    device = Device.first
    expect(device.guid).to eq("d1")
    expect(device.organization_name).to eq("instedd")
    expect(device.location_id).to eq(2597326)
  end

  it "creates new cases" do
    device = Device.create! guid: "d1", organization_name: "instedd", location_id: 2597326

    Case.save_from_sync_file("d1", sample_case_info.to_json)
    
    expect(Case.count).to eq(1)
    c = Case.first
    
    expect(c.device).to eq(device)
    expect(c.patient_name).to eq(sample_case_info[:name])
    expect(c.patient_phone_number).to eq(sample_case_info[:phone_number])
    expect(c.patient_age).to eq(sample_case_info[:age].to_i)
    expect(c.patient_gender).to eq(sample_case_info[:gender])
    expect(c.dialect_code).to eq(sample_case_info[:dialect_code])
    expect(c.symptoms).to eq(sample_case_info[:symptoms].map(&:to_s))
    expect(c.note).to eq(sample_case_info[:note])
  end

  it "supports case files arriving before device files" do
    Case.save_from_sync_file("d1", sample_case_info.to_json)

    device = Device.find_by_guid("d1")
    expect(device).to be_present
    expect(device.cases.size).to eq(1)

    Device.save_from_sync_file("d1", '{"organization":"instedd", "location":2597326}')

    device.reload
    
    expect(device.guid).to eq("d1")
    expect(device.organization_name).to eq("instedd")
    expect(device.location_id).to eq(2597326)
  end

end