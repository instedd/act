require 'rails_helper'

describe LocationRecord do

  before(:each) {
    stub_request(:head, "http://localhost:9200/cases").
      with(:headers => {'Accept'=>'*/*', 'User-Agent'=>'Faraday v0.9.1'}).
      to_return(:status => 200, :body => "", :headers => {})

    stub_request(:put, /http:\/\/localhost:9200\/cases\/case\/.*/).
      to_return(:status => 200, :body => "", :headers => {})

  }

  let(:_case) { FactoryGirl.create :case }
  let(:location) { FactoryGirl.create :location, lat: 1.1, lng: 0.9, geo_id: "777_5_2" }

  it "validates presence of case and coordinates" do
    expect { LocationRecord.create! lat: 1, lng: 1 }.to raise_error
    expect { LocationRecord.create! case: _case, lat: 1 }.to raise_error
    expect { LocationRecord.create! case: _case, lng: 1 }.to raise_error
    expect {
      LocationRecord.create! case: _case, lat: 1, lng: 1
    }.to change(LocationRecord, :count).by(1)
  end

  it "saves creation timestamp when saved" do
    record = LocationRecord.create! case: _case, lat: 1, lng: 1
    expect(record.created_at).not_to be_nil
  end

  it "associates a location" do
    record = LocationRecord.create! case: _case, lat: 1, lng: 1, location: location
    expect(record.created_at).not_to be_nil
    expect(record.location_id).to eq(location.id)
  end

end
