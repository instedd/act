class CallRecord < ActiveRecord::Base
  belongs_to :case

  serialize :symptoms, JSON

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

  def who_is_sick
    return "Patient sick" if sick?
    return "Family member sick" if family_sick?
    return "Community member sick" if community_sick?
    "Not sick"
  end

  def formated_symptoms
    known_symptoms = CallRecord.known_symptoms
    positive_symptoms = (symptoms.select { |symptom, value| value }).keys
    symptoms_descriptions = positive_symptoms.map {|key| CallRecord.known_symptoms[key] }
    symptoms_descriptions.join ", "
  end

end
