class User < ActiveRecord::Base

  belongs_to :organization
  
  # Include default devise modules. Others available are:
  # :confirmable, :lockable, :timeoutable and :omniauthable
  devise :invitable, :database_authenticatable, :recoverable, :rememberable, :trackable, :validatable

  delegate :can?, to: :ability

  def admin?
    organization.nil?
  end

  def ability
    @ability ||= Ability.new(self)
  end

end
