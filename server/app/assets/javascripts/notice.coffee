$(document).ready ->

  message = $(".flash")
  if (message.length > 0)
    timeout = message.attr('data-hide-timeout') || 2000
    
    message.fadeIn()
    window.setTimeout(( -> message.fadeOut()), timeout)