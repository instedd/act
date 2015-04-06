require 'rails_helper'
require 'cancan/matchers'

describe User do

  describe "authorization for organization users" do

    let(:user)    { FactoryGirl.create :organization_user }

    describe "access to offices" do

      it "cannot access non-approved offices" do
        office = FactoryGirl.create :non_approved_office
        expect(user).not_to be_able_to(:read,    office)
        expect(user).not_to be_able_to(:create,  office)
        expect(user).not_to be_able_to(:update,  office)
        expect(user).not_to be_able_to(:destroy, office)
      end

      it "can access offices approved for his organzation" do
        office = FactoryGirl.build :approved_office, organization: user.organization
        expect(user).to be_able_to(:read, office)
      end

      it "cannot modify offices approved for his organzation" do
        office = FactoryGirl.build :approved_office, organization: user.organization
        expect(user).not_to be_able_to(:create, office)
        expect(user).not_to be_able_to(:update, office)
        expect(user).not_to be_able_to(:destroy, office)
      end

      it "cannot access offices approved for other organizations" do
        office = FactoryGirl.build :approved_office
        expect(user).not_to be_able_to(:read,    office)
        expect(user).not_to be_able_to(:create,  office)
        expect(user).not_to be_able_to(:update,  office)
        expect(user).not_to be_able_to(:destroy, office)
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
        office = FactoryGirl.create :office, organization: user.organization
        FactoryGirl.create :case, office: office
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