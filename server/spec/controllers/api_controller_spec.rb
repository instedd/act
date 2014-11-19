require 'rails_helper'

describe ApiController, type: :controller do
  
  let(:device) { Device.create! guid: "GUID",\
                                public_key: "PK123",\
                                organization_name: "instedd",\
                                location_id: 123,\
                                supervisor_name: "John Doe",\
                                supervisor_phone_number: "123"
  }

  describe "device registration" do
    
    it "creates unconfirmed device using suplied information" do
      params = {
         publicKey: "PK123",
         deviceInfo: {
           organization: "instedd",
           location: 111,
           supervisorNumber: "222",
           supervisorName: "John Doe"
         }
       }

      expect { xhr :post, :register, params }.to change(Device, :count).by(1)
      expect(response).to be_successful
      expect(Device.first).not_to be_confirmed
    end

  end

  describe "listing cases" do

    it "returns empty json list when there are no cases" do
      xhr :get, :cases

      expect(response).to be_successful
      expect(JSON.parse(response.body)).to eq([])
    end

    it "returns all available events if no since_id is specified" do    
      device.cases.create! sample_params

      xhr :get, :cases

      expect(response).to be_successful
      expect(json_response.size).to eq(1)

      case_json = json_response[0]
      expect(case_json.keys).to match_array(sample_params.keys + ["id"])
      expect(case_json["id"]).to eq(device.cases.first.id)
      sample_params.each { |k, v| expect(case_json[k]).to eq(v) }
    end

    it "returns only events strictly after specified id" do
      device.cases.create! sample_params({guid: "CASE1"})
      since_id = device.cases.create!(sample_params({guid: "CASE2"})).id
      device.cases.create! sample_params({guid: "CASE3"})

      xhr :get, :cases, { since_id: since_id }

      expect(json_response.map { |c| c["guid"] }).to eq(["CASE3"])
    end

  end

  describe "update case with call follow up information" do

    before(:each) { device.cases.create! sample_params({guid: "CASE1"}) }
    let(:case_id)  { device.cases.first.id }

    it "fails if case does no exist" do
      xhr :put, :update_case, id: "NONEXISTENT"
      expect(response).to be_client_error
    end

    it "fails if is_sick parameter is not defined" do
      xhr :put, :update_case, id: case_id
      expect(response).to be_client_error
      expect(Case.find_by_guid("CASE1").sick).to be_nil
    end

    it "updates updates case sick status" do
      expect(Device).to receive(:sync_sick_status).with(device.guid, "CASE1", true)

      xhr :put, :update_case, id: case_id, sick: true
      expect(response).to be_successful
      expect(Case.find_by_guid("CASE1")).to be_sick
    end

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