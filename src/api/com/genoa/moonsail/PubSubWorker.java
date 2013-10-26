package com.genoa.moonsail;

import java.net.*;
import java.io.*;

import com.google.gson.*;
import com.google.gson.stream.*;

/*
 * The Stream
 */
public class PubSubWorker  {

	private Gson  gson;
	private JsonWriter sOutput;		// to write on the socket
	private OutputStreamWriter sStreamOutput;		// to write on the socket
	private Socket serverSocket;
	private Socket socket;

	private String server;
	private int port;
	private int reconnect;

	protected PubSubWorker(String server, int port, int reconnect) {
		this.server = server;
		this.port = port;
		this.reconnect = 1;
	}
	
	public boolean serve() {
		
		try {
			socket = new Socket(server, port);
		} catch(IOException ec) {
			System.out.println("Error binding to port "+ port + ": " + ec);
			return false;
		}
		
		String msg = "connected to PubSubManager " + socket.getInetAddress() + ":" + socket.getPort();
		System.out.println(msg);
	
		try
		{
			gson = new Gson();
			sOutput = new JsonWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")));
            sStreamOutput = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
		}
		catch (IOException eIO) {
			System.out.println("Exception creating new Output Streams: " + eIO);
			eIO.printStackTrace();
			return false;
		}

		return true;
	}

	public void disconnect() {
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} // not much else I can do
        try{
			if(serverSocket != null) serverSocket.close();
		}
		catch(Exception e) {} // not much else I can do
		
	}
		
	public boolean broadcast(Message message) {
		try {
			gson.toJson(message, message.getClass(), sOutput);
			sOutput.flush();
			sStreamOutput.write("\r\n\r\n");
			sStreamOutput.flush();
		} 
		catch (JsonIOException eJIO) {
			System.out.println("Json error transforming message object: " + eJIO);
			eJIO.printStackTrace();
			return false;
		}
		catch (IOException eIO) {
			System.out.println("IO error transmitting message: " + eIO);
			eIO.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean broadcast(JsonObject message) {
		Message msg  = new Message(message);
		return this.broadcast(msg);
	}
	
	
}

