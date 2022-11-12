package br.pucrio.inf.les.jat.core.fipaprotocols;

import br.pucrio.inf.les.jat.core.TestReporter;
import br.pucrio.inf.les.jat.core.exception.InvalidProposalException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * @author roberta
 * 
 *         Esta classe desempenha o papel de um agente que inicia o protocolo de
 *         interacao FIPA chamado Contract Net.
 *
 */

public abstract class FIPAContractNetInitiator extends Agent implements TestReporter {
	// The title of the book to buy
	protected String cfpId;
	// The list of known seller agents
	protected AID[] participants;
	protected String testResult = "OK";
	protected long cfp_interval = 0;

	public void setTestResult(String resultado) {
		testResult = resultado;
	}

	public String getTestResult() {
		return testResult;
	}

	protected abstract void findParticipants();

	protected abstract Proposal createProposal(String messageContent) throws InvalidProposalException;

	protected void setup() {
		super.setup();

		System.out.println("--------------------- ENTROU NO SETUP DO FIPA----------------");

		Object[] args = getArguments();

		if (args != null && args.length >= 2) {

			System.out.println("--------------------- ARGUMENTOS >= 2 ----------------");

			cfpId = (String) args[0];
			System.out.println("CFP is " + cfpId);
			cfp_interval = Long.parseLong((String) args[1]);

			System.out.println("--------------------- VOU PROCURAR PARTICIPANTES ----------------");

			findParticipants();

			System.out.println("--------------------- VOU ADICIONAR UM COMPORTAMENTO ----------------");

			addBehaviour(new RequestPerformer());

		} else {
			// Make the agent terminate
			System.out.println("The CFP or the CFP timeout was not defined!");
			doDelete();
		}
	}

	protected void takeDown() {
		System.out.println("CFP initiator " + getAID().getName() + " terminating.");
	}

	/**
	 * Inner class RequestPerformer. This is the behaviour used by Book-buyer agents
	 * to request seller agents the target book.
	 */
	private class RequestPerformer extends OneShotBehaviour {

		private AID bestProposalAgent; // The agent who provides the best offer
		private Proposal bestProposal; // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
		private String conversationID = "cfp-protocol";

		public void action() {

			System.out.println("--------------------- COMPORTAMENTO ADICIONADO ----------------");

			System.out.println("Vou fazer request.");

			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

			for (int i = 0; i < participants.length; ++i) {
				cfp.addReceiver(participants[i]);
			}

			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@tamanho da lista de participantes::: "
					+ participants.length + " ----------------");

			cfp.setContent(cfpId);
			cfp.setConversationId(conversationID);
			cfp.setReplyWith("cfp" + System.currentTimeMillis());
			myAgent.send(cfp);

			System.out.println("Envia proposta.");
			mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
					MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));

			ACLMessage reply = myAgent.blockingReceive(mt, cfp_interval);
			System.out.println("espera resposta proposta.");
			if (reply != null) {
				System.out.println("Resposta != null.");
				// Reply received
				if (reply.getPerformative() == ACLMessage.PROPOSE) {
					System.out.println("ACLMessage.PROPOSE");
					// This is an offer
					try {
						Proposal current = createProposal(reply.getContent());
						bestProposal = current;
						bestProposalAgent = reply.getSender();
						System.out.println("Best Proposal: " + bestProposal);

					} catch (InvalidProposalException ee) {
						ee.printStackTrace();
						System.out.println("ERROR CONDITION: 1");
						setTestResult(ee.getMessage());
						return;
					}
					System.out.println("ACLMessage.PROPOSE saida");
				} else {
					System.out.println("ERROR CONDITION: 2");
					setTestResult("reply.getPerformative() != ACLMessage.PROPOSE");
					return;
				}
			} else {
				try {
					Integer.parseInt("e3");
				} catch (NumberFormatException ee) {
					System.out.println("ERROR CONDITION: 3");
					setTestResult("reply == null. Message: " + ee.getMessage());
				}
				return;
			}

			// Send the purchase order to the seller that provided the best offer
			System.out.println("Envia aceita proposta.");

			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			order.addReceiver(bestProposalAgent);
			order.setContent(cfpId);
			order.setConversationId(conversationID);
			order.setReplyWith("order" + System.currentTimeMillis());
			myAgent.send(order);
			// Prepare the template to get the purchase order reply
			mt = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
					MessageTemplate.MatchInReplyTo(order.getReplyWith()));

			System.out.println("-------------------\n\n\n" + order.getReplyWith() + "\n\n ---------------");
			// Receive the purchase order reply
			System.out.println("Espera resposta proposta.");
			reply = myAgent.blockingReceive(mt, cfp_interval);

			if (reply != null) {
				// Purchase order reply received
				if (reply.getPerformative() == ACLMessage.INFORM) {
					// Purchase successful. We can terminate
					System.out.println(cfp + " successfully purchased from agent " + reply.getSender().getName());
					System.out.println("Price = " + bestProposal);

				} else {
					System.out.println("ERROR CONDITION: 4");
					setTestResult("reply.getPerformative() != ACLMessage.INFORM");
					return;
				}

			} else {
				System.out.println("ERROR CONDITION: 5");
				setTestResult("reply for ACLMessage.ACCEPT_PROPOSAL == null");
				return;
			}

			setTestResult("OK");

		}// End action

	} // End of inner class RequestPerformer

}// End of agent.
