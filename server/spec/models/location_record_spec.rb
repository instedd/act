require 'rails_helper'

describe LocationRecord do

  let(:_case) { FactoryGirl.create :case }

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

end