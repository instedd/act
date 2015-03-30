require 'rails_helper'

describe Organization do

  let!(:organization) { FactoryGirl.create :organization }

  it "cannot be created without a name" do
    expect {
      Organization.create!
    }.to raise_error
  end

  it "references registered containers" do
    office = create_office(organization)

    expect(organization.reload.offices).to include(office)
  end

  it "can be deleted if it has no registered offices" do
    expect{
      organization.destroy!
    }.to change(Organization, :count).by(-1)
  end

  it "cannot be deleted if it has registered offices" do
    office = create_office(organization)
    
    expect{
      organization.destroy!
    }.to raise_error
    
    expect{
      organization.reload
    }.not_to raise_error
  end

  def create_office(organization)
    FactoryGirl.create :office, organization: organization
  end

end