class CasesFile < ActiveRecord::Base

  belongs_to :device

  validates_presence_of :device, :file

  delegate :organization, :organization_id, to: :device

  has_many :cases
end