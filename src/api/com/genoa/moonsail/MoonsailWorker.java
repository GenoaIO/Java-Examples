package com.genoa.moonsail;

import java.net.*;
import java.io.*;
import java.util.regex.Pattern;
import java.lang.Exception;
import java.lang.NoSuchMethodException;
import com.google.gson.*;
import com.google.gson.stream.*;


/*
 * The Stream
 */
public class MoonsailWorker {

	// for I/O
	private Gson  gson;
	private JsonReader sInput;		// to read from the socket
	private JsonWriter sOutput;		// to write on the socket
	private OutputStreamWriter sBufOutput;		// to write on the socket
	private Socket socket;

	Object obj;
	
	// the server, the port
	private String server;
	private int port;
	private int reconnect;
	
	public MoonsailWorker(String server, int port, int reconnect, Object obj) {
		this.server = server;
		this.port = port;
		this.reconnect = reconnect;
		this.obj = obj;
		
	}
	
	public boolean serve() {
		try {
			socket = new Socket(server, port);
		} 
		catch(IOException ec) {
			System.out.println("Error connectiong to MoonsailManager " + server + ":" + port + " : " + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		System.out.println(msg);
	
		try
		{
			gson = new Gson();
			sOutput = new JsonWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")));
            sInput = new JsonReader(new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")));
            sInput.setLenient(true);
            sBufOutput = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
		}
		catch (IOException eIO) {
			System.out.println("Exception creating new Input/output Streams: " + eIO);
			eIO.printStackTrace();
			return false;
		}

		// creates the Thread to listen from the server 
		new ListenFromServer(obj).start();
		// success we inform the caller that it worked
		return true;
	}	
	
	/*
	 * When something goes wrong
	 * Close the Input/Output streams and disconnect not much to do in the catch clause
	 */
	public void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} // not much else I can do
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} // not much else I can do
		
	}


	/*
	 * a class that waits for the message from the server and append them to the JTextArea
	 * if we have a GUI or simply System.out.println() it in console mode
	 */
	class ListenFromServer extends Thread {
		
		String decimalPattern = "([0-9]*)\\.([0-9]*)"; 
		
		
		private final MoonsailPerform performObject;  
		  
	    public ListenFromServer(Object object)  
	    {  
	    	 performObject = (MoonsailPerform) object;  
	    }  		
		
		public JsonObject getObject(JsonReader reader) throws IOException {
			reader.beginObject();
			String peek = reader.peek().toString();
			JsonObject json_object = new JsonObject();
			while (sInput.hasNext() && !peek.equals("END_OBJECT")) {
				String name = sInput.nextName();
				peek = sInput.peek().toString();
				if (peek.equals("STRING")) {
					json_object.addProperty(name,reader.nextString());
				} else if (peek.equals("BOOLEAN")) {
					json_object.addProperty(name,reader.nextBoolean());
				} else if (peek.equals("NULL")) {
					reader.nextNull();
					json_object.add(name,null);
				} else if (peek.equals("NUMBER")) {
					String number = reader.nextString();
					boolean match = Pattern.matches(decimalPattern, number);								
					if (match) {
						json_object.addProperty(name,Double.parseDouble(number));
					} else {
						json_object.addProperty(name,Integer.parseInt(number));
					}
				} else if (peek.equals("BEGIN_OBJECT")) {
					json_object.add(name, getObject(reader));
				} else {
					//json_object.addProperty(name,reader.nextString());
					System.out.println("skipping json item: " + name);
					reader.skipValue();
				}
			}
			sInput.endObject();			
			return json_object;
		}
		
		public void start() {
			
			while(true) {
				try {
					String signal = null;
					Long message_id = null;
					JsonObject message = new JsonObject();
					
					Message ret_msg;
					
					try {
						sInput.beginObject();
						while (sInput.hasNext()) {
							String name = sInput.nextName();
							String peek = sInput.peek().toString();						
							if (name.equals("message_id")) {
								message_id = sInput.nextLong();
							} else if (name.equals("message") && peek.equals("BEGIN_OBJECT")) {
								message.add(name, getObject(sInput));
							} else if (name.equals("signal")) {
								signal = sInput.nextString();
							} else {
								sInput.skipValue();
							}
					    }
						sInput.endObject();
						System.out.println("Message received" + message);
						
						if (!message.has("message")) {
							ret_msg = new Message("{\"message_id\":0,\"message\":{\"error\":\"Moonsail protocol error: missing message object in request\"}}");
						} else {
							JsonObject request = message.get("message").getAsJsonObject(); 
							int request_id = request.get("request_id").getAsInt();
							String service = request.get("service").getAsString();
							String method_name = request.get("method").getAsString();
							int accl_key = request.get("accl_key").getAsInt();
							JsonObject args = request.get("args").getAsJsonObject(); 
							
							try {
								
								JsonObject ret_object = performObject.perform(method_name, args, accl_key);
								ret_msg = new Message("{\"message_id\":" + message_id + ",\"message\":{\"result\":" + ret_object + "}}");
							}
							catch (Exception e) {
								System.out.println("Error performing request: " + e);
								ret_msg = new Message("{\"message_id\":" + message_id + ",\"message\":{\"error\":\"" + e + "\"}}");
								e.printStackTrace();
							}
						}
					}
					catch (Exception pe) {
						ret_msg = new Message("{\"message_id\":0,\"message\":{\"error\":\"Moonsail protocol error: " + pe + "\"}}");
						System.out.println("Moonsail protocol error: " + pe);
						pe.printStackTrace();
					}
					
					gson.toJson(ret_msg, ret_msg.getClass(), sOutput);
					sOutput.flush();
					sBufOutput.write("\r\n\r\n");
					sBufOutput.flush();
					
				}
				catch(IOException e) {
					System.out.println("Server has close the connection: " + e);
					break;
				}
			}
		}
	}
		
}

