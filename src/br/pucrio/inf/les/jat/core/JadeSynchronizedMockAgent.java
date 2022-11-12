package br.pucrio.inf.les.jat.core;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class JadeSynchronizedMockAgent extends JadeMockAgent {

	public void sendMessage(int performative, AID receiver, String content) {
		ACLMessage msg = new ACLMessage(performative);
		msg.setSender(getAID());
		msg.addReceiver(receiver);
		msg.setContent(content);

		send(msg);
	}
}
