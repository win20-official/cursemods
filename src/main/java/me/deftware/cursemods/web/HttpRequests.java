package me.deftware.cursemods.web;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Deftware
 */
public final class HttpRequests {

	public static String BROWSER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

	public static HttpResponse post(URL url, String payload, HashMap<String, String> headers, String agent, String type) throws Exception {
		HttpResponse httpResponse = new HttpResponse();
		// Create connection
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setConnectTimeout(8 * 1000);
		connection.setRequestProperty("User-Agent", agent);
		if (headers != null) {
			headers.forEach(connection::setRequestProperty);
		}
		// Send it
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", type);
		DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
		outputStream.writeBytes(payload);
		outputStream.flush();
		outputStream.close();
		connection.connect();
		// Check status
		httpResponse.fromConnection(connection);
		connection.disconnect();
		return httpResponse;
	}

	public static HttpResponse get(URL url, HashMap<String, String> headers, String agent) throws Exception {
		HttpResponse httpResponse = new HttpResponse();
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(8 * 1000);
		connection.setRequestProperty("User-Agent", agent);
		if (headers != null) {
			headers.forEach(connection::setRequestProperty);
		}
		connection.setRequestMethod("GET");
		// Check status
		httpResponse.fromConnection(connection);
		connection.disconnect();
		return httpResponse;
	}

	public static String urlEncodeParams(HashMap<String, String> params) throws Exception {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for(Map.Entry<String, String> entry : params.entrySet()){
			if (first)
				first = false;
			else
				result.append("&");
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return result.toString();
	}

}
