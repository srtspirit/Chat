package chatudpserver;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChatServer {
	private static final String STOP = "stop";
	private static final int BUFFER_SIZE = 512;
	
	private DatagramSocket dataSocket; // for receiving messages
	private DatagramSocket controlSocket; // for receiving control packets: echo for example
	private DatagramSocket sendSocket; // To send messagges

	private DatagramPacket sendPacket;
	private int dataPortServer;
	private int dataPortClient;
	private int controlPort;
	private long checkConnectionsTimeInterval; // how often we check for down connections
	private PrintStream log; //logging info
	private Random rand;
	
	//I use Map to collect all connections with clients
	//InetAddress -- address of a client
	//Boolean - his status. True if client is alive; false - probably down
	//A client becomes probably down if he doesn't send echo packet within a period of time
	//echo packet from client can change probably down to alive
	//probably down clients will removed from the set
	//AtomicBoolean vs Boolean: AtomicBoolean is mutable
	private Map<InetAddress, AtomicBoolean> connectionsMap;
	
	// classes that extens this class will receive packets from socket
	// DataListener will operate with messages
	// ControlListener will operate with echo packets
	
	private abstract class Listener implements Runnable{
		protected DatagramSocket socket;
		protected DatagramPacket packet;
		
		public Listener (DatagramSocket socket){
			super();
			this.socket = socket;
			packet = new DatagramPacket( new byte[BUFFER_SIZE], BUFFER_SIZE );
		}
	}
	
	private class DataListener extends Listener{
		public DataListener (DatagramSocket socket){
			super(socket);
		}
		
		@Override
		public void run() {
			try{
				while (true){
					socket.receive(packet);
					log.println( "Received message: " + new String (packet.getData(), 0, packet.getLength()) );
					SendMessageToRandom(packet);
				}
			}
			catch (SocketException e){ // socket has been closed. Server is shutting down
				
			}
			catch (IOException e){
				log.println("Error while listening for data");
				e.printStackTrace();
				TerminateServer();				
			}
		}
	}
	
	
	private class ControlListener extends Listener{
		public ControlListener (DatagramSocket socket){
			super(socket);
		}
		
		@Override
		public void run() {
			try{
				while(true){
				
					socket.receive(packet);
					InetAddress inetaddress = packet.getAddress();
					log.println("Got echo packet from " + inetaddress);
					AtomicBoolean status;
				
						// we received an echo packet
						// need to put this client into collection or renew status
						synchronized (connectionsMap){
							status = connectionsMap.get(inetaddress);
							if ( status == null ){ //  new client
								connectionsMap.put(inetaddress, new AtomicBoolean(true));
								log.println("New client. Adding...");
								log.println("OK");
								log.println("Total " + connectionsMap.size() + " clients");
							}
							else{ // existing client renew status
								status.set(true);
							}
						}
					}
				
				
					
				
				}
			catch (SocketException e){ // socket has been closed. Server is shutting down
				
			}
			catch (IOException e){
				log.println("Error while listening for echo");
				e.printStackTrace();
				TerminateServer();
			}
			
			}
		}
	
	public ChatServer(int dataPortServer, int dataPortClient, int controlPort, long timeInterval, PrintStream log){
		
		this.dataPortServer = dataPortServer;
		this.dataPortClient = dataPortClient;
		this.controlPort = controlPort;
		this.checkConnectionsTimeInterval = timeInterval;
		this.log = log;
		rand = new Random();
		sendPacket = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
		sendPacket.setPort(dataPortClient);
		
		connectionsMap = new HashMap<InetAddress, AtomicBoolean>();
	}
	
	public void StartServer() throws SocketException{
		log.println("Starting server...");
		
		dataSocket = new DatagramSocket(dataPortServer);
		controlSocket = new DatagramSocket(controlPort);
		sendSocket = new DatagramSocket();
		
		Thread dataThread = new Thread( new DataListener(dataSocket) );
		dataThread.start();
		
		Thread controlThread = new Thread ( new ControlListener(controlSocket) );
		controlThread.start();
		
		Timer checkConnectionTimer = new Timer(true); // deamon thread
		checkConnectionTimer.schedule(new CheckConnectionsTimerTask(connectionsMap),
										checkConnectionsTimeInterval,
										checkConnectionsTimeInterval);
		
		log.println("OK");
	}

	public boolean ExecuteCommand(String command){
		if ( STOP.equalsIgnoreCase(command) ){ //couldn't do switch statement cause of java 1.6
			log.println(command + " command");
			StopServer();
			return false;
		}
		
		SendMessageToAll(command); //it's like default case
		
		return true;
	}
	
	public void TerminateServer(){
		log.println("Server is closing in an unusual way");
		StopServer();
	}
	
	private void StopServer(){
		if (dataSocket != null) dataSocket.close();
		if (controlSocket != null) controlSocket.close();
		if (sendSocket != null) sendSocket.close();
	}
	
	// sends message to all (string)
	private void SendMessageToAll(String message){
		byte [] buffer = message.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer,
													buffer.length,
													null, //address will be later
													dataPortClient);
		Set<InetAddress> set = connectionsMap.keySet();
		for(InetAddress address:set){
			packet.setAddress(address);
			
			synchronized(sendSocket){
				try {
					sendSocket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.println("Error while sending the message");
					e.printStackTrace();
					TerminateServer();
				}
			}
		}
	}
	
	//SendMessage decides whether we should send message to all clients or to a particular one
	//we get a random int from 0 to <number of clients + 1>
	//if this int is more then number of client, i.e. +1 then we send message to all clients
	//otherwise to particular one, according to this number
	// caller needs to not send message back
	private void SendMessageToRandom(DatagramPacket packetToResend){
		
		InetAddress senderAddress = packetToResend.getAddress(); // to not send message back
		int randomNumber = rand.nextInt( connectionsMap.size() + 1 );
		Set<InetAddress> addressesSet = connectionsMap.keySet();
		
		System.arraycopy(packetToResend.getData(), 0, sendPacket.getData(), 0, packetToResend.getLength());
		sendPacket.setLength(packetToResend.getLength());
		
		if (randomNumber == addressesSet.size()){ //sending message to all clients
			log.println("Sending to all...");
			
			for (InetAddress address: addressesSet){
				if (address.equals(senderAddress)) continue; //the same object that sent the message
				
				sendPacket.setAddress(address);
				synchronized(sendSocket){
					try {
						sendSocket.send(sendPacket);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						log.println("Error while sending the message");
						e.printStackTrace();
						TerminateServer();
					}
				}
			}
		}
		else{ // in this case message can be sent back to the sender
			log.println("Sending to " + randomNumber + "...");
			
			InetAddress address;
			Iterator<InetAddress> iterator = addressesSet.iterator();
			address = iterator.next();
			for (int i = 0; i < randomNumber; i++) // looking the address with index randomNumber
				address = iterator.next();
			sendPacket.setAddress(address);
			synchronized(sendSocket){
				try {
					sendSocket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.println("Error while sending the message");
					e.printStackTrace();
					TerminateServer();
				}
			}
		}
		log.println("OK");
	}
	
	private class CheckConnectionsTimerTask extends TimerTask{
		private Map<InetAddress, AtomicBoolean> map;
		
		public CheckConnectionsTimerTask( Map<InetAddress, AtomicBoolean> map ){
			super();
			this.map = map;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			log.println("timer works");
			synchronized (map){
				for (Entry<InetAddress, AtomicBoolean> entry: map.entrySet()){
					if ( entry.getValue().get() ){ // alive client
						entry.getValue().set(false); // make him probably down to remove on the next call
					}
					else{ // probably down client
						map.remove(entry.getKey()); // removing it
						log.println("Client " + entry.getKey() + " is down. Removing...");
						log.println("OK");
						log.println("Total " + map.size() + " clients");
					}
				}
			}
		}
		
	}
}
