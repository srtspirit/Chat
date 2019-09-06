package chatudpserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

public class EchoTimerTask extends TimerTask {
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	public EchoTimerTask(DatagramSocket socket, DatagramPacket packet){
		this.socket = socket;
		this.packet = packet;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
