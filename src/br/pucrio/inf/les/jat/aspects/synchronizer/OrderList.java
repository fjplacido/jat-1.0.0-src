package br.pucrio.inf.les.jat.aspects.synchronizer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.SAXException;

//import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;

import jade.core.AID;

public class OrderList {

	private URL arquivo;
	private static OrderList instance;
	private Queue<Action> actions = null;

	private OrderList(URL arquivo) {
		this.arquivo = arquivo;
		this.actions = new LinkedBlockingQueue<Action>();
		this.ler();
	}

	public synchronized static OrderList getInstance(URL arquivo) {

		if (instance == null) {
			instance = new OrderList(arquivo);
		}

		return instance;
	}

	private void ler() {

		try {
			SAXParserFactory factory = SAXParserFactoryImpl.newInstance();
			SAXParser parser = factory.newSAXParser();
			OrderListHandle handler = new OrderListHandle();
			InputStream stream = arquivo.openStream();
			parser.parse(stream, handler);
			this.actions = handler.getActions();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO Implementar o metodo checkTurn.
	public boolean checkTurn(AID aid, int performative) {

		Action action = actions.peek();

		if (action != null && (action.getAgent().equals(aid) && action.getPerformative() == performative)) {
			try {
				Thread.sleep(500);
				actions.poll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		}

		return false;
	}
}
