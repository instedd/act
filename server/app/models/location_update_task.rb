require 'rexml/document'
include REXML

class LocationUpdateTask

  #
  # amount if times a task can be retried on errors that are
  # assumed to be recoverable or temporary.
  #
  TASK_RETRIES = 2
  
  @queue = :location_update

  def self.perform(case_id, number, retry_count = 0)
    begin
      response = request_location(number)
    rescue => ex
      Rails.logger.error "An error occurred contacting the Cellcom API "\
                         "for location check of case #{case_id}: #{ex}\n"\
                         "#{ex.backtrace.take(15).join("\n")}"
      
      retry_later(case_id, number, retry_count)
    end
    
    process_result(case_id, response) if response
  end

  def self.retry_later(case_id, number, retry_count)
    if (retry_count < TASK_RETRIES)
      Resque.enqueue_in(30.minutes, self, case_id, number, retry_count + 1)
    end
  end


  class QueueTaskJob

    def self.perform
      Case.select(:id, :patient_phone_number).find_each do |c|
        Resque.enqueue(::LocationUpdateTask, c.id, c.patient_phone_number)
      end
    end

  end

  private

  def self.request_location(number)
    url = URI.parse(Settings.cellcom.location_endpoint)
    request = Net::HTTP::Post.new(url.path)
    request.content_type = "application/soap+xml"
    request.body = request_body(number)
    Net::HTTP.start(url.host, url.port) { |http| http.request(request) }
  end

  def self.request_body(number)
    ctx = {
      number: number,
      username: Settings.cellcom.username,
      password: Settings.cellcom.password
    }
    ERBTemplate.new(REQUEST_TEMPLATE).render(ctx)
  end

  def self.process_result(case_id, response)
    doc = Document.new(response.body)
    result_node = XPath.first(doc, '/s:Envelope/s:Body/SubscriberGetLocationResponse/SubscriberGetLocationResult')
    result_id = XPath.first(result_node, '//[local-name() = "ResultID"]').text
    
    case result_id
    when "0"
      reply_msg = XPath.first(result_node, '//[local-name() = "ReplyMsg"]').text
      lat, lng = reply_msg.split(",").map { |part| part.strip.to_f }
      LocationRecord.create! case_id: case_id, lat: lat, lng: lng
    
    when "1"
      #
      # TODO
      #
      # There are different result codes for non-error responses: one means
      # that the location was found and the other that it couldn't be found
      # but there were no errors processing the request.
      #
      # API documentation is not clear about which code corresponds to each
      # of those cases.
    
    when "-5", "-6"
      #
      # TODO: notify development team?
      #
      Rails.logger.error "Cellcom API returned result code #{result_id}."\
                          "This means an authentication or authorization problem, task will not be"
    else
      #
      # TODO
      #
      # Other kind of errors could represent different things. For example:
      #
      # - contact does not have a cellcom account
      # - invalid phone number
      # - recoverable technical problem in cellcom server
      #
      # At the moment it is not clear how to distinguish these cases, and which
      # (and how) we should notify to the field supervisor in charge of following
      # the case.
      #
    end
  end


REQUEST_TEMPLATE = <<-XML
<s:Envelope xmlns:a="http://www.w3.org/2005/08/addressing" xmlns:s="http://www.w3.org/2003/05/soap-envelope">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://tempuri.org/ISubscribers/SubscriberGetLocation</a:Action>
    <a:MessageID>urn:uuid:bdb732fc-dd11-4744-941c-15657735cbe0</a:MessageID>
    <a:ReplyTo>
      <a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address>
    </a:ReplyTo>
  </s:Header>
  <s:Body>
    <SubscriberGetLocation xmlns="http://tempuri.org/">
      <c_subs_req xmlns:d4p1="http://schemas.datacontract.org/2004/07/CellcomAPILibrary" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        <d4p1:Client>ACT</d4p1:Client>
        <d4p1:Password><%= @password %></d4p1:Password>
        <d4p1:UserName><%= @username %></d4p1:UserName>
        <d4p1:PhoneNumber><%= @number %></d4p1:PhoneNumber>
      </c_subs_req>
    </SubscriberGetLocation>
  </s:Body>
</s:Envelope>
XML

end