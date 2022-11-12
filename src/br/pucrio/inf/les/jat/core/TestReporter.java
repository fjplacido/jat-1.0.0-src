package br.pucrio.inf.les.jat.core;

/**
 * Interface que deve ser implementada por todos os MockAgents que permite que o
 * resultado do teste seja retornado para o test case.
 * 
 * @author roberta
 *
 */

public interface TestReporter {
	public String getTestResult();

	public void setTestResult(String msg);
}
