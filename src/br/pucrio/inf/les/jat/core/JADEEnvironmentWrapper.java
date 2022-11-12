package br.pucrio.inf.les.jat.core;

import jade.Boot;
import jade.wrapper.ContainerController;

public class JADEEnvironmentWrapper {

	private static JADEEnvironmentWrapper wrapper = null;
	private ContainerController environment = null;
	private Boot b = null;

	public static JADEEnvironmentWrapper getInstance() {
		if (wrapper == null) {
			wrapper = new JADEEnvironmentWrapper();
			wrapper.init();
		}
		return wrapper;
	}

	private void init() {
		String[] argteste = new String[0];
		Boot b = new Boot(argteste);
		environment = b.getContainerController();
	}

	public ContainerController getContainerController() {
		return environment;
	}

}
