package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;


public class Chat {
	private static final int DEFAULT_PORT_NUMBER = 4096;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ServerChat server = new ServerChat(DEFAULT_PORT_NUMBER, System.out);
		
		try {
			server.StartServer();
		} catch (BindException e){
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			server.Terminate();
		}
	}
}
