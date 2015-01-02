require 'erb'

#
# Simplifies rendering of ERB templates with contexts defined by hashes.
#
# Usage:
#         ERBTemplate.new("<%= foo %>").render(foo: 123)
#
#
class ERBTemplate

  def initialize(template)
    @template = template
  end

  def render(ctx)
    ERB.new(@template).result(ERBContext.new(ctx).get_binding)
  end



  class ERBContext

    def initialize(h)
      h.each_pair do |key, value|
        instance_variable_set("@#{key.to_s}", value)
      end
    end

    def get_binding
      binding
    end

  end

end