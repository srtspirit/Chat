package chatClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatClient {
	private static final String STOP = "stop";
	private static final String DEFAULT_SERVER_ADDRESS = "localhost";
	private static final int DEFAULT_PORT_NUMBER = 4096;

	public static void main(String[] args) { // from commandline we recieve address like localhost:4096
		// TODO Auto-generated method stub
		String dest_addr;
		int dest_port;
		if (args.length >= 1){ // using parameters from command line
			Pattern pattern = Pattern.compile("(.+):(\\d{1,5})"); // regular expression that matches address (IP or domain) and port
															// actually I use only ':'. The rest will be checked by Socket class
			Matcher matcher = pattern.matcher(args[0]);
			matcher.find();
			dest_addr = matcher.group(1); // address
			dest_port = Integer.parseInt( matcher.group(2) ); // port
		}
		else{ // using default parameters
			dest_addr = DEFAULT_SERVER_ADDRESS;
			dest_port = DEFAULT_PORT_NUMBER;
		}
	
		Socket socket = null;
		try {
			socket = new Socket(dest_addr, dest_port);
			
			BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
			
			new Thread( new RunnableThread(reader) ).start();
			
			BufferedReader input = new BufferedReader( new InputStreamReader(System.in) );
			String command = input.readLine(); // reading messages from the console
			while(!STOP.equalsIgnoreCase(command)){ // stop -- close application
				writer.write(command + "\n"); // without \n reciever won't read anything
				writer.flush();
				command = input.readLine();
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

}
