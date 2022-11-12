/*
 * Created on 05/01/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.pucrio.inf.les.jat.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.pucrio.inf.les.jat.core.exception.AgentCreationException;
import br.pucrio.inf.les.jat.core.exception.AssertAgentFailed;
import jade.core.Specifier;
import jade.util.leap.LinkedList;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import junit.framework.TestCase;

/**
 * @author roberta
 *
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class JadeTestCase extends TestCase {

	private ContainerController environment = null;
	private List<String> agents = null;
	private List<String> startedAgents = null;
	private Map<String, RegisteredAgent> registredAgents = null;

	public JadeTestCase() {
		environment = JADEEnvironmentWrapper.getInstance().getContainerController();
	}

	public void setUp() {
		this.agents = new ArrayList<String>();
		this.startedAgents = new ArrayList<String>();
		this.registredAgents = new HashMap<String, RegisteredAgent>();
	}

	public void tearDown() {
		printReport();
		killAllAgents();
	}

	protected void finalize() throws Throwable {

	}

	public void registerAgent(String name, String className) {
		RegisteredAgent registeredAgent = new RegisteredAgent();
		registeredAgent.setName(name);
		registeredAgent.setClassName(className);

		this.registredAgents.put(name, registeredAgent);
		this.agents.add(name);
	}

	public void registerAndStartAgent(String name, String className, Object[] args) throws AgentCreationException {
		this.registerAgent(name, className);
		this.startedAgents.add(name);
		this.createAndStartAgent(name, className, args);
	}

	public void registerMockAgent(String name, String className) {
		this.agents.add(name);
	}

	public void registerAndStartMockAgent(String name, String className, Object[] args) throws AgentCreationException {
		this.registerMockAgent(name, className);
		this.startedAgents.add(name);
		this.createAndStartAgent(name, className, args);
	}

	protected void createAndStartAgent(String name, String className, Object[] args) throws AgentCreationException {

		LinkedList l = new LinkedList();
		Specifier spec = new Specifier();
		spec.setName(name);
		spec.setClassName(className);
		spec.setArgs(args);

		l.add(spec);
		environment.startAgents(l);
	}

	@Deprecated
	protected void createAndStartAgent(String likeInBoot) throws AgentCreationException {
		char space = ' ';
		Specifier spec;
		try {
			spec = Specifier.parseSpecifier(likeInBoot, space);
			LinkedList l = new LinkedList();
			l.add(spec);
			System.err.println("STARTAR AGENTE: " + spec.getName());
			environment.startAgents(l);
		} catch (Exception e) {
			throw new AgentCreationException(e.getMessage());
		}

	}

	public void assertMockAgent(String agentName) throws AssertAgentFailed {

		AgentController agent = null;

		try {

			agent = getEnvironment().getAgent(agentName);

			if (agent != null) {
				jade.core.Agent aglocal = agent.acquireLocalAgent();
				String res = ((JadeMockAgent) aglocal).getTestResult();

				if (!"OK".equalsIgnoreCase(res)) {
					throw new AssertAgentFailed(res);
				}

				aglocal.doDelete();
			}

		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (ControllerException e) {
			e.printStackTrace();
		}
	}

	public Object getAgentBelief(String name, String belief) {

		AgentController agent = null;
		Object result = null;

		RegisteredAgent registeredAgent = this.registredAgents.get(name);

		try {

			agent = getEnvironment().getAgent(registeredAgent.getName());

			if (agent != null) {
				jade.core.Agent aglocal = agent.acquireLocalAgent();

				Class agentClass = Class.forName(registeredAgent.getClassName());
				Object agentObject = agentClass.cast(aglocal);

				Field beliefField = null;

				try {
					beliefField = agentClass.getField(belief);
				} catch (NoSuchFieldException e) {
					try {
						beliefField = agentClass.getDeclaredField(belief);
					} catch (NoSuchFieldException e1) {
						beliefField = null;
					}
				}

				if (beliefField != null) {
					if (!beliefField.isAccessible()) {
						beliefField.setAccessible(true);
					}
					result = beliefField.get(agentObject);
				}

				aglocal.doDelete();
			}
		} catch (StaleProxyException e) {
			e.printStackTrace();
		} catch (ControllerException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return result;
	}

	public void killAllAgents() {

		for (int x = 0; x < this.agents.size(); x++) {
			String agentName = (String) this.agents.get(x);

			try {
				AgentController agent = getEnvironment().getAgent(agentName);
				agent.releaseLocalAgent();
				agent.kill();
			} catch (ControllerException e) {

			}
		}

		ArrayList killedAgents = new ArrayList();

		while (this.agents.size() > 0) {
			for (int x = 0; x < this.agents.size(); x++) {
				String agentName = (String) this.agents.get(x);

				try {
					AgentController agent = getEnvironment().getAgent(agentName);

					if (agent == null) {
						killedAgents.add(agentName);
					}
				} catch (ControllerException e) {
					killedAgents.add(agentName);
				}
			}

			this.agents.removeAll(killedAgents);
			killedAgents.clear();
		}
	}

	private void printReport() {

		System.out.println("**********************************************************");
		System.out.println("AGENTES REGISTRADOS");
		System.out.println("----------------------------------------------------------");

		for (int x = 0; x < this.agents.size(); x++) {
			System.out.println(this.agents.get(x));
		}

		System.out.println("**********************************************************");
	}

	public ContainerController getEnvironment() {
		return environment;
	}

	private class RegisteredAgent {

		private String name = null;
		private String className = null;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}