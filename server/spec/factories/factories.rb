FactoryGirl.define do

  factory :device do
    organization_name       "instedd"
    location_id             123
    supervisor_name         "John Doe"
    supervisor_phone_number "123"
    
    public_key "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCuNYfUAOSfCEsiV8ZETZ92LkwHHGHaaSXcgfDlDQZHSaChUjaQ"\
               "fUho/bpSG4XLVjL33F/6fEQmdR+VSXMcctYaP7YPvTRDylhynQ+Yr+hoFt8d3uxbSRTYOJ9+y94zNGEGSRX+3HiNK"\
               "aS30v3UDLkB8oXtPbfzIVnWH+0BQViy+nils0y+EpdIgTp85eVf4Ozok8r0AUZGbE5Oi7zS5YHkoN5VZLuIazL9X1"\
               "vanKK1diw9ouPf0jIdCUpWQWo04fwBoSelTfXwwEgcZNOdvGgYC7HVFdEqe93K7jXshqdYbxM5qxIzACff+pfG1mT"\
               "GImg1IM8X43fs/+t6R0BO6scX user@client"
  end

  factory :user do
    email      "act_admin@instedd.org"
    password   "12345678"
  end

end