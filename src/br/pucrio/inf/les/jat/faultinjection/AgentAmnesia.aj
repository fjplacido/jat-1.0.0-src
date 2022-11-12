package br.pucrio.inf.les.jat.faultinjection;

import java.util.Arrays;

import br.pucrio.inf.les.jat.annotations.ChangeContent;
import br.pucrio.inf.les.jat.annotations.ChangePerformative;
import br.pucrio.inf.les.jat.annotations.Sleep;
import jade.lang.acl.ACLMessage;

/**
 * @author roberta
 * 
 *         Esta classe é responsável por injetar faltas no AUT (Agent Under
 *         Test). As faltas devem ser injetadas de acordo com o modelo de falhas
 *         que definimos para este teste.
 * 
 */

privileged aspect AgentAmnesia {

	pointcut sendMessage(Object b) : call(* Agent.send(..))&&args(b);

	pointcut annotedWithChangePerformative(ChangePerformative cp):@withincode(cp);

	pointcut annotedWithSleep(Sleep s):@withincode(s);

	pointcut annotedWithChangContent(ChangeContent con):@withincode(con);

	void around(Object b, ChangePerformative cp):sendMessage(b)&&annotedWithChangePerformative(cp){

		int performative = cp.performative();

		if (Arrays.asList(ACLMessage.getAllPerformativeNames()).contains(ACLMessage.getPerformative(performative))) {
			ACLMessage message = (ACLMessage) b;
			message.setPerformative(performative);
		}

		proceed(b, cp);
	}

	void around(Object b, Sleep s):sendMessage(b)&&annotedWithSleep(s){

		int time = s.time();

		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	void around(Object b, ChangeContent con):sendMessage(b)&&annotedWithChangContent(con){

		String content = con.content();

		ACLMessage message = (ACLMessage) b;
		message.setContent(content);

		proceed(b, con);
	}

}
