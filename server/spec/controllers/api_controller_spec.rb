require 'rails_helper'

describe ApiController, type: :controller do
  
  let(:device) { Device.create! guid: "DEVICE_123", organization_name: "Instedd", location_id: 1 }

  it "returns empty json list when there are no cases" do
    xhr :get, :cases

    expect(response).to be_successful
    expect(JSON.parse(response.body)).to eq([])
  end

  it "returns all available events if no since_date is specified" do    
    device.cases.create! sample_params

    xhr :get, :cases

    expect(response).to be_successful
    expect(json_response.size).to eq(1)

    case_json = json_response[0]
    expect(case_json.keys).to match_array(sample_params.keys + ["timestamp"])
    expect(case_json["timestamp"]).to be_present
    sample_params.each { |k, v| expect(case_json[k]).to eq(v) }
  end

  it "returns only events strictly after specified date" do
    Timecop.freeze Time.new(2000, 01, 1, 0, 0, 0, '+00:00').utc
    device.cases.create! sample_params({guid: "CASE1"})
    
    Timecop.freeze Time.new(2000, 01, 1, 12, 0, 0, '+00:00').utc
    device.cases.create! sample_params({guid: "CASE2"})

    Timecop.freeze Time.new(2000, 01, 2, 0, 0, 0, '+00:00').utc
    device.cases.create! sample_params({guid: "CASE3"})

    Timecop.freeze Time.new(2000, 01, 3, 0, 0, 0, '+00:00').utc
    device.cases.create! sample_params({guid: "CASE4"})

    xhr :get, :cases, { since_date: Time.new(2000, 01, 1, 0, 0, 0, '+00:00').utc }

    expect(json_response.map { |c| c["guid"] }).to eq(["CASE2", "CASE3", "CASE4"])
  end

  it "returns dates in the same format used for querying" do
    Timecop.freeze Time.new(2000, 01, 1, 0, 0, 0, '+00:00').utc
    device.cases.create! sample_params({guid: "CASE1"})

    xhr :get, :cases
    max_date = json_response[0]["timestamp"]

    Timecop.freeze Time.new(2000, 01, 2, 0, 0, 0, '+00:00').utc
    device.cases.create! sample_params({guid: "CASE2"})

    xhr :get, :cases, { since_date: max_date }
    expect(json_response.map { |c| c["guid"] }).to eq(["CASE2"])
  end

  def json_response
    JSON.parse(response.body)
  end

  def sample_params(overrides = {})
   {
     "guid" => "CASE123",
     "patient_name" => "John Doe",
     "patient_phone_number" => "1111111",
     "patient_age" => 21,
     "patient_gender" => "M",
     "dialect_code" => "D123",
     "symptoms" => [ "fever", "vomiting" ],
     "note" => "Nothing in particular.",
   }.merge(overrides) 
  end

end