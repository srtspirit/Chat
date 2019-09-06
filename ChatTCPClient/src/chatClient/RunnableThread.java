package chatClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

// this class listen socket in separate thread and print results to console
// in the constructor it gets BufferedReader object from which it will read lines
public class RunnableThread implements Runnable {
	private BufferedReader reader;
	
	public RunnableThread(BufferedReader reader){
		this.reader = reader;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String string;
		try{
			string = reader.readLine();
			while (string != null){ // until connection closed
				System.out.println(string);
				string = reader.readLine();
			}
		} catch (SocketException e){
			// connection closed
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
