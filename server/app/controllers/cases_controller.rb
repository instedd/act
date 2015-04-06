class CasesController < AuthenticatedController

  load_and_authorize_resource

  def index
    @cases = @cases.order("updated_at DESC")
  end

  def show
  end

  def new
    @offices = Office.all
  end

  def create
    @case.guid = SecureRandom.uuid
    @case.report_time = DateTime.now
    @case.symptoms = params[:case][:symptoms].reject { |symptom| symptom.blank? }
    if @case.save
      flash[:notice] = "Case of #{@case.patient_name} reported"
      redirect_to cases_path
    else
      @offices = Office.all
      flash[:error] = "Please fix the erroneous data for reporting a new case"
      render action: "new"
    end
  end

  private

  def update_params
    params.require(:organization).permit(:confirmed)
  end

  def create_params
    params.require(:case).permit(:office_id, :patient_name, :patient_phone_number, :patient_age, :patient_gender, :dialect_code, :symptoms, :note)
  end


end
