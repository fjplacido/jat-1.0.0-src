package br.pucrio.inf.les.jat.core;

import jade.wrapper.ContainerController;
import junit.extensions.TestSetup;
import junit.framework.Test;

/**
 * 
 * @author roberta
 *
 *         Esta classe ainda nao está sendo utilizada. A ideia é usarmos um test
 *         setup para inicializar e finalizar o container JADE no final da
 *         execucao de todos os testes
 * 
 */

public class JadeTestSetup extends TestSetup {

	public JadeTestSetup(Test test) {
		super(test);
	}

	protected void setUp() throws Exception {
		JADEEnvironmentWrapper wrapper = JADEEnvironmentWrapper.getInstance();
	}

	protected void shutDown() throws Exception {
		ContainerController container = JADEEnvironmentWrapper.getInstance().getContainerController();
		container.shutDown();
	}

}
