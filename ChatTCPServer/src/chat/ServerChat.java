package chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class ServerChat {
	private static final String STOP = "stop";
	
	private int port = 0;
	private Random rand;
	private PrintStream log; // for logging. I Use System.out
	
	// this method recieves a command that user input from console
	// interprete it and call corresponding method
	// return false if we have got Stop command -- server stops
	// and we can close the application
	
	private void WriteNumberOfConnectedClientsIntoLog(){
		log.println("Total " + chatConnectionsList.size() + " clients");
	}
	
	private void SendToAll(String message){
		log.println("Sending " + message + " to all clients");
		if (chatConnectionsList.size() > 0){
			for (ChatConnection chatConnection: chatConnectionsList){
				chatConnection.SendMessage(message);
			}
			log.println("OK");
		}
		else{
			log.println("There's no clients");
		}
	}
	
	public boolean ExecuteCommand(String command){
		if ( STOP.equalsIgnoreCase(command) ){ //couldn't do switch statement cause of java 1.6
			log.println(command + " command");
			StopServer();
			return false;
		}
		
		SendToAll(command); //it's like default case
		
		return true;
	}
	
	public void Terminate(){ // emergency closes the server
		log.println("Terminate...");
		StopServer();
	}
	
	//SendMessage decides whether we should send message to all clients or to a particular one
	//we get a random int from 0 to <number of clients + 1>
	//if this int is more then number of client, i.e. +1 then we send message to all clients
	//otherwise to particular one, according to this number
	// caller needs to not send message back
	private void SendMessageToRandom(String message, ChatConnection caller){
		log.println("Message: " + message);
		int randomNumber = rand.nextInt( chatConnectionsList.size() + 1 );
		
		if (randomNumber == chatConnectionsList.size()){ //sending message to all clients
			log.println("Sending to all...");
			message = "Resending to all clients\n" + message;
			for (ChatConnection chatConnection: chatConnectionsList){
				if (chatConnection == caller) continue; //the same object that sent the message
				chatConnection.SendMessage(message);
			}
		}
		else{ // in this case message can be sent back to the sender
			log.println("Sending to " + randomNumber + "...");
			chatConnectionsList.get(randomNumber).SendMessage(message);
		}
		log.println("OK");
	}
	
	//Method startServer opens socket and starts accept connections
	public void StartServer() throws IOException{
		log.println("Starting server...");
		try{
			listener = new Listener(port);
			listenerThread = new Thread(listener);
			listenerThread.start();
			log.println("OK");
		} catch (IOException e){
			log.println("Couldn't start the server.");
			throw e;
		}
	}
	
	//Method StopServer closes listener and opened connections
	private void StopServer(){
		try{
			log.println("Stopping server...");
			if (listener != null){
				listener.listenSocket.close(); // closes socket. accept() method throws SocketException
			}
		
			for (ChatConnection chatConnection: chatConnectionsList){
				if (chatConnection.socket != null){
					chatConnection.socket.close(); //closes socket. method read will throw exception
				}
			}
			log.println("OK");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// this class represents connection with a client
	private class ChatConnection implements Runnable {
		private Socket socket = null; // socket (established connection)
		private BufferedReader reader = null; // buffers for reading and writing
		private BufferedWriter writer = null;
		
		public ChatConnection(Socket s) throws IOException {
			socket = s;
			reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			writer = new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
		}
		
		@Override
		public void run(){ // getting messages from the socket
			try{
				String string;
				
				while( (string = reader.readLine()) != null ){ // null means connection closed
					SendMessageToRandom(string, this);
					// retrieve a string from the socket and send it to ServerChat 
				}
			} catch (SocketException e){
				// connection closed by other side
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (socket != null){
					log.println("Clent " + socket.getRemoteSocketAddress() + " disconnected");
					log.println("Closing connection....");
					try {
						socket.close();
						chatConnectionsList.remove(this); // connection removed from the list
						log.println("OK");
						WriteNumberOfConnectedClientsIntoLog();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						log.println("Couldn't close connection");
						e.printStackTrace();
					}
				}
			}
		}
		
		private void SendMessage(String message){
			try {
				writer.write(message + "\n"); // without \n other side doesn't read line
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private List<ChatConnection> chatConnectionsList;
	// listener class accepts incoming connections, creates sockets and stores them into collection
	// Listening is in separate thread
	private class Listener implements Runnable {
		private ServerSocket listenSocket = null; // Socket for incoming connections
		public Listener(int port) throws IOException {
			listenSocket = new ServerSocket(port);
		}
		
		@Override
		public void run() { //accepts new connections
			// TODO Auto-generated method stub
			try{
				while(true){
					Socket socket = listenSocket.accept();
					ChatConnection conn = new ChatConnection(socket);
					chatConnectionsList.add( conn );
					log.println("Client " + socket.getRemoteSocketAddress() + " connected");
					WriteNumberOfConnectedClientsIntoLog();
					conn.SendMessage("Connected successfully");
					new Thread(conn).start();
				}
			}
			catch(SocketException e){
				//SocketException means that socket has been closed
				//This is the only way to quit this endless cycle
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private Listener listener;
	Thread listenerThread;
	
	public ServerChat(int port, PrintStream log){
		this.log = log;
		this.port = port;
		rand = new Random();
		chatConnectionsList = new LinkedList<ChatConnection>();
		listener = null; // we will create listener later within StartServerMethod
						//don't want to throw exceptions from a constructor
	}
}
