/*
 * Created on 06/01/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.pucrio.inf.les.jat.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

import br.pucrio.inf.les.jat.core.exception.AUTFaildException;
import br.pucrio.inf.les.jat.core.exception.ReplyReceptionFailed;
/**
 * A set of assert methods.  Messages are only displayed when an assert fails.
 */
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * @author roberta
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class JadeMockAgent extends Agent implements TestReporter {

	private String testResult = "OK";

	public final static Assert asserts = new Assert();

	public void sendMessage(int performative, AID receiver, String content) {
		ACLMessage cfp = new ACLMessage(performative);
		cfp.addReceiver(receiver);
		cfp.setContent(content);

		send(cfp);
	}

	public void sendMessage(int performative, AID receiver, String conversationID,
			Map<String, String> userDefinedParameter, String content) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setConversationId(conversationID);

		for (String key : userDefinedParameter.keySet()) {
			msg.addUserDefinedParameter(key, userDefinedParameter.get(key));
		}

		ACLMessage cfp = new ACLMessage(performative);
		cfp.addReceiver(receiver);
		cfp.setContent(content);

		send(cfp);
	}

	public ACLMessage receiveMessage(Integer... performative) throws ReplyReceptionFailed {

		ACLMessage msg = blockingReceive();

		if (msg == null) {
			throw new ReplyReceptionFailed("Message is null.");
		} else if (!Arrays.asList(performative).contains(msg.getPerformative())) {
			StringBuffer msgErro = new StringBuffer();
			msgErro.append("Performative expected:<");

			for (int x = 0; x < performative.length - 1; x++) {
				msgErro.append(ACLMessage.getPerformative(performative[x]));
				msgErro.append(",");
			}

			msgErro.append(ACLMessage.getPerformative(performative[performative.length - 1]));
			msgErro.append("> but was: <");
			msgErro.append(ACLMessage.getPerformative(msg.getPerformative()));
			msgErro.append(">");
			msgErro.append(" - ");
			msgErro.append(msg.getSender().getName());

			throw new ReplyReceptionFailed(msgErro.toString());
		}

		return msg;
	}

	// TODO Criar um blockReceiveMessage(long timeout,MessageTemplate template)
	// throws ReplyReceptionFalied

	public ACLMessage blockReceiveMessage(long timeout, Integer... performative) throws ReplyReceptionFailed {

		ACLMessage msg = blockingReceive(timeout);

		if (msg == null) {
			throw new ReplyReceptionFailed("Time out.");
		}

		if (!Arrays.asList(performative).contains(msg.getPerformative())) {
			StringBuffer msgErro = new StringBuffer();
			msgErro.append("Performative expected:<");

			for (int x = 0; x < performative.length - 1; x++) {
				msgErro.append(ACLMessage.getPerformative(performative[x]));
				msgErro.append(",");
			}

			msgErro.append(ACLMessage.getPerformative(performative[performative.length - 1]));
			msgErro.append("> but was: <");
			msgErro.append(ACLMessage.getPerformative(msg.getPerformative()));
			msgErro.append(">");
			msgErro.append(" - ");
			msgErro.append(msg.getSender().getLocalName());

			throw new ReplyReceptionFailed(msgErro.toString());
		}

		return msg;
	}

	public ACLMessage blockReceiveMessage(long timeout) throws ReplyReceptionFailed {

		ACLMessage msg = blockingReceive(timeout);

		if (msg == null) {
			throw new ReplyReceptionFailed("Message is null.");
		}

		return msg;
	}

	public String prepareMessageResult(Throwable e) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		e.printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		return buffer.toString();
	}

	public void setTestResult(String resultado) {
		if (resultado.equals("OK")) {
			testResult = resultado;
		} else {
			try {
				throw new AUTFaildException(resultado);
			} catch (AUTFaildException aut) {
				testResult = prepareMessageResult(aut);
			}
		}
	}

	public String getTestResult() {
		return testResult;
	}

}
