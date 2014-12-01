require 'rails_helper'

describe Organization do

  let!(:organization) { FactoryGirl.create :organization }

  it "cannot be created without a name" do
    expect {
      Organization.create!
    }.to raise_error
  end

  it "references registered containers" do
    device = create_device(organization)

    expect(organization.reload.devices).to include(device)
  end

  it "can be deleted if it has no registered devices" do
    expect{
      organization.destroy!
    }.to change(Organization, :count).by(-1)
  end

  it "cannot be deleted if it has registered devices" do
    device = create_device(organization)
    
    expect{
      organization.destroy!
    }.to raise_error
    
    expect{
      organization.reload
    }.not_to raise_error
  end

  def create_device(organization)
    FactoryGirl.create :device, organization: organization
  end

end