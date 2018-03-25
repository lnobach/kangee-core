package de.roo.portmapping.stun;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import de.roo.logging.ILog;
import de.roo.portmapping.AddressDiscoveryException;
import de.roo.util.ByteArrayToolkit;
import de.roo.util.ByteArrayToolkit.Radix;

/**
 * 
 * @author Leo Nobach
 *
 */
public class STUNRequestResponse {

	private static final boolean DBG_PACKETS = false;
	final int SERIALIZE_BUF = 1024;
	final int RESPONSE_BUF = 1024;
	
	public STUNMessage sendSTUNMessageAndWait(STUNMessage msg, InetAddress target, int port, int timeout, ILog log) throws AddressDiscoveryException {
		try {
			STUNTransactionID id = STUNTransactionID.newRandomTransactionID();
			final DatagramSocket s = new DatagramSocket();
			
			byte[] request = new byte[SERIALIZE_BUF];
			int len = msg.writeBytes(request, 0, id);

			if (DBG_PACKETS) {
				log.dbg(this ,"Request is: \n" + ByteArrayToolkit.printArrayRFCLike(request, 0, len, Radix.Binary)
				+ " Decimal: " + ByteArrayToolkit.printArrayRFCLike(request, 0, len, Radix.Decimal));
			}
			
			DatagramPacket p = new DatagramPacket(request, len, target, port);
			s.send(p);
			byte[] buf = new byte[RESPONSE_BUF];
			DatagramPacket response = new DatagramPacket(buf,
					RESPONSE_BUF);
			//TimeoutThread timeoutT = new TimeoutThread(Thread.currentThread(), timeout);
			//timeoutT.start();
			s.setSoTimeout(timeout);
			s.receive(response);
			//timeoutT.interrupt();
			
			if (DBG_PACKETS) {
				log.dbg(this, "Response is: " + ByteArrayToolkit.printArrayRFCLike(response.getData(), 0, response.getLength(), Radix.Binary)
				+ " Decimal: " + ByteArrayToolkit.printArrayRFCLike(response.getData(), 0, len, Radix.Decimal));
			}
			
			return STUNMessage.fromBytes(response.getData(), response.getOffset(), response.getLength(), id);
			
		} catch (SocketTimeoutException e) {
			throw new AddressDiscoveryException("Waited for a STUN response " + timeout + " ms, but now timed out.");
		} catch (SocketException e) {
			throw new AddressDiscoveryException("Could not use socket.", e);
		} catch (UnknownHostException e) {
			throw new AddressDiscoveryException(e);
		} catch (IOException e) {
			throw new AddressDiscoveryException(
					"I/O Error while sending and waiting for packet.", e);
		}
	}
	
	class TimeoutThread extends Thread {
		
		private Thread thread2interrupt;
		private long timeout;

		public TimeoutThread(Thread thread2interrupt, long timeout) {
			this.thread2interrupt = thread2interrupt;
			this.timeout = timeout;
		}

		public void run() {
			try {
				Thread.sleep(timeout);
				System.out.println("Interrupting main thread.");
				thread2interrupt.interrupt();
			} catch (InterruptedException e) {
				//Nothing to do
			}
			
		}

	}
	
}
