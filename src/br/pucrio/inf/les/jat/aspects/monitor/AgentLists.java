package br.pucrio.inf.les.jat.aspects.monitor;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import jade.core.Agent;

/**
 * 
 * This class handles the life cicle of agents.
 * 
 */

public class AgentLists {

	private static AgentLists agentLists;

	/**
	 * agents executing their run methods.
	 */
	private Set runningAgents = new LinkedHashSet();

	/**
	 * Keeps all agents that have started, but are not running yet.
	 */
	private Set startedAgents = new LinkedHashSet();

	/**
	 * agents waiting on a given object.
	 */
	private Set testIsDoneAgents = new LinkedHashSet();

	/**
	 * agents premature died.
	 */
	private Set prematureDiedAgents = new LinkedHashSet();

	/**
	 * testCase timeOut
	 */
	private long timeOut = System.currentTimeMillis() + 600000;

	private AgentLists() {

	}

	public static AgentLists getInstance() {

		if (agentLists == null) {
			agentLists = new AgentLists();
		}

		return agentLists;
	}

	/**
	 * Include in running Agents a given agent.
	 * 
	 * @param t The agent to be included.
	 */
	public synchronized void includeInTestIsDoneAgents(String t) {
		boolean included = testIsDoneAgents.add(t);
	}

	/**
	 * Include in running Agents a given Agent.
	 * 
	 * @param t The Agent to be included.
	 */
	public synchronized void includeInRunningAgents(String t) {
		boolean included = runningAgents.add(t);
		this.startedAgents.remove(t);// removes from started Agents

	}

	/**
	 * Include in running Agents a given Agent.
	 * 
	 * @param t The Agent to be included.
	 */
	public synchronized void includeInStartedAgents(String t) {
		boolean included = startedAgents.add(t);
	}

	/**
	 * Include in premature died Agents a given Agent.
	 * 
	 * @param t The Agent to be included.
	 */
	public synchronized void includeInPrematureDiedAgents(String t) {
		boolean included = startedAgents.add(t);
	}

	/**
	 * Removes from running Agents a given Agent
	 * 
	 * @param t The Agent to be removed.
	 */
	private synchronized void removeFromRunningAgents(String ag) {
		boolean removed = runningAgents.remove(ag);
	}

	/**
	 * Removes a runnable Agent from the list.
	 */
	public synchronized void removeRunnableAgent(String agName) {
		removeFromRunningAgents(agName);
	}

	/**
	 * set testCase timeOut
	 */
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * This method makes the classe which calls it wait untill the agent ID passed
	 * as parameter, is included at the testIsDone list which means that the mock
	 * agent finished the execution of the tests
	 * 
	 * @param agentKey receives the JADE agent ID.
	 */

	public synchronized void waitUntilTestHasFinished(String agentKey) {

		while (!this.testIsDoneAgents.contains(agentKey) && (timeOut > System.currentTimeMillis())) {
			try {
				wait(20000);
			} catch (InterruptedException e) {
				// TODO Loging:: System.out.println("Agent interrupted " + e);
			}
		}
		// TODO Loging:: System.out.println("Agent stops running:" + agentKey);
	}

	/**
	 * Verifies if a given Agent is running (it has started its run method and has
	 * not leaved it. Notice that it may also be waiting).
	 * 
	 * @param t The wanted Agent
	 * @return true if t is running.
	 */
	public synchronized boolean isAgentRunning(Agent t) {
		return (this.runningAgents.contains(t));
	}

	/**
	 * Verifies if a given Agent is started, but not running yet
	 * 
	 * @param t The wanted Agent
	 * @return true if t is started, but not running yet.
	 */
	public synchronized boolean isAgentStarted(Agent t) {
		return (this.startedAgents.contains(t));
	}

	public synchronized boolean hasPrematureDiedAgents() {
		return !this.prematureDiedAgents.isEmpty();
	}

	/**
	 * Prints all Agents running, started and waiting.
	 *
	 */
	public void printAllAgents() {
		System.out.println("Runnig Agents: ");
		printAgents(runningAgents);
		System.out.println("Started Agents: ");
		printAgents(startedAgents);
		System.out.println("Test Is Done Agents: ");
		printAgents(this.testIsDoneAgents);
	}

	private synchronized void printAgents(Set list) {
		Iterator t = list.iterator();
		while (t.hasNext()) {
			System.out.println("=>Agent:" + t.next());
		}
	}

	public void printAllPrematureDiedAgents() {
		Iterator t = prematureDiedAgents.iterator();

		while (t.hasNext()) {
			System.out.println(t.next());
		}
	}

	public void printStartedAgents() {
		Iterator t = startedAgents.iterator();

		while (t.hasNext()) {
			System.out.println(t.next());
		}
	}

	/**
	 * Clear All Lists
	 */
	public void clearAllLists() {
		this.runningAgents.clear();
		this.startedAgents.clear();
		this.testIsDoneAgents.clear();
	}
}
