package actions;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.Time;

public class SubmitCarbonEmissionReport implements Action {
	
	final private Double carbonEmission;	

	final private Time simTime;
	
	public SubmitCarbonEmissionReport(final Double carbonEmission, final Time simTime){
		this.carbonEmission = carbonEmission;
		this.simTime = simTime;
	}
	
	@Override
	public String toString() {
		return "carbon emission reported= "
				+ this.carbonEmission+ "]";
	}

	public Time getSimTime() {
		return simTime;
	}
	
	public Double getCarbonEmission() {
		return carbonEmission;
	}
	
}
