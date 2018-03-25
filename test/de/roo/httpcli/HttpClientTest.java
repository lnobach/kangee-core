package de.roo.httpcli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import de.roo.http.ReqMethod;
import de.roo.logging.ConsoleLog;
import de.roo.logging.ILog;
import de.roo.util.stream.StreamCopy;

/**
 * 
 * @author Leo Nobach (dev@getkangee.com)
 *
 */
public class HttpClientTest {

	public static void main(String[] args) {
		
		test2();
		
	}
	
	static void test1() {
		ILog log = new ConsoleLog();
		
		HttpClient cli = new HttpClient("localhost", 80);
		try {		
			URL reqURL = new URL("http://localhost/roows/");
			HttpRequest req = new HttpRequest(reqURL);
			cli.makeRequest(req, log, new RespHdlrImpl());
		} catch (HttpClientException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	static void test2() {
		ILog log = new ConsoleLog();
		
		HttpClient cli = new HttpClient("localhost", 80);
		try {		
			URL reqURL = new URL("http://localhost/roows/");
			HttpRequest req = new HttpRequest(reqURL);
			req.setMethod(ReqMethod.POST);
			req.setPOSTContent("Blablub".getBytes());
			cli.makeRequest(req, log, new RespHdlrImpl());
			
			URL reqURL2 = new URL("http://localhost/roows2/");
			HttpRequest req2 = new HttpRequest(reqURL2);
			req2.setMethod(ReqMethod.GET);
			cli.makeRequest(req2, log, new RespHdlrImpl());
		} catch (HttpClientException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public static class RespHdlrImpl implements IResponseHandler {

		@Override
		public void handleResponse(HttpResponse resp) throws IOException {
			System.out.println(resp.getResponseVersion() + " " + resp.getResponseCode());
			System.out.println("Headers: " + resp.getResponseHeaders());
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			new StreamCopy().copy(resp.getInputStream(), os);
			
			System.out.println("Content: " + os.toString());
			
		}
		
	}
	
}
