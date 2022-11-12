package br.pucrio.inf.les.jat.aspects.synchronizer;

import jade.core.AID;

public class Action {

	private AID agent = null;
	private int performative;
	private AID reciever = null;
	private String content = null;

	public Action() {

	}

	public AID getAgent() {
		return agent;
	}

	public void setAgent(AID agent) {
		this.agent = agent;
	}

	public int getPerformative() {
		return performative;
	}

	public void setPerformative(int performative) {
		this.performative = performative;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public AID getReciever() {
		return reciever;
	}

	public void setReciever(AID reciever) {
		this.reciever = reciever;
	}
}
