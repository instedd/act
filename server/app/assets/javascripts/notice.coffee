$(document).ready ->

  message = $(".flash")
  if (message.length > 0)
    timeout = message.attr('data-hide-timeout') || 2000
    
    window.setTimeout((() -> message.fadeOut(400)), timeout)