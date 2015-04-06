class Organization < ActiveRecord::Base

  has_many :offices, dependent: :restrict_with_error

  def can_be_deleted?
    not offices.any?
  end

end