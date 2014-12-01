require 'rails_helper'

describe ApiKey do

  it "generates access token upon creation" do
    api_key = ApiKey.create!
    expect(api_key.access_token).to be_present
  end

end