RSpec::Matchers.define :be_unauthorized do
  match do |response|
    response.status == 403
  end

  failure_message do |res|
    "Expected response code to be 403 but was #{res.status}"
  end

  failure_message_when_negated do |res|
    "Didn't expect response code to be 403"
  end
end