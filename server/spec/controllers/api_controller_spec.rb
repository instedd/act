require 'rails_helper'

describe ApiController, type: :controller do

  let(:office) do
    FactoryGirl.create :office, reported_organization_name: "instedd",\
                                reported_location_code: "123",\
                                supervisor_name: "John Doe",\
                                supervisor_phone_number: "123"
  end

  let!(:access_token) { ApiKey.create!.access_token }

  describe "office registration" do
    
    let(:valid_key) { FactoryGirl.attributes_for(:office)[:public_key] }

    let(:location) { FactoryGirl.create :location, geo_id: "11_1"}

    let(:params) do
      {
        publicKey: valid_key,
        deviceInfo: {
          organization: "instedd",
          location: "11_1",
          supervisorNumber: "222",
          supervisorName: "John Doe"
        },
        apiVersion: 2
      }
    end

    it "creates unconfirmed office using suplied information" do
      location.save!
      expect(Office).to receive(:init_sync_path).with(anything)

      expect { xhr :post, :register, params }.to change(Office, :count).by(1)
      expect(response).to be_successful
      expect(Office.first).not_to be_confirmed
    end

    it "rejects invalid public keys" do
      expect(Office).not_to receive(:init_sync_path)
      
      params[:publicKey] = "#{valid_key}\n#{valid_key}"
      xhr :post, :register, params

      expect(response).not_to be_successful
    end

    it "accepts trailing newline in public key" do
      location.save!
      expect(Office).to receive(:init_sync_path)
      
      params[:publicKey] = "#{valid_key}\n"
      xhr :post, :register, params

      expect(response).to be_successful
    end

  end

  describe "listing cases" do

    context "without acces token header" do
      
      it "refuses access" do
        xhr :get, :cases
        expect(response.code.to_i).to eq(401)
      end

    end

    context "with access token header" do
  
      before(:each) { add_access_token_header }

      it "returns empty json list when there are no cases" do
        xhr :get, :cases

        expect(response).to be_successful
        expect(JSON.parse(response.body)).to eq([])
      end

      it "returns all available events if no since_id is specified" do    
        office.cases.create! sample_params

        xhr :get, :cases

        expect(response).to be_successful
        expect(json_response.size).to eq(1)

        case_json = json_response[0]
        expect(case_json.keys).to match_array(sample_params.keys + ["id"])
        expect(case_json["id"]).to eq(office.cases.first.id)
        sample_params.each { |k, v| expect(case_json[k]).to eq(v) }
      end

      it "returns only events strictly after specified id" do
        office.cases.create! sample_params({guid: "CASE1"})
        since_id = office.cases.create!(sample_params({guid: "CASE2"})).id
        office.cases.create! sample_params({guid: "CASE3"})

        xhr :get, :cases, { since_id: since_id }

        expect(json_response.map { |c| c["guid"] }).to eq(["CASE3"])
      end
    end

  end

  describe "update case with call follow up information" do

    before(:each) { office.cases.create! sample_params({guid: "CASE1"}) }
    let(:case_id)  { office.cases.first.id }

    context "without access token header" do
  
      it "refuses access" do
        xhr :put, :update_case, id: case_id
        expect(response.code.to_i).to eq(401)
      end
  
    end

    context "with access token header" do
      
      before(:each) { add_access_token_header }

      it "fails if case does no exist" do
        xhr :put, :update_case, id: "NONEXISTENT"
        expect(response).to be_client_error
      end

      it "fails if is_sick parameter is not defined" do
        xhr :put, :update_case, id: case_id
        expect(response).to be_client_error
        expect(Case.find_by_guid("CASE1").sick).to be_nil
      end

      it "updates updates case sick status (when user declares feeling sick)" do
        expect(Office).to receive(:sync_sick_status).with(office.guid, "CASE1", true)

        xhr :put, :update_case, id: case_id, sick: ApiController::AFFIRMATIVE_ANSWER_CODE
        expect(response).to be_successful
        expect(Case.find_by_guid("CASE1")).to be_sick
      end

      it "updates updates case sick status (when user declares not feeling sick)" do
        expect(Office).to receive(:sync_sick_status).with(office.guid, "CASE1", false)

        xhr :put, :update_case, id: case_id, sick: ApiController::NEGATIVE_ANSWER_CODE
        expect(response).to be_successful
        expect(Case.find_by_guid("CASE1")).not_to be_sick
      end

      it "creates notifications when case is confirmed sick" do
        expect(Office).to receive(:sync_sick_status)
        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::AFFIRMATIVE_ANSWER_CODE
        }.to change(Notification, :count).by(1)
      end

      it "does not create a notification if case was already confirmed sick" do
        expect(Office).to receive(:sync_sick_status)
        
        CallRecord.create! _case: office.cases.first, sick: true, family_sick: false, community_sick: false, symptoms: {}

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::AFFIRMATIVE_ANSWER_CODE
        }.not_to change(Notification, :count)
      end

      it "records a family member has fever, no rash" do
        expect(Office).to receive(:sync_sick_status)

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::NEGATIVE_ANSWER_CODE, family_sick: ApiController::AFFIRMATIVE_ANSWER_CODE, fever_family: ApiController::AFFIRMATIVE_ANSWER_CODE, rash_family: ApiController::NEGATIVE_ANSWER_CODE
        }.not_to change(Notification, :count)

        _case = Case.find_by_guid("CASE1")

        expect(_case.call_records.last.sick).to be(false)
        expect(_case.call_records.last.family_sick).to be(true)
        expect(_case.call_records.last.symptoms["fever_family"]).to be(true)
        expect(_case.call_records.last.symptoms["rash_family"]).to be(false)
      end

      it "generates a report from multilpe calls" do
        expect(Office).to receive(:sync_sick_status).twice

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::NEGATIVE_ANSWER_CODE, family_sick: ApiController::AFFIRMATIVE_ANSWER_CODE, fever_family: ApiController::AFFIRMATIVE_ANSWER_CODE, rash_family: ApiController::NEGATIVE_ANSWER_CODE

          xhr :put, :update_case, id: case_id, sick: ApiController::NEGATIVE_ANSWER_CODE, family_sick: ApiController::NEGATIVE_ANSWER_CODE, community_sick: ApiController::AFFIRMATIVE_ANSWER_CODE, fever_community: ApiController::AFFIRMATIVE_ANSWER_CODE, rash_community: ApiController::NEGATIVE_ANSWER_CODE
        }.not_to change(Notification, :count)

        _case = Case.find_by_guid("CASE1")

        expect(_case.calls_report[:sick]).to be(false)
        expect(_case.calls_report[:family_sick]).to be(true)
        expect(_case.calls_report[:community_sick]).to be(true)
        expect(_case.calls_report[:symptoms]).not_to include("individual_fever")
        expect(_case.calls_report[:symptoms]).to include("fever_family")
        expect(_case.calls_report[:symptoms]).to include("fever_community")
        expect(_case.calls_report[:symptoms]).not_to include("rash_individual")
        expect(_case.calls_report[:symptoms]).not_to include("rash_family")
        expect(_case.calls_report[:symptoms]).not_to include("rash_community")
      end

      it "records a call as failed with error" do
        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::AFFIRMATIVE_ANSWER_CODE, family_sick: ApiController::NEGATIVE_ANSWER_CODE, fever_family: ApiController::NEGATIVE_ANSWER_CODE, rash_individual: ApiController::AFFIRMATIVE_ANSWER_CODE, call_status: 'failed (busy)'
        }.not_to change(Notification, :count)

        _case = Case.find_by_guid("CASE1")

        expect(_case.call_records.count).to eq(1)
        expect(_case.calls_report[:sick]).to be_nil
        expect(_case.calls_report[:family_sick]).to be_nil
        expect(_case.calls_report[:community_sick]).to be_nil
        expect(_case.calls_report[:anyone_sick]).to be_nil
        expect(_case.calls_report[:who_is_sick]).to eq('Not contacted yet')
        expect(_case.calls_report[:symptoms]).to be_empty
      end

      it "records a successful call" do
        expect(Office).to receive(:sync_sick_status)

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::AFFIRMATIVE_ANSWER_CODE, family_sick: ApiController::NEGATIVE_ANSWER_CODE, fever_family: ApiController::NEGATIVE_ANSWER_CODE, rash_individual: ApiController::AFFIRMATIVE_ANSWER_CODE, call_status: 'completed'
        }.to change(Notification, :count).by(1)

        _case = Case.find_by_guid("CASE1")

        expect(_case.call_records.count).to eq(1)
        expect(_case.calls_report[:sick]).to be_truthy
        expect(_case.calls_report[:family_sick]).to be_falsy
        expect(_case.calls_report[:community_sick]).to be_falsy
        expect(_case.calls_report[:anyone_sick]).to be_truthy
        expect(_case.calls_report[:who_is_sick]).to eq('Patient sick')
        expect(_case.calls_report[:symptoms].count).to eq(1)
        expect(_case.calls_report[:symptoms]).to include('rash_individual')
      end

      it "takes successful calls into account for a call report" do
        expect(Office).to receive(:sync_sick_status).twice

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::AFFIRMATIVE_ANSWER_CODE, family_sick: ApiController::NEGATIVE_ANSWER_CODE, fever_family: ApiController::NEGATIVE_ANSWER_CODE, rash_individual: ApiController::AFFIRMATIVE_ANSWER_CODE, call_status: 'completed'
        }.to change(Notification, :count).by(1)

        _case = Case.find_by_guid("CASE1")

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::NEGATIVE_ANSWER_CODE, family_sick: ApiController::AFFIRMATIVE_ANSWER_CODE, fever_family: ApiController::NEGATIVE_ANSWER_CODE, rash_family: ApiController::AFFIRMATIVE_ANSWER_CODE, call_status: 'completed'

          _case.reload
        }.to change(_case, :calls_report)
      end

      it "doesn't take into account failed calls for a call report" do
        expect(Office).to receive(:sync_sick_status)

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::AFFIRMATIVE_ANSWER_CODE, family_sick: ApiController::NEGATIVE_ANSWER_CODE, fever_family: ApiController::NEGATIVE_ANSWER_CODE, rash_individual: ApiController::AFFIRMATIVE_ANSWER_CODE, call_status: 'completed'
        }.to change(Notification, :count).by(1)

        _case = Case.find_by_guid("CASE1")

        expect {
          xhr :put, :update_case, id: case_id, sick: ApiController::NEGATIVE_ANSWER_CODE, family_sick: ApiController::AFFIRMATIVE_ANSWER_CODE, fever_family: ApiController::NEGATIVE_ANSWER_CODE, rash_family: ApiController::AFFIRMATIVE_ANSWER_CODE, call_status: 'failed (busy)'

          _case.reload
        }.not_to change(_case, :calls_report)
      end

    end


  end


  describe "listing notifications" do

    context "without access token header" do
      it "refuses access without access token header" do
        xhr :get, :notifications
        expect(response.code.to_i).to eq(401)
      end
    end

    context "with access token header" do

      before(:each) { add_access_token_header }

      it "returns no notifications if there are no notifications" do
        xhr :get, :notifications
        expect(response).to be_successful
        expect(json_response).to eq([])
      end

      it "returns all notifications if there is no since_id specified" do
        n1 = FactoryGirl.create :notification, metadata: { "k1" => "v1" }
        n2 = FactoryGirl.create :notification, metadata: { "k2" => "v2" }
        expected_response = [n1, n2].map do |n| 
          { "id" => n.id, "notification_type" => n.notification_type.to_s, "metadata" => n.metadata }
        end
        
        xhr :get, :notifications
        
        expect(response).to be_successful
        expect(json_response).to eq(expected_response)
      end

      it "returns only notifications after specified since_id" do
        3.times { FactoryGirl.create :notification }
        first_notificaton = Notification.first

        xhr :get, :notifications, since_id: first_notificaton.id

        expect(response).to be_successful
        expect(json_response.size).to eq(2)
        expect(json_response.map {|n| n["id"]}).not_to include(first_notificaton.id)
      end

      it "allows to filter by notification_type" do
        FactoryGirl.create :notification, notification_type: :foo
        FactoryGirl.create :notification, notification_type: :bar

        xhr :get, :notifications, notification_type: :foo

        expect(response).to be_successful
        expect(json_response.size).to eq(1)
        expect(json_response[0]["notification_type"]).to eq("foo")
      end

    end

  end

  describe "handling Hub's call notifications" do
    # cases taken from real Hub invocations

    before(:each) { office.cases.create! sample_params({guid: "CASE1"}) }
    before(:each) { add_access_token_header }
    let(:case_id)  { office.cases.first.id }

    it "records a not sick patient" do
        expect(Office).to receive(:sync_sick_status)

        expect {

          xhr :put, :update_case, {"community_sick"=>"1", "diarreah_community"=>"2", "diarreah_family"=>"1", "diarreah_individual"=>"1", "family_sick"=>"1", "fever_community"=>"2", "fever_family"=>"1", "headache_community"=>"1", "headache_family"=>"1", "headache_individual"=>"1", "hemorrhage_community"=>"1", "hemorrhage_family"=>"1", "hemorrhage_individual"=>"1", "individual_fever"=>"1", "nausea_vomiting_community"=>"1", "nausea_vomiting_family"=>"1", "nausea_vomiting_individual"=>"1", "rash_community"=>"1", "rash_family"=>"1", "rash_individual"=>"1", "sick"=>"1", "sorethroat_community"=>"3", "sorethroat_family"=>"2", "sorethroat_individual"=>"1", "weakness_pain_community"=>"1", "weakness_pain_family"=>"2", "weakness_pain_individual"=>"1", "id"=>case_id}

        }.not_to change(Notification, :count)

        _case = Case.find(case_id)

        expect(_case.calls_report[:sick]).to be(false)
        expect(_case.calls_report[:family_sick]).to be(false)
        expect(_case.calls_report[:community_sick]).to be(false)

        expect(_case.calls_report[:symptoms]).to be_empty
      end

      it "records a sick patient with all symptoms" do
        expect(Office).to receive(:sync_sick_status)

        expect {

          xhr :put, :update_case, {"community_sick"=>"1", "diarreah_community"=>"2", "diarreah_family"=>"1", "diarreah_individual"=>"2", "family_sick"=>"1", "fever_community"=>"2", "fever_family"=>"1", "headache_community"=>"1", "headache_family"=>"1", "headache_individual"=>"2", "hemorrhage_community"=>"1", "hemorrhage_family"=>"1", "hemorrhage_individual"=>"2", "individual_fever"=>"2", "nausea_vomiting_community"=>"1", "nausea_vomiting_family"=>"1", "nausea_vomiting_individual"=>"2", "rash_community"=>"1", "rash_family"=>"1", "rash_individual"=>"2", "sick"=>"2", "sorethroat_community"=>"3", "sorethroat_family"=>"2", "sorethroat_individual"=>"2", "weakness_pain_community"=>"1", "weakness_pain_family"=>"2", "weakness_pain_individual"=>"2", "id"=>case_id}

        }.to change(Notification, :count).by(1)

        _case = Case.find(case_id)

        expect(_case.calls_report[:sick]).to be(true)
        expect(_case.calls_report[:family_sick]).to be(false)
        expect(_case.calls_report[:community_sick]).to be(false)

        expect(_case.calls_report[:symptoms]).to include("diarreah_individual")
        expect(_case.calls_report[:symptoms]).to include("headache_individual")
        expect(_case.calls_report[:symptoms]).to include("hemorrhage_individual")
        expect(_case.calls_report[:symptoms]).to include("nausea_vomiting_individual")
        expect(_case.calls_report[:symptoms]).to include("rash_individual")
        expect(_case.calls_report[:symptoms]).to include("sorethroat_individual")
        expect(_case.calls_report[:symptoms]).to include("weakness_pain_individual")
        expect(_case.calls_report[:symptoms]).to include("individual_fever")

        expect(_case.calls_report[:symptoms]).to_not include("diarreah_family")
        expect(_case.calls_report[:symptoms]).to_not include("fever_family")
        expect(_case.calls_report[:symptoms]).to_not include("headache_family")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_family")
        expect(_case.calls_report[:symptoms]).to_not include("nausea_vomiting_family")
        expect(_case.calls_report[:symptoms]).to_not include("rash_family")
        expect(_case.calls_report[:symptoms]).to_not include("sorethroat_family")
        expect(_case.calls_report[:symptoms]).to_not include("weakness_pain_family")

        expect(_case.calls_report[:symptoms]).to_not include("diarreah_community")
        expect(_case.calls_report[:symptoms]).to_not include("fever_community")
        expect(_case.calls_report[:symptoms]).to_not include("headache_community")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_community")
        expect(_case.calls_report[:symptoms]).to_not include("nausea_vomiting_community")
        expect(_case.calls_report[:symptoms]).to_not include("rash_community")
        expect(_case.calls_report[:symptoms]).to_not include("sorethroat_community")
        expect(_case.calls_report[:symptoms]).to_not include("weakness_pain_community")
      end

      it "records a patient with a sick family member" do
        expect(Office).to receive(:sync_sick_status)

        expect {

          xhr :put, :update_case, {"community_sick"=>"1", "diarreah_community"=>"2", "diarreah_family"=>"2", "diarreah_individual"=>"2", "family_sick"=>"2", "fever_community"=>"2", "fever_family"=>"1", "headache_community"=>"1", "headache_family"=>"1", "headache_individual"=>"2", "hemorrhage_community"=>"1", "hemorrhage_family"=>"1", "hemorrhage_individual"=>"2", "individual_fever"=>"2", "nausea_vomiting_community"=>"1", "nausea_vomiting_family"=>"1", "nausea_vomiting_individual"=>"2", "rash_community"=>"1", "rash_family"=>"3", "rash_individual"=>"2", "sick"=>"1", "sorethroat_community"=>"3", "sorethroat_family"=>"2", "sorethroat_individual"=>"2", "weakness_pain_community"=>"1", "weakness_pain_family"=>"1", "weakness_pain_individual"=>"2", "id"=>case_id}

        }.to_not change(Notification, :count)

        _case = Case.find(case_id)

        expect(_case.calls_report[:sick]).to be(false)
        expect(_case.calls_report[:family_sick]).to be(true)
        expect(_case.calls_report[:community_sick]).to be(false)

        expect(_case.calls_report[:symptoms]).to_not include("diarreah_individual")
        expect(_case.calls_report[:symptoms]).to_not include("headache_individual")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_individual")
        expect(_case.calls_report[:symptoms]).to_not include("nausea_vomiting_individual")
        expect(_case.calls_report[:symptoms]).to_not include("rash_individual")
        expect(_case.calls_report[:symptoms]).to_not include("sorethroat_individual")
        expect(_case.calls_report[:symptoms]).to_not include("weakness_pain_individual")
        expect(_case.calls_report[:symptoms]).to_not include("individual_fever")

        expect(_case.calls_report[:symptoms]).to include("diarreah_family")
        expect(_case.calls_report[:symptoms]).to_not include("fever_family")
        expect(_case.calls_report[:symptoms]).to_not include("headache_family")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_family")
        expect(_case.calls_report[:symptoms]).to_not include("nausea_vomiting_family")
        expect(_case.calls_report[:symptoms]).to_not include("rash_family")
        expect(_case.calls_report[:symptoms]).to include("sorethroat_family")
        expect(_case.calls_report[:symptoms]).to_not include("weakness_pain_family")

        expect(_case.calls_report[:symptoms]).to_not include("diarreah_community")
        expect(_case.calls_report[:symptoms]).to_not include("fever_community")
        expect(_case.calls_report[:symptoms]).to_not include("headache_community")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_community")
        expect(_case.calls_report[:symptoms]).to_not include("nausea_vomiting_community")
        expect(_case.calls_report[:symptoms]).to_not include("rash_community")
        expect(_case.calls_report[:symptoms]).to_not include("sorethroat_community")
        expect(_case.calls_report[:symptoms]).to_not include("weakness_pain_community")
      end

      it "fails when not sure if there's a sick family member" do
        expect(Office).to receive(:sync_sick_status)

        expect {

          xhr :put, :update_case, {"community_sick"=>"1", "diarreah_community"=>"2", "diarreah_family"=>"2", "diarreah_individual"=>"2", "family_sick"=>"undefined", "fever_community"=>"2", "fever_family"=>"1", "headache_community"=>"1", "headache_family"=>"1", "headache_individual"=>"2", "hemorrhage_community"=>"1", "hemorrhage_family"=>"1", "hemorrhage_individual"=>"2", "individual_fever"=>"2", "nausea_vomiting_community"=>"1", "nausea_vomiting_family"=>"1", "nausea_vomiting_individual"=>"2", "rash_community"=>"1", "rash_family"=>"3", "rash_individual"=>"2", "sick"=>"1", "sorethroat_community"=>"3", "sorethroat_family"=>"2", "sorethroat_individual"=>"2", "weakness_pain_community"=>"1", "weakness_pain_family"=>"1", "weakness_pain_individual"=>"2", "id"=>case_id}

        }.to_not change(Notification, :count)

        _case = Case.find(case_id)

        expect(_case.calls_report[:sick]).to be(false)
        expect(_case.calls_report[:family_sick]).to be(false)
        expect(_case.calls_report[:community_sick]).to be(false)

        expect(_case.calls_report[:symptoms]).to be_empty
      end

      it "records a community member is sick with unknown symptoms" do
        expect(Office).to receive(:sync_sick_status)

        expect {

          xhr :put, :update_case, {"community_sick"=>"2", "diarreah_community"=>"1", "diarreah_family"=>"2", "diarreah_individual"=>"2", "family_sick"=>"1", "fever_community"=>"3", "fever_family"=>"1", "headache_community"=>"3", "headache_family"=>"1", "headache_individual"=>"2", "hemorrhage_community"=>"1", "hemorrhage_family"=>"1", "hemorrhage_individual"=>"2", "individual_fever"=>"2", "nausea_vomiting_community"=>"2", "nausea_vomiting_family"=>"1", "nausea_vomiting_individual"=>"2", "rash_community"=>"1", "rash_family"=>"3", "rash_individual"=>"2", "sick"=>"1", "sorethroat_community"=>"1", "sorethroat_family"=>"2", "sorethroat_individual"=>"2", "weakness_pain_community"=>"2", "weakness_pain_family"=>"1", "weakness_pain_individual"=>"2", "id"=>case_id}

        }.to_not change(Notification, :count)

        _case = Case.find(case_id)

        expect(_case.calls_report[:sick]).to be(false)
        expect(_case.calls_report[:family_sick]).to be(false)
        expect(_case.calls_report[:community_sick]).to be(true)

        expect(_case.calls_report[:symptoms]).to_not include("diarreah_individual")
        expect(_case.calls_report[:symptoms]).to_not include("headache_individual")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_individual")
        expect(_case.calls_report[:symptoms]).to_not include("nausea_vomiting_individual")
        expect(_case.calls_report[:symptoms]).to_not include("rash_individual")
        expect(_case.calls_report[:symptoms]).to_not include("sorethroat_individual")
        expect(_case.calls_report[:symptoms]).to_not include("weakness_pain_individual")
        expect(_case.calls_report[:symptoms]).to_not include("individual_fever")

        expect(_case.calls_report[:symptoms]).to_not include("diarreah_family")
        expect(_case.calls_report[:symptoms]).to_not include("fever_family")
        expect(_case.calls_report[:symptoms]).to_not include("headache_family")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_family")
        expect(_case.calls_report[:symptoms]).to_not include("nausea_vomiting_family")
        expect(_case.calls_report[:symptoms]).to_not include("rash_family")
        expect(_case.calls_report[:symptoms]).to_not include("sorethroat_family")
        expect(_case.calls_report[:symptoms]).to_not include("weakness_pain_family")

        expect(_case.calls_report[:symptoms]).to_not include("diarreah_community")
        expect(_case.calls_report[:symptoms]).to_not include("fever_community")
        expect(_case.calls_report[:symptoms]).to_not include("headache_community")
        expect(_case.calls_report[:symptoms]).to_not include("hemorrhage_community")
        expect(_case.calls_report[:symptoms]).to include("nausea_vomiting_community")
        expect(_case.calls_report[:symptoms]).to_not include("rash_community")
        expect(_case.calls_report[:symptoms]).to_not include("sorethroat_community")
        expect(_case.calls_report[:symptoms]).to include("weakness_pain_community")
      end

      it "records a sick patient with no symptoms" do
        expect(Office).to receive(:sync_sick_status)

        expect {

          xhr :put, :update_case, {"community_sick"=>"2", "diarreah_community"=>"1", "diarreah_family"=>"2", "diarreah_individual"=>"1", "family_sick"=>"1", "fever_community"=>"3", "fever_family"=>"1", "headache_community"=>"3", "headache_family"=>"1", "headache_individual"=>"1", "hemorrhage_community"=>"1", "hemorrhage_family"=>"1", "hemorrhage_individual"=>"1", "individual_fever"=>"1", "nausea_vomiting_community"=>"2", "nausea_vomiting_family"=>"1", "nausea_vomiting_individual"=>"1", "rash_community"=>"1", "rash_family"=>"3", "rash_individual"=>"1", "sick"=>"2", "sorethroat_community"=>"1", "sorethroat_family"=>"2", "sorethroat_individual"=>"1", "weakness_pain_community"=>"2", "weakness_pain_family"=>"1", "weakness_pain_individual"=>"1", "id"=>case_id}

        }.to change(Notification, :count).by(1)

        _case = Case.find(case_id)

        expect(_case.calls_report[:sick]).to be(true)
        expect(_case.calls_report[:family_sick]).to be(false)
        expect(_case.calls_report[:community_sick]).to be(false)

        expect(_case.calls_report[:symptoms]).to be_empty
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
     "report_time" => "2015-03-31T19:04:19.750Z"
   }.merge(overrides) 
  end

  def add_access_token_header
    request.env['HTTP_AUTHORIZATION'] = ActionController::HttpAuthentication::Token.encode_credentials(access_token)
  end

end