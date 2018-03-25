package de.roo.http;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public class HttpStatusCodes {

	static Map<Integer, String> strFromID = new HashMap<Integer, String>();
	static Map<String, Integer> idFromStr = new HashMap<String, Integer>();
	
	static {
		add(100, "Continue");
		add(101, "Switching Protocols");
		add(102, "Processing");

		add(200, "OK");
		add(201, "Created");
		add(202, "Accepted");
		add(203, "Non-Authoritative Information");
		add(204, "No Content");
		add(205, "Reset Content");
		add(206, "Partial Content");
		add(207, "Multi-Status");

		add(300, "Multiple Choices");
		add(301, "Moved Permanently");
		add(302, "Found");
		add(303, "See Other");
		add(304, "Not Modified");
		add(305, "Use Proxy");
		add(306, "Switch Proxy");
		add(307, "Temporary Redirect");

		add(400, "Bad Request");
		add(401, "Unauthorized");
		add(402, "Payment Required");
		add(403, "Forbidden");
		add(404, "Not Found");
		add(405, "Method Not Allowed");
		add(406, "Not Acceptable");
		add(407, "Proxy Authentication Required");
		add(408, "Request Timeout");
		add(409, "Conflict");
		add(410, "Gone");
		add(411, "Length Required");
		add(412, "Precondition Failed");
		add(413, "Request Entity Too Large");
		add(414, "Request-URI Too Long");
		add(415, "Unsupported Media Type");
		add(416, "Requested Range Not Satisfiable");
		add(417, "Expectation Failed");
		add(418, "I'm a teapot");
		add(422, "Unprocessable Entity");
		add(423, "Locked");
		add(424, "Failed Dependency");
		add(425, "Unordered Collection");
		add(426, "Upgrade Required");
		add(449, "Retry With");
		add(450, "Blocked by Windows Parental Controls");

		add(500, "Internal Server Error");
		add(501, "Not Implemented");
		add(502, "Bad Gateway");
		add(503, "Service Unavailable");
		add(504, "Gateway Timeout");
		add(505, "HTTP Version Not Supported");
		add(506, "Variant Also Negotiates");
		add(507, "Insufficient Storage");
		add(509, "Bandwidth Limit Exceeded");
		add(510, "Not Extended");
	}

	public static void add(int number, String code) {
		strFromID.put(number, code);
		idFromStr.put(code, number);
	}
	
	public static int fromStr(String statusCode) {
		return idFromStr.get(statusCode);
	}
	
	public static String fromID(int statusCode) {
		return strFromID.get(statusCode);
	}
}
