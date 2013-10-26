package com.genoa.moonsail;

import java.io.Serializable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Message implements Serializable {

	private static final long serialVersionUID = 3473943463725509315L;
	
	public String signal;
	public int message_id;
	public JsonObject message;

	// constructor
	Message(String message) {
		JsonParser parser = new JsonParser();
		System.out.println(message);
		JsonObject json_object = (JsonObject) parser.parse(message);
		if (json_object.has("signal")) {
			this.signal = json_object.get("signal").getAsString();
		}
		if (json_object.has("message_id")) {
			this.message_id = json_object.get("message_id").getAsInt();
		}
		if (json_object.has("message")) {
			this.message = json_object.get("message").getAsJsonObject();
		}
	}

	// constructor
	Message(JsonObject message) {
		if (message.has("signal")) {
			this.signal = message.get("signal").getAsString();
		}
		if (message.has("message_id")) {
			this.message_id = message.get("message_id").getAsInt();
		}
		if (message.has("message")) {
			this.message = message.get("message").getAsJsonObject();
		}
	}
	
	// constructor
	Message(String signal, int message_id, JsonObject message) {
		this.signal = signal;
		this.message_id = message_id;
		this.message = message;
	}
	
	// constructor
	Message(String signal, int message_id, String message) {
		this.signal = signal;
		this.message_id = message_id;
		JsonParser parser = new JsonParser();
		this.message = (JsonObject) parser.parse(message);
	}	
	
	public String json_string(){
		return this.json_object().toString();
	}
	
	public JsonObject json_object(){
		JsonObject result = new JsonObject();
		result.addProperty("signal", signal);
		result.addProperty("message_id", message_id);
		result.add("message", message);
		return result;
	}
	
}
