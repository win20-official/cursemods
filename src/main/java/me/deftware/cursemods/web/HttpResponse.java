package me.deftware.cursemods.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.stream.Collectors;

/**
 * @author Deftware
 */
@Data
public final class HttpResponse {

	private int statusCode;
	private String response;

	public JsonObject asJson() {
		return new Gson().fromJson(response, JsonObject.class);
	}

	public void fromConnection(HttpURLConnection connection) throws Exception {
		setStatusCode(connection.getResponseCode());
		if (getStatusCode() == 204) { // Error
			setResponse("No content");
		} else {
			InputStream stream = getStatusCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
			setResponse(new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining()));
		}
	}

}
