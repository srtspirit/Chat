package chatudpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class ChatUdpServer {
	private static final int CONTROL_PORT_NUMBER = 4095;
	private static final int DATA_PORT_SERVER_NUMBER = 4096;
	private static final int DATA_PORT_CLIENT_NUMBER = 4097;
	private static final long CHECK_TIME_INTERVAL = 90000; //1,5 minutes

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ChatServer server = new ChatServer(DATA_PORT_SERVER_NUMBER,
											DATA_PORT_CLIENT_NUMBER,
											CONTROL_PORT_NUMBER,
											CHECK_TIME_INTERVAL,
											System.out);
		
		try {
			server.StartServer();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} 
		
		BufferedReader input = new BufferedReader( new InputStreamReader(System.in) );
		
		String command;
		try{
			do{
				command = input.readLine();
			}
			while ( server.ExecuteCommand(command) ); //execute commands until method returns false
													// it will return false after executing shut down command
		}
		catch(IOException e){
			e.printStackTrace();
//			server.Terminate();
		}
	}

}
