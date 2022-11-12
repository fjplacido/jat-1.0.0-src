package br.pucrio.inf.les.jat.aspects.monitor;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 * @author Roberta Coelho Este aspecto é responsável por monitorar o ciclo de
 *         vida dos agentes armazenando informacoes sobre o ciclo de vida em um
 *         conjunto de 3 listas.
 * 
 */
privileged public aspect AgentMonitor{

private AgentLists agLists=AgentLists.getInstance();

pointcut AgentCreationCalls():call(public AgentController ContainerController.createNewAgent(..));

pointcut AgentRunExecutions(Agent o):execution(protected void Agent.setup())&&target(o);

pointcut finalizeAgentExecutions(Agent o):execution(public void Agent.doDelete())&&target(o);

pointcut agentFinishTests(Agent o):(execution(public void br.pucrio.inf.les.jat.core.TestReporter.setTestResult(..))||execution(public void jade.core.Agent+.setBelief(..)))&&target(o);

pointcut prematureDiedAgent(Agent o,boolean ok):execution(public void Agent.clean(boolean))&&target(o)&&args(ok);

before():AgentCreationCalls(){Object args[]=thisJoinPoint.getArgs();String agentName="";if(args.length>0){agentName=args[0].toString();agLists.includeInStartedAgents(agentName);}}

before(Agent o):AgentRunExecutions(o){String nome=o.getLocalName();agLists.includeInRunningAgents(nome);}

after(Agent o):finalizeAgentExecutions(o){String nome=o.getLocalName();agLists.removeRunnableAgent(nome);}

after(Agent o):agentFinishTests(o){String nome=o.getLocalName();agLists.includeInTestIsDoneAgents(nome);}

before(Agent o,boolean ok):prematureDiedAgent(o,ok){if(!ok){String name=o.getLocalName();agLists.includeInPrematureDiedAgents(name);}}

void around(String t):execution(public static void br.pucrio.inf.les.jat.aspects.monitor.AgentMonitorServices.waitUntilTestHasFinished(String))&&args(t){agLists.waitUntilTestHasFinished(t);}

void around(long o):execution(public static void br.pucrio.inf.les.jat.aspects.monitor.AgentMonitorServices.setTimeOut(long))&&args(o){agLists.setTimeOut(System.currentTimeMillis()+o);}

pointcut finishTest(br.pucrio.inf.les.jat.core.JadeTestCase testCase):execution(public void br.pucrio.inf.les.jat.core.JadeTestCase.tearDown())&&target(testCase);

pointcut beginTest(br.pucrio.inf.les.jat.core.JadeTestCase testCase):execution(public void br.pucrio.inf.les.jat.core.JadeTestCase.setUp())&&target(testCase);

before(br.pucrio.inf.les.jat.core.JadeTestCase testCase):finishTest(testCase){

}

after(br.pucrio.inf.les.jat.core.JadeTestCase testCase):finishTest(testCase){

agLists.clearAllLists();

System.out.println("**********************************************************");System.out.println("TESTE CASE FINALIZADO -> "+testCase.getName());System.out.println("**********************************************************");}

after(br.pucrio.inf.les.jat.core.JadeTestCase testCase):beginTest(testCase){agLists.setTimeOut(System.currentTimeMillis()+600000);

System.out.println("**********************************************************");System.out.println("TESTE CASE INICIADO -> "+testCase.getName());System.out.println("**********************************************************");

// TODO Load the sincronizer file

}}