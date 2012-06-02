/**
 * 
 */
package helloprotocol;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.UnicastMessage;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.Action;
import uk.ac.imperial.presage2.util.fsm.AndCondition;
import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
import uk.ac.imperial.presage2.util.fsm.FSM;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.fsm.StateType;
import uk.ac.imperial.presage2.util.fsm.Transition;
import uk.ac.imperial.presage2.util.protocols.ConversationCondition;
import uk.ac.imperial.presage2.util.protocols.ConversationSpawnEvent;
import uk.ac.imperial.presage2.util.protocols.FSMConversation;
import uk.ac.imperial.presage2.util.protocols.FSMProtocol;
import uk.ac.imperial.presage2.util.protocols.InitialiseConversationAction;
import uk.ac.imperial.presage2.util.protocols.MessageAction;
import uk.ac.imperial.presage2.util.protocols.MessageTypeCondition;
import uk.ac.imperial.presage2.util.protocols.SpawnAction;
import uk.ac.imperial.presage2.util.protocols.TimeoutCondition;

/**
 * @author farhanrahman
 *
 */
public class SimpleProtocol extends FSMProtocol {

	Logger logger = Logger.getLogger(SimpleProtocol.class);
	
	private final UUID authKey;
	private final UUID participantID;
	
	private final EnvironmentConnector environment;
	
	/**
	 * @param name
	 * @param description
	 * @param network
	 */
	
	public enum States{
		START,
		WAIT_FOR_REPLY,
		DONE,
		MESSAGE_RECEIVED,
		TIMED_OUT
	};
	
	public enum Transitions{
		SEND_MESSAGE,
		REPLY,
		GET_REPLY,
		TIMEOUT
	};
	
	public SimpleProtocol(String name,
			NetworkAdaptor network, UUID aKey, UUID pID, EnvironmentConnector environment) {
		super(name, FSM.description(), network);
		this.authKey = aKey;
		this.participantID = pID;
		this.environment = environment;
		
		try {
			this.description.addState(States.START, StateType.START)
							.addState(States.WAIT_FOR_REPLY)
							.addState(States.DONE,StateType.END)
							.addState(States.MESSAGE_RECEIVED, StateType.END)
							.addState(States.TIMED_OUT, StateType.END);
			
			//Initiator transition
			this.description.addTransition(
						Transitions.SEND_MESSAGE, 
						new EventTypeCondition(ConversationSpawnEvent.class), 
						States.START, 
						States.WAIT_FOR_REPLY,
						new SpawnAction(){

							@Override
							public void processSpawn(
									ConversationSpawnEvent event,
									FSMConversation conv, Transition transition) {
									conv.getNetwork().sendMessage(
											new UnicastMessage<Object>(
													Performative.PROPOSE, 
													"MESSAGE", 
													SimTime.get(), 
													conv.getNetwork().getAddress(), 
													conv.recipients.get(0)
													)
											);
								
							}
				
			}).addTransition(
						Transitions.GET_REPLY, 
						new AndCondition(new MessageTypeCondition("REPLY"), new ConversationCondition()), 
						/*Conversation conditiion makes sure that the ID that the conversation was initialised with
						 * remains consistent and the MessageTypeCondition checks whether the mssage given back is
						 * the string literal "REPLY"*/
						States.WAIT_FOR_REPLY, 
						States.DONE, 
						new MessageAction(){

							@Override
							public void processMessage(Message<?> message,
									FSMConversation conv, Transition transition) {
									logger.info("Got REPLY message for agent with ID" + SimpleProtocol.this.getParticipantID());
							}
							
						}).addTransition(
								Transitions.TIMEOUT, 
								new TimeoutCondition(4), 
								States.WAIT_FOR_REPLY, 
								States.TIMED_OUT, 
								new Action(){

									@Override
									public void execute(Object event,
											Object entity, Transition transition) {
											logger.info("Timed out");
										
									}
									
								}
						);
			
			//Replier transitions
			this.description.addTransition(
					Transitions.REPLY, 
					new MessageTypeCondition("MESSAGE"), 
					States.START, 
					States.MESSAGE_RECEIVED, 
					new InitialiseConversationAction(){
						@Override
						public void processInitialMessage(Message<?> message,
								FSMConversation conv, Transition transition) {
								conv.getNetwork().sendMessage(
										new UnicastMessage<Object>(
												Performative.AGREE,
												"REPLY",
												SimTime.get(),
												conv.getNetwork().getAddress(),
												message.getFrom())
										);
							
						}
						
					});
			
		} catch (FSMException e) {
			logger.warn("Error in trying to instantiate the Hello Protocol Class");
			logger.warn(e.getMessage());
		}
		
	}

	public UUID getAuthKey() {
		return authKey;
	}

	public UUID getParticipantID() {
		return participantID;
	}

	public EnvironmentConnector getEnvironment() {
		return environment;
	}

}
