package br.pucrio.inf.les.jat.core.exception;

/**
 * @author roberta Excecao lancada quando a proposta enviada por um participande
 *         to FIPA Contract Net Protocol é invalida.
 * 
 */

public class InvalidProposalException extends Exception {

	public InvalidProposalException(String msg) {
		super(msg);
	}

	public InvalidProposalException() {
		super();
	}
}
