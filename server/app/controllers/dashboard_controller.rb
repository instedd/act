class DashboardController < AuthenticatedController
  def view
    @body_class = "dashboard"
  end
end
