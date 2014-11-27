module NavigationHelper

  def navigation_item title, active_controller, link_path, opts = {}
    unless opts.include?(:if) and !opts[:if]
      is_active = active_controller.eql? controller_name.to_sym

      content_tag :li, class: [('active' if is_active)] do
        link_to title, link_path
      end
    end
  end

end