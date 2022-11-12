package br.pucrio.inf.les.jat.core.exception;

/**
 * @author roberta
 * 
 *         Excecao lançada quando um probleba ocorre no momento da recepcao de
 *         uma mensagem.
 */
public class ReplyReceptionFailed extends Exception {

	public ReplyReceptionFailed(String msg) {
		super(msg);
	}
}
