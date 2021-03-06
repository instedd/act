# encoding: UTF-8
# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20150408224301) do

  # These are extensions that must be enabled in order to support this database
  enable_extension "plpgsql"
  enable_extension "hstore"

  create_table "api_keys", force: true do |t|
    t.string   "access_token"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "label"
  end

  add_index "api_keys", ["access_token"], name: "index_api_keys_on_access_token", unique: true, using: :btree

  create_table "call_records", force: true do |t|
    t.integer  "case_id"
    t.boolean  "sick"
    t.datetime "created_at",                     null: false
    t.datetime "updated_at",                     null: false
    t.boolean  "family_sick"
    t.boolean  "community_sick"
    t.text     "symptoms"
    t.boolean  "successful",      default: true
    t.string   "reported_status"
  end

  create_table "cases", force: true do |t|
    t.integer  "office_id"
    t.string   "patient_name"
    t.string   "patient_phone_number"
    t.integer  "patient_age"
    t.string   "patient_gender"
    t.string   "dialect_code"
    t.string   "symptoms",             default: [], array: true
    t.text     "note"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "guid"
    t.datetime "report_time"
  end

  create_table "location_shapes", force: true do |t|
    t.integer  "location_id"
    t.string   "geo_id"
    t.text     "geo_shape"
    t.string   "geo_type"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  add_index "location_shapes", ["location_id"], name: "index_location_shapes_on_location_id", using: :btree

  create_table "locations", force: true do |t|
    t.integer  "parent_id"
    t.string   "name"
    t.integer  "lft"
    t.integer  "rgt"
    t.float    "lat"
    t.float    "lng"
    t.integer  "depth"
    t.string   "geo_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "notifications", force: true do |t|
    t.string   "notification_type", null: false
    t.hstore   "metadata"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "offices", force: true do |t|
    t.string   "guid"
    t.string   "reported_organization_name"
    t.string   "supervisor_phone_number"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "supervisor_name"
    t.text     "public_key"
    t.boolean  "confirmed",                  default: false
    t.boolean  "public_key_allowed",         default: false
    t.integer  "organization_id"
    t.string   "reported_location_code"
    t.integer  "location_id"
  end

  add_index "offices", ["organization_id"], name: "index_offices_on_organization_id", using: :btree

  create_table "organizations", force: true do |t|
    t.string   "name",       null: false
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "users", force: true do |t|
    t.string   "email",                  default: "", null: false
    t.string   "encrypted_password",     default: ""
    t.string   "reset_password_token"
    t.datetime "reset_password_sent_at"
    t.datetime "remember_created_at"
    t.integer  "sign_in_count",          default: 0,  null: false
    t.datetime "current_sign_in_at"
    t.datetime "last_sign_in_at"
    t.inet     "current_sign_in_ip"
    t.inet     "last_sign_in_ip"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string   "invitation_token"
    t.datetime "invitation_sent_at"
    t.datetime "invitation_accepted_at"
    t.integer  "invitation_limit"
    t.integer  "invited_by_id"
    t.string   "invited_by_type"
    t.datetime "invitation_created_at"
    t.integer  "organization_id"
  end

  add_index "users", ["email"], name: "index_users_on_email", unique: true, using: :btree
  add_index "users", ["invitation_token"], name: "index_users_on_invitation_token", unique: true, using: :btree
  add_index "users", ["reset_password_token"], name: "index_users_on_reset_password_token", unique: true, using: :btree

end
