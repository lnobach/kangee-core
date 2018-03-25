package de.roo.portmapping.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Leo Nobach
 *
 */
public class UDPReceiverThread extends Thread {

	private DatagramSocket sock;
	private List<String> responses;

	public UDPReceiverThread(DatagramSocket sock) {
		this.sock = sock;
	}

	public void run() {
		responses = new ArrayList<String>(10);
		while (true) {
			try {
				final int RESPONSE_L = 1024;
				byte[] buf = new byte[RESPONSE_L];
				DatagramPacket response = new DatagramPacket(buf,
						RESPONSE_L);
				sock.receive(response);
				synchronized (responses) {
					String responseStr = new String(response.getData());
					responses.add(responseStr);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void terminate() {
		synchronized (responses) {
			this.interrupt();
		}
	}

	public List<String> getResponses() {
		return responses;
	}

}
