class Organization < ActiveRecord::Base

  has_many :devices, dependent: :restrict_with_error

  def can_be_deleted?
    not devices.any?
  end

end