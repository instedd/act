module FlashMessageHelper

  def flash_message(timeout = nil)
    res = nil
    keys = {
      notice: 'flash_notice alert alert-info',
      error:  'flash_error  alert alert-danger',
      alert:  'flash_error  alert alert-warning'
    }

    keys.each do |key, value|
      if flash[key]
        attrs = {
          class: "flash #{value}",
          style: "display:none"
        }
        attrs['data-hide-timeout'] = timeout unless !timeout or flash[:flash_message_persist]

        res = content_tag :div, attrs do
          content_tag :div do
            flash[key]
          end
        end
      end
    end

    res
  end

end