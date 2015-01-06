require 'rails_helper'

describe LocationUpdateTask do

  let(:_case)         { FactoryGirl.create :case, patient_phone_number: 111000 }
  let(:endpoint_url)  { Settings.cellcom.location_endpoint }

  it "performs SOAP request to retrieve last known location of contact's phone" do
    set_successful_response(123, 456)

    LocationUpdateTask.perform(_case.id, _case.patient_phone_number)
    assert_requested :post, endpoint_url, headers: { 'Content-Type' => 'application/soap+xml'}, body: expected_soap_request
  end

  it "should create a new location record when API call is successful" do
    set_successful_response(123, 456)

    expect {
      LocationUpdateTask.perform(_case.id, _case.patient_phone_number)
    }.to change(LocationRecord, :count).by(1)

    record = LocationRecord.last
    expect(record.lat).to eq(123)
    expect(record.lng).to eq(456)
  end

  it "should create multiple records after repeated calls" do
    set_successful_response(123, 456)
    LocationUpdateTask.perform(_case.id, _case.patient_phone_number)

    set_successful_response(111, 222)
    LocationUpdateTask.perform(_case.id, _case.patient_phone_number)


    location_history = LocationRecord.order(:created_at)
                                     .map { |r| [r.lat, r.lng] }
    expect(location_history).to eq([ [123, 456], [111, 222] ])
  end

  it "should not raise error nor create records for failed calls" do
    set_failed_response
    expect {
      LocationUpdateTask.perform(_case.id, _case.patient_phone_number)
    }.not_to change(LocationRecord, :count)
  end

  it "schedules up to two retries when a connection error occurs" do
    stub_request(:any, endpoint_url).to_raise("connection error")

    expect_retry(1)
    LocationUpdateTask.perform(_case.id, _case.patient_phone_number)

    expect_retry(2)
    LocationUpdateTask.perform(_case.id, _case.patient_phone_number, 1)

    expect_no_retry
    LocationUpdateTask.perform(_case.id, _case.patient_phone_number, 2)
  end

  #---------------------

  def set_successful_response(lat, lng)
    stub_request(:any, endpoint_url).to_return(body: soap_response("#{lat}, #{lng}", true))
  end

  def set_failed_response
    stub_request(:any, endpoint_url).to_return(body: soap_response("error description", false))
  end

  def expect_retry(retry_count)
    expect(Resque).to receive(:enqueue_in).with(30.minutes, LocationUpdateTask, _case.id, _case.patient_phone_number, retry_count)
  end

  def expect_no_retry
    expect(Resque).not_to receive(:enqueue_in)
  end

  def expected_soap_request
    template = <<-XML
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
        <d4p1:Password>cellcom_pass</d4p1:Password>
        <d4p1:UserName>cellcom_user</d4p1:UserName>
        <d4p1:PhoneNumber>##PHONE_NUMBER##</d4p1:PhoneNumber>
      </c_subs_req>
    </SubscriberGetLocation>
  </s:Body>
</s:Envelope>
    XML

    template.sub('##PHONE_NUMBER##', _case.patient_phone_number.to_s)
  end

  def soap_response(reply_msg, return_value)
      template = <<-XML
<s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:a="http://www.w3.org/2005/08/addressing">
  <s:Header>
    <a:Action s:mustUnderstand="1">http://tempuri.org/ISubscribers/SubscriberGetLocationResponse</a:Action>
    <a:RelatesTo>urn:uuid:d04c2973-ca29-4b25-a88e-eecd604cdef1</a:RelatesTo>
  </s:Header>
  <s:Body>
    <SubscriberGetLocationResponse xmlns="http://tempuri.org/">
      <SubscriberGetLocationResult xmlns:b="http://schemas.datacontract.org/2004/07/CellcomAPILibrary" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
        <b:ReplyMsg><%= @reply_msg %></b:ReplyMsg>
        <b:ResultID>111</b:ResultID>
        <b:ReturnValue><%= @return_value %></b:ReturnValue>
      </SubscriberGetLocationResult>
    </SubscriberGetLocationResponse>
  </s:Body>
</s:Envelope>
      XML

      ERBTemplate.new(template).render(reply_msg: reply_msg, return_value: return_value)
  end
end