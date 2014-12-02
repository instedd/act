require 'rails_helper'

describe Notification do

  it "cannot be created without a type" do
    expect { Notification.create! }.to raise_error
    expect { Notification.create! notification_type: :case_confirmed_sick }.not_to raise_error
  end

  it "can store arbitrary metadata" do
    notification = Notification.create! notification_type: :case_confirmed_sick, metadata: { "foo" => "bar" }
    expect(notification.reload.metadata["foo"]).to eq("bar")
  end

end