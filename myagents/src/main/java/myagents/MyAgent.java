package myagents;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import services.ParticipantCarbonReportingService;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import actions.SubmitCarbonEmissionReport;

public class MyAgent extends AbstractParticipant {

	Location loc;
	//Test commit
	/*
	 * To be added to kyoto project*/
	private double carbonEmission = 10.0;

	private Map<Integer, Double> carbonEmissionReports;
	
	ParticipantCarbonReportingService reportingService;
	/*=============================*/
	
	ParticipantLocationService locationService;
	
	public MyAgent(UUID id, String name, Location loc){
		super(id,name);
		this.loc = loc;
		this.carbonEmissionReports = new HashMap<Integer, Double>();
	}
	
	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState(){
		Set<ParticipantSharedState> s = super.getSharedState();
		s.add(ParticipantLocationService.createSharedState(getID(), loc));
		
		s.add(ParticipantCarbonReportingService.createSharedState(this.getCarbonEmissionReports(), this.getID()));
		//s.add(new ParticipantSharedState("Report", 
	    //        (Serializable) this.getCarbonEmissionReports(), getID()));
		return s;
	}
	
	public Map<Integer,Double> getCarbonEmissionReports(){
		return this.carbonEmissionReports;
	}
	
	/**
	 * Private setter function for personal reports
	 * @param simTime
	 * @param emission
	 * @return
	 */
	private Map<Integer,Double> addToReports(Time simTime, Double emission){
		this.carbonEmissionReports.put(simTime.intValue(), emission);
		return this.carbonEmissionReports;
	}
	
	/**
	 * Report the carbonEmissions. This function internally
	 * updates the report already owned by the agent after
	 * calculating the carbon emission that the agent wants
	 * to report to the environment
	 * @param t: Simulation time at which report submission was made
	 * @return
	 */
	public Double reportCarbonEmission(Time t){
		//TODO add code to calculate whether to submit true or false report (cheat)
		//Once calculations done, update the report owned by this agent
		carbonEmission++; //Default code now just increments it
		this.addToReports(t, carbonEmission);
		return new Double(carbonEmission);
	}
	
	@Override
	public void initialise(){
		super.initialise();
		try{
			this.locationService = this.getEnvironmentService(ParticipantLocationService.class);
			
			/*Need to add this to kyoto*/
			this.reportingService = this.getEnvironmentService(ParticipantCarbonReportingService.class);
		}catch(UnavailableServiceException e){
			logger.warn(e);
		}
		
	}
	
	@Override
	public void execute(){
		/*this.loc = locationService.getAgentLocation(this.getID());
		logger.info("My Location is: " + this.loc);
		
		int dx = Random.randomInt(2) - 1;
		int dy = Random.randomInt(2) - 1;
		Move move = new Move(dx,dy);
		
		try{
			this.environment.act(move, this.getID(), this.authkey);
		}catch(ActionHandlingException e){
			logger.warn("Error tyring to move", e);
		}
		
		Location loca = this.locationService.getAgentLocation(getID());
		logger.info("nearby location is" + loca.toString() + " Distance to: "+ loca.distanceTo(loc));*/
		
		//Test for submitting reports
		try{
			Time t = SimTime.get();
			this.environment.act(new SubmitCarbonEmissionReport(this.reportCarbonEmission(t), t), this.getID(), this.authkey);
		}catch(ActionHandlingException e){
			logger.warn("Error trying to submit report");
		}
		
		
		
		/*Debugging purposes*/
		/*for(NetworkAddress net : this.network.getConnectedNodes()){
			Map<Integer,Double> reports = this.reportingService.getReportFor(net.getId());
			logger.info("Agent reporting: " + this.getID() + " name: " + this.getName());
			if(this.getName().equals("agent0")){
				for(Integer key : reports.keySet()){
					logger.info(" Key: " + key + " Value: " + reports.get(key) + "\n");
				}
			}
		}*/
		
	}

}
