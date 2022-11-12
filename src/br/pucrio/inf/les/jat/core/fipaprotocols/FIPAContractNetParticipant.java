
package br.pucrio.inf.les.jat.core.fipaprotocols;

import br.pucrio.inf.les.jat.core.TestReporter;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * @author roberta
 * 
 *         Este Agente desempenha o papel de um agente participante do protocolo
 *         de interacao FIPA chamado Contract Net.
 * 
 */

public abstract class FIPAContractNetParticipant extends Agent implements TestReporter {

	protected String testResult = "OK";
	protected long cfp_interval = 0;

	public void setTestResult(String resultado) {
		testResult = resultado;
	}

	public String getTestResult() {
		return testResult;
	}

	protected void setup() {
		super.setup();
		addBehaviour(new ParticipantBehaviour());
	}

	protected void takeDown() {
		System.out.println("CFP initiator " + getAID().getName() + " terminating.");
	}

	/**
	 * Inner class RequestPerformer. This is the behaviour used by Book-buyer agents
	 * to request seller agents the target book.
	 */

	public abstract String getProposal(String cfp_info);

	public abstract boolean removeFromStock(String element_key);

	private class ParticipantBehaviour extends OneShotBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.blockingReceive(mt, cfp_interval);

			if (msg != null) {
				// CFP Message received. Process it
				String cfp_info = msg.getContent();

				ACLMessage reply = msg.createReply();
				String proposal = getProposal(cfp_info);

				if (proposal != null) {
					// The requested book is available for sale. Reply with the price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(proposal);
				} else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);

				// Espera a resposta
				mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				msg = myAgent.blockingReceive(mt, cfp_interval);

				if (msg != null) {

					String key = msg.getContent();
					boolean succeed = removeFromStock(key);
					reply = msg.createReply();

					if (succeed) {
						reply.setPerformative(ACLMessage.INFORM);
						reply.setContent("done");
						System.out.println(" Negotiation Done ");
					} else {
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("not-available");
					}
					myAgent.send(reply);

				} // fim if msg null

			} // fim if msg null

		}// End action

	} // End of inner class RequestPerformer

}// End of agent.
