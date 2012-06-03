package myagents;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import simpleprotocol.SimpleProtocol;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
import uk.ac.imperial.presage2.util.protocols.Protocol;

public class MyAgent extends AbstractParticipant {

	Logger logger = Logger.getLogger(MyAgent.class);
	
	Location loc;
	
	ParticipantLocationService locationService;
	
	private Protocol simpleProtocol;
	
	public MyAgent(UUID id, String name, Location loc){
		super(id,name);
		this.loc = loc;
	}
	
	@Override
	protected void processInput(Input in) {
		if(this.simpleProtocol.canHandle(in)){
			this.simpleProtocol.handle(in);
		}
	}
	
	@Override
	protected Set<ParticipantSharedState> getSharedState(){
		Set<ParticipantSharedState> s = super.getSharedState();
		s.add(ParticipantLocationService.createSharedState(getID(), loc));
		return s;
	}
	
	public static enum Replies{
		GOOD_REPLY,
		BAD_REPLY
	};
	
	public static final List<Replies> replies = Collections.unmodifiableList(Arrays.asList(Replies.values()));
	public static final int SIZE = replies.size();
	public final Random r = new Random();
	
	@Override
	public void initialise(){
		super.initialise();
		
		this.simpleProtocol = new SimpleProtocol("SIMPLE",
			this.network, this.authkey, this.getID(), this.environment){

				@Override
				public Replies getReply() {
					return MyAgent.replies.get(MyAgent.this.r.nextInt(SIZE));
				}
			
		};		
	}
	
	@Override
	public void execute(){
		super.execute();
		
		//Put tests in here
		if(this.simpleProtocol != null){
			simpleProtocol.incrementTime();
		}		
		Set<NetworkAddress> activeConversations = this.simpleProtocol.getActiveConversationMembers();
		
		logger.info("Number of participants in the conversation with: " + this.getName()
					+ " is " +activeConversations.size());
		
			try {
				Set<NetworkAddress> alreadyTalkingTo = this.simpleProtocol
						.getActiveConversationMembers();
				for (NetworkAddress a : this.network.getConnectedNodes()) {
					logger.info(this.getName() + " is connected to: " + a);
					// spawn a conversation if I'm not already taking them them
					// (limit 5 convs)
					if (!alreadyTalkingTo.contains(a)) {
						this.simpleProtocol.spawn(a);
					}
				}
			} catch (UnsupportedOperationException e) {
				logger.warn("Can't get connected nodes", e);
			}			
		
	}

}
