package br.pucrio.inf.les.jat.aspects.synchronizer;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class OrderListHandle extends DefaultHandler {

	private Queue<Action> actions = null;
	private String tag = null;
	private Action action = null;

	public OrderListHandle() {
		this.actions = new LinkedBlockingQueue<Action>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {

		if ("JAT".equals(qName)) {

		} else if ("action".equals(qName)) {
			action = new Action();
		} else if ("agent".equals(qName)) {
			tag = "agent";
		} else if ("performative".equals(qName)) {
			tag = "performative";
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if ("JADEUnit".equals(qName)) {

		} else if ("action".equals(qName)) {
			actions.offer(action);
			action = null;
		} else if ("agent".equals(qName)) {
			tag = null;
		} else if ("performative".equals(qName)) {
			tag = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (tag != null && !tag.equals("")) {
			if ("agent".equals(tag)) {
				action.setAgent(new AID(new String(ch, start, length), AID.ISLOCALNAME));
			} else if ("performative".equals(tag)) {
				action.setPerformative(ACLMessage.getInteger(new String(ch, start, length)));
			}
		}
	}

	public Queue<Action> getActions() {
		return actions;
	}
}
