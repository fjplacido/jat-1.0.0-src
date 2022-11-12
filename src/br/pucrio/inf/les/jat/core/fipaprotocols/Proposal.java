package br.pucrio.inf.les.jat.core.fipaprotocols;

import br.pucrio.inf.les.jat.core.exception.InvalidProposalException;

/**
 *
 * Esta classe defini uma proposta generica que será negociada entre os
 * participantes do protocolo Contract Net. Os participantes poderao estender
 * esta classe, de forma a obter uma Proposta mais especifica.
 * 
 * @author roberta
 *
 */
public class Proposal {

	public static int PARAM_IS_BETTER = 1;
	public static int EQUAl = 0;
	public static int PARAM_IS_WORSE = -1;

	public void setContent(String content) throws InvalidProposalException {
		System.out.print("inplementacao vazia");
	}

	public int compare(Proposal other) {
		return 0;
	}

	public String toString() {
		return this.toString();
	}

}
