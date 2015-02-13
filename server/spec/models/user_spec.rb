require 'rails_helper'
require 'cancan/matchers'

describe User do

  before(:each) {
    stub_request(:head, "http://localhost:9200/cases").
      with(:headers => {'Accept'=>'*/*', 'User-Agent'=>'Faraday v0.9.1'}).
      to_return(:status => 200, :body => "", :headers => {})

    stub_request(:put, /http:\/\/localhost:9200\/cases\/case\/.*/).
      to_return(:status => 200, :body => "", :headers => {})

  }

  describe "authorization for organization users" do

    let(:user)    { FactoryGirl.create :organization_user }

    describe "access to devices" do

      it "cannot access non-approved devices" do
        device = FactoryGirl.create :non_approved_device
        expect(user).not_to be_able_to(:read,    device)
        expect(user).not_to be_able_to(:create,  device)
        expect(user).not_to be_able_to(:update,  device)
        expect(user).not_to be_able_to(:destroy, device)
      end

      it "can access devices approved for his organzation" do
        device = FactoryGirl.build :approved_device, organization: user.organization
        expect(user).to be_able_to(:read, device)
      end

      it "cannot modify devices approved for his organzation" do
        device = FactoryGirl.build :approved_device, organization: user.organization
        expect(user).not_to be_able_to(:create, device)
        expect(user).not_to be_able_to(:update, device)
        expect(user).not_to be_able_to(:destroy, device)
      end

      it "cannot access devices approved for other organizations" do
        device = FactoryGirl.build :approved_device
        expect(user).not_to be_able_to(:read,    device)
        expect(user).not_to be_able_to(:create,  device)
        expect(user).not_to be_able_to(:update,  device)
        expect(user).not_to be_able_to(:destroy, device)
      end

    end

    describe "access to users" do

      it "cannot access users" do
        expect(user).not_to be_able_to(:read,    User)
        expect(user).not_to be_able_to(:create,  User)
        expect(user).not_to be_able_to(:update,  User)
        expect(user).not_to be_able_to(:destroy, User)
      end

    end

    describe "access to organizations" do

      it "can access his organization" do
        expect(user).to be_able_to(:read, user.organization)
      end

      it "cannot modify his organization" do
        expect(user).not_to be_able_to(:update,  user.organization)
        expect(user).not_to be_able_to(:destroy, user.organization)
      end

      it "cannot access other organizations" do
        other_organization = FactoryGirl.create :organization
        
        expect(user).not_to be_able_to(:read,    other_organization)
        expect(user).not_to be_able_to(:create,  other_organization)
        expect(user).not_to be_able_to(:update,  other_organization)
        expect(user).not_to be_able_to(:destroy, other_organization)
      end

    end

    describe "access to cases" do

      let(:same_organization_case) do
        device = FactoryGirl.create :device, organization: user.organization
        FactoryGirl.create :case, device: device
      end

      let(:other_organization_case) do
        FactoryGirl.create :case
      end

      it "can access cases belonging to his organization" do
        expect(user).to be_able_to(:read, same_organization_case)
      end

      it "cannot modify cases belonging to his organization" do
        expect(user).not_to be_able_to(:create,  same_organization_case)
        expect(user).not_to be_able_to(:update,  same_organization_case)
        expect(user).not_to be_able_to(:destroy, same_organization_case)
      end

      it "cannot access cases belonging to other organizations" do
        expect(user).not_to be_able_to(:read,    other_organization_case)
        expect(user).not_to be_able_to(:create,  other_organization_case)
        expect(user).not_to be_able_to(:update,  other_organization_case)
        expect(user).not_to be_able_to(:destroy, other_organization_case)
      end

    end

  end

end