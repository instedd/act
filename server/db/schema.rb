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

ActiveRecord::Schema.define(version: 20141106214235) do

  # These are extensions that must be enabled in order to support this database
  enable_extension "plpgsql"

  create_table "cases", force: true do |t|
    t.integer  "device_id"
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
  end

  create_table "devices", force: true do |t|
    t.string   "guid"
    t.string   "organization_name"
    t.integer  "location_id"
    t.string   "supervisor_phone_number"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

end
