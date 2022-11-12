
package br.pucrio.inf.les.jat.core.exception;

/**
 * @author roberta Excecao lancada quando acontece algum problema durante a
 *         criacao de um agente na plataforma JADE.
 * 
 */
public class AgentCreationException extends RuntimeException {

	public AgentCreationException(String msg) {
		super(msg);
	}
}
