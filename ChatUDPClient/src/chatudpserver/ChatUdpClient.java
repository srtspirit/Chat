package chatudpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Timer;

public class ChatUdpClient {

	private static final String STOP = "stop";
	private static final String DEFAULT_SERVER_ADDRESS = "localhost";
	private static final int CONTROL_PORT_NUMBER = 4095;
	private static final int DATA_PORT_SERVER_NUMBER = 4096;
	private static final int DATA_PORT_CLIENT_NUMBER = 4097;
	private static final int BUFFER_SIZE = 512;
	private static final long ECHO_TIME = 30000; // 0,5 minutes

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/**
		 * Control echo packet and Timer for sending it
		 * 
		 * */
		DatagramSocket sendControlSocket = null;
		try{
			sendControlSocket = new DatagramSocket();
		}
		catch(SocketException e){
			e.printStackTrace();
			return;
		}
		InetSocketAddress serverControlSockAddr = new InetSocketAddress(DEFAULT_SERVER_ADDRESS, CONTROL_PORT_NUMBER);
		DatagramPacket sendControlPacket = null;
		sendControlPacket = new DatagramPacket(new byte[1], //Datagram for first echo packet
													1,
													serverControlSockAddr);
		Timer echoTimer = new Timer(true); // deamon thread
		echoTimer.schedule(new EchoTimerTask(sendControlSocket, sendControlPacket),
							0,
							ECHO_TIME);
		
		/*
		 * End of control echo packet and sending it
		 * */
			
		
		/*
		 * receiving socket and Thread to receive datagrams
		 * */
		
		DatagramSocket recvSocket = null;
		try {
			recvSocket = new DatagramSocket(DATA_PORT_CLIENT_NUMBER);
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			if (sendControlSocket != null){
			sendControlSocket.close();
			
			}
			return;
		}
		Thread thread = new Thread (new RunThread(recvSocket, BUFFER_SIZE));
		thread.start();
		
		/*
		 * End of creating receiving socket and receiving thread
		 * */

		
		/*
		 * sending socket and cycle for accepting command from the keyboard
		 * */
		DatagramSocket sendSocket = null;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if (sendControlSocket != null){
				sendControlSocket.close();
			}
			if (recvSocket != null){
				recvSocket.close();
			}
			return;
		}
		
		
		
		BufferedReader input = new BufferedReader( new InputStreamReader(System.in) );
		String command = null;
		try {
			command = input.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DatagramPacket sendPacket = null;
		
		byte [] buffer = new byte[BUFFER_SIZE];
		sendPacket = new DatagramPacket(buffer,
											BUFFER_SIZE,
											new InetSocketAddress(DEFAULT_SERVER_ADDRESS, DATA_PORT_SERVER_NUMBER));
		
		byte [] temp;
		try {
			while(!STOP.equalsIgnoreCase(command)){ // stop -- close application
			temp = command.getBytes();
			System.arraycopy(temp, 0, buffer, 0, temp.length);
			//sendPacket.setData(buffer, 0, buffer.length);
			sendPacket.setLength(temp.length);
			
			
				sendSocket.send(sendPacket);
				command = input.readLine();
				
			} 
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if (sendControlSocket != null){
				sendControlSocket.close();
			}
			if (sendSocket != null){
				sendSocket.close();
			}
			if (recvSocket != null){
				recvSocket.close();
			}
		}
	}

}
