package moonsail;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.google.gson.*;
import com.google.gson.stream.*;
import com.genoa.moonsail.*;
/*
 * The Stream
 */
public class Broadcaster extends PubSubWorker {


	/*
	 *  Constructor called by console mode
	 *  server: the server address
	 *  port: the port number
	 */
	Broadcaster(String bserver, int bport) {
		super(bserver, bport, 1);
	}

	
	public static void main(String[] args) {
		// default values
		int portNumber = 9082;
		String serverAddress = "localhost";

		// depending of the number of arguments provided we fall through
		switch(args.length) {
			// > javac Stream username portNumber
			case 2:
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Stream [portNumber] [serverAddress]");
					return;
				}
			// > java Stream
			case 0:
				break;
			// invalid number of arguments
			default:
				System.out.println("Usage is: > java Stream[portNumber] {serverAddress]");
			return;
		}
		
		// create the Stream object
		Broadcaster broadcaster = new Broadcaster(serverAddress, portNumber);
		boolean started = broadcaster.serve();
		if (!started) {
			return;
		}
		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
		while(true) {
			System.out.print("> ");
			// read message from user
			String cmd = scan.nextLine();
			// logout if message is LOGOUT
			if(cmd.equalsIgnoreCase("LOGOUT")) {
				// break to do the disconnect
				break;
			}
			
			JsonObject msg = new JsonObject();
			msg.addProperty("signal", "datagrid_price");
			JsonObject message = new JsonObject();
			message.addProperty("price", 23.45);
			msg.add("message", message);
			
			broadcaster.broadcast(msg);
			
		}
		// done disconnect
		broadcaster.disconnect();	
	}
		
}

