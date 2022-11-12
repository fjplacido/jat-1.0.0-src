package br.pucrio.inf.les.jat.aspects.synchronizer;

import java.io.File;
import java.net.MalformedURLException;

public aspect Synchronizer {

	private OrderList orderList;
	private boolean sincronize = false;

	pointcut MockSendMessage(br.pucrio.inf.les.jat.core.JadeSynchronizedMockAgent agent,jade.lang.acl.ACLMessage message) : call(public final void br.pucrio.inf.les.jat.core.JadeSynchronizedMockAgent+.send(..)) && this(agent) && args(message);

	pointcut beginTest(br.pucrio.inf.les.jat.core.JadeTestCase testCase): execution (public void br.pucrio.inf.les.jat.core.JadeTestCase.setUp()) && target(testCase);

	before(br.pucrio.inf.les.jat.core.JadeTestCase testCase) : beginTest(testCase) {
		String fileName = testCase.getName() + "SincronizerConfig.xml";
		File file = new File(fileName);

		if ( file.exists() ) {
			try {
				this.orderList = OrderList.getInstance(file.toURL());
				this.sincronize = true;
				System.out.println("*********************************************************");
				System.out.println("* Syncronize File: " + fileName + " was loaded successful");
				System.out.println("*********************************************************");				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}			
	}

	before(br.pucrio.inf.les.jat.core.JadeSynchronizedMockAgent agent,jade.lang.acl.ACLMessage message) : MockSendMessage(agent,message) {
		if ( sincronize ) {
			sendMessage(agent,message);
		}	
	}
	
	private void sendMessage(br.pucrio.inf.les.jat.core.JadeSynchronizedMockAgent agent,jade.lang.acl.ACLMessage message) {
		while ( !orderList.checkTurn(agent.getAID(), message.getPerformative()) ) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}