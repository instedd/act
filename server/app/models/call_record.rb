class CallRecord < ActiveRecord::Base
  belongs_to :_case, class_name: 'Case', foreign_key: 'case_id'

  serialize :symptoms, JSON

  before_create :notify_if_sick

  def self.known_symptoms
    {
      "diarreah_community" => "Community member has diarreah",
      "diarreah_family" => "Family member has diarreah",
      "diarreah_individual" => "Patient has diarreah",
      "fever_community" => "Community member has fever",
      "fever_family" => "Family member has fever",
      "headache_community" => "Community member has headeaches",
      "headache_family" => "Family member has headaches",
      "headache_individual" => "Patient has headaches",
      "hemorrhage_community" => "Community member has hemorrhage",
      "hemorrhage_family" => "Family member has hemorrhage",
      "hemorrhage_individual" => "Patient has hemorrhage",
      "individual_fever" => "Patient has fever",
      "nausea_vomiting_community" => "Community member has nausea or vomits",
      "nausea_vomiting_family" => "Family member has nausea or vomits",
      "nausea_vomiting_individual" => "Patient has nausea or vomits",
      "rash_community" => "Community member has rash",
      "rash_family" => "Family member has rash",
      "rash_individual" => "Patient has rash",
      "sorethroat_community" => "Community member has sore throat",
      "sorethroat_family" => "Family member has sore throat",
      "sorethroat_individual" => "Patient has sore throat",
      "weakness_pain_community" => "Community member feels weak or muscle pain",
      "weakness_pain_family" => "Family member feels weak or muscle pain",
      "weakness_pain_individual" => "Patient feels weak or muscle pain"
    }
  end

  def self.reports_to_symptoms
    {
      "diarreah_community" => "Diarreah",
      "diarreah_family" => "Diarreah",
      "diarreah_individual" => "Diarreah",
      "fever_community" => "Fever",
      "fever_family" => "Fever",
      "headache_community" => "Severe headache",
      "headache_family" => "Severe headache",
      "headache_individual" => "Severe headache",
      "hemorrhage_community" => "Unexplained hemorrhage, bleeding or bruising",
      "hemorrhage_family" => "Unexplained hemorrhage, bleeding or bruising",
      "hemorrhage_individual" => "Unexplained hemorrhage, bleeding or bruising",
      "individual_fever" => "Fever",
      "nausea_vomiting_community" => "Vomiting",
      "nausea_vomiting_family" => "Vomiting",
      "nausea_vomiting_individual" => "Vomiting",
      "rash_community" => "Rash",
      "rash_family" => "Rash",
      "rash_individual" => "Rash",
      "sorethroat_community" => "Sore throat",
      "sorethroat_family" => "Sore throat",
      "sorethroat_individual" => "Sore throat",
      "weakness_pain_community" => "Weakness",
      "weakness_pain_family" => "Weakness",
      "weakness_pain_individual" => "Weakness"
    }
  end

  def who_is_sick
    return "Patient sick" if sick?
    return "Family member sick" if family_sick?
    return "Community member sick" if community_sick?
    "Not sick"
  end

  def formatted_symptoms
    positive_symptoms = (symptoms.select { |symptom, value| value }).keys
    symptoms_descriptions = CallRecord.formatted_symptoms positive_symptoms
    symptoms_descriptions.join ", "
  end

  def self.formatted_symptoms symptoms
    known_symptoms = CallRecord.known_symptoms
    symptoms.map {|key| known_symptoms[key] }
  end

  def notify_if_sick
    Notification.case_confirmed_sick! _case if sick? and !_case.sick?
  end

end
