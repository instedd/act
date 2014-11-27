FactoryGirl.define do

  factory :non_approved_device, class: Device do
    reported_organization_name       "instedd"
    location_id                       123
    supervisor_name                   "John Doe"
    supervisor_phone_number           "123"
    
    public_key "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCuNYfUAOSfCEsiV8ZETZ92LkwHHGHaaSXcgfDlDQZHSaChUjaQ"\
               "fUho/bpSG4XLVjL33F/6fEQmdR+VSXMcctYaP7YPvTRDylhynQ+Yr+hoFt8d3uxbSRTYOJ9+y94zNGEGSRX+3HiNK"\
               "aS30v3UDLkB8oXtPbfzIVnWH+0BQViy+nils0y+EpdIgTp85eVf4Ozok8r0AUZGbE5Oi7zS5YHkoN5VZLuIazL9X1"\
               "vanKK1diw9ouPf0jIdCUpWQWo04fwBoSelTfXwwEgcZNOdvGgYC7HVFdEqe93K7jXshqdYbxM5qxIzACff+pfG1mT"\
               "GImg1IM8X43fs/+t6R0BO6scX user@client"
  end

  factory :approved_device, parent: :non_approved_device, aliases: [:device] do
    approved_at   { 1.day.ago }
    organization
  end

  factory :organization do
    name     "Instedd"
  end

  factory :admin_user, class: User do
    email        "act_admin@instedd.org"
    password     "12345678"
    organization nil
  end

  factory :organization_user, aliases: [:user], class: User do
    email        "act_admin@instedd.org"
    password     "12345678"
    organization
  end

  factory :case do
    patient_name           "John Doe"
    patient_phone_number   "1111111"
    patient_age            25
    patient_gender         "M"
    dialect_code           "123"
    symptoms               ["fever"]
    guid                   { SecureRandom.hex(16).upcase }
    device
  end

end