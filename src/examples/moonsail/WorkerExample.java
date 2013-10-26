package moonsail;

import java.util.Scanner;

import com.genoa.moonsail.MoonsailPerform;
import com.genoa.moonsail.MoonsailWorker;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/*
 * The Stream
 */
public class WorkerExample implements MoonsailPerform {

	MoonsailWorker worker = null;
	
	WorkerExample(String server, int port, int reconnect) {
		worker = new MoonsailWorker(server, port, reconnect, this);
	}

	public boolean serve() {
		return worker.serve();
	}
	
	public void disconnect() {
		worker.disconnect();
	}
	
	public JsonObject sum(int a, int b) {
		int result = a + b;	
		JsonObject json = new JsonObject();
		json.addProperty("result", result);
		return json;
	}
	
	public JsonObject describe() {
		/*
			description = {
				"name":name, 
				"args": args, 
				"defaults": defaults,
				"requires": requires, 
				"docs": docs
			}    	
		 */
    	JsonArray json_array = new JsonArray();
    	JsonObject json_obj = new JsonObject();
    	
    	//For each method:
    	JsonObject member = new JsonObject();
    	member.addProperty("name", "sum");
    	member.addProperty("docs", "function sum(int a, int b) returns the sum of two integers.");
    	json_array.add(member);
    	    	
    	json_obj.add("methods", json_array);        
        return json_obj;
    }	

    public JsonObject perform(String method_name, JsonObject args, int accl_key) throws Exception{
    	if (method_name.equals("_describe_")) {
    		return describe();
    	} else if (method_name.equals("sum")) {
    		int a = args.get("a").getAsInt();
    		int b = args.get("b").getAsInt();
    		return sum(a,b);
    	} else {
    		throw new Exception("No such method " + method_name);
    	}
    }
	
	public static void main(String[] args) {
		// default values
		int portNumber = 9083;
		String serverAddress = "localhost";

		// depending of the number of arguments provided we fall through
		switch(args.length) {
			// > javac Stream portNumber
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
		WorkerExample worker = new WorkerExample(serverAddress, portNumber, 1);
		// test if we can start the connection to the Server
		// if it failed nothing we can do
		
		//JsonObject test = worker.describe();
		
		if(!worker.serve())
			return;
		
		// wait for messages from user
		Scanner scan = new Scanner(System.in);
		// loop forever for message from the user
		while(true) {
			// read message from user
			String msg = scan.nextLine();
			// logout if message is LOGOUT
			if(msg.equalsIgnoreCase("LOGOUT")) {
				// break to do the disconnect
				break;
			}
		}
		// done disconnect
		worker.disconnect();	
	}

		
}

