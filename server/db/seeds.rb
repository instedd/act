unless User.where(email: Settings.user.admin_username).any?
  user = User.new(email: Settings.user.admin_username, password: Settings.user.admin_password)
  user.save!
end