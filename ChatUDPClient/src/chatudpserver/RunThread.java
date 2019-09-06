package chatudpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RunThread implements Runnable {
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	public RunThread(DatagramSocket socket, int buffersize){
		super();
		this.socket = socket;
		packet = new DatagramPacket(new byte[buffersize], buffersize);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

		try{
			while(true){
			
				socket.receive(packet);
				System.out.println("Received Message " + new String(packet.getData(), 0, packet.getLength()));
			}
		}
			catch(SocketException e){ //socket has been closed	
			}
			catch (IOException e){
				e.printStackTrace();
				if (socket != null)
					socket.close();
			}
			
		
		
	}

}
