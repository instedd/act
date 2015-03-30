class Ability
  include CanCan::Ability

  # The first argument to `can` is the action you are giving the user
  # permission to do.
  # If you pass :manage it will apply to every action. Other common actions
  # here are :read, :create, :update and :destroy.
  #
  # The second argument is the resource the user can perform the action on.
  # If you pass :all it will apply to every resource. Otherwise pass a Ruby
  # class of the resource.
  #
  # The third argument is an optional hash of conditions to further filter the
  # objects.
  # For example, here the user can only update published articles.
  #
  #   can :update, Article, :published => true
  #
  # See the wiki for details:
  # https://github.com/CanCanCommunity/cancancan/wiki/Defining-Abilities
  def initialize(user)
    if user.admin?
      admin user
    else
      organization_user user
    end
  end

  def admin user
      can :manage, :all
      cannot :destroy, Case
      cannot :destroy, Office, confirmed: true
  end

  def organization_user user
    can :read, Organization, id: user.organization_id
    can :read, Office, organization_id: user.organization_id
    can :read, Case,   office: { organization_id: user.organization_id, confirmed: true }
  end

end
