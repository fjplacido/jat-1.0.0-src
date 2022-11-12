
package br.pucrio.inf.les.jat.aspects.monitor;

/**
 * Class that presents fake methods, that will be replaced by correct ones using
 * an aspect AgentMonitor.
 *
 */

public class AgentMonitorServices {

	public static void waitUntilTestHasFinished(String agentKey) {
		System.out.println("A classe foi compilada sem aspectos! ");
	}

	public static void setTimeOut(long time) {
		System.out.println("A classe foi compilada sem aspectos! ");
	}
}
