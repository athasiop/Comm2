package comm2;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;




public class Test {
	public static void main(String[] args) throws IOException, LineUnavailableException, InterruptedException {
		Scanner sc = new Scanner(System.in);
		
		
		System.out.println("Enter your input: ");
		int userInput = sc.nextInt();
		sc.close();
		switch (userInput) {
		case 1:
			echo();
			break;
		case 2:
			image();
			break;
		case 3:
			sound();
			break;
		}
	}
	public static void echo() throws IOException {
		byte[] txdata = new byte[128];
		byte[] rxdata = new byte[128];
		DatagramSocket s = new DatagramSocket();
		DatagramSocket r = new DatagramSocket(48031);
		InetAddress hostAddress = InetAddress.getByName("ithaki.eng.auth.gr");
		String echo = "E5942T00";
		System.out.println(echo);
		txdata = echo.getBytes();
		DatagramPacket p = new DatagramPacket(txdata,txdata.length,hostAddress,38031);
		DatagramPacket q = new DatagramPacket(rxdata,rxdata.length);
		r.setSoTimeout(2000);
	    	 
			for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); stop > System.nanoTime();) {
				try {
					s.send(p);
				long start = System.currentTimeMillis();
				r.receive(q);
				String message = new String(rxdata,0,q.getLength());
				System.out.println(message);
				System.out.print("Temperature = ");
				for(int i=1;i<5;i++) {
				System.out.print(message.charAt(findTemp(message)+i));
				}
				System.out.println();
				long end = System.currentTimeMillis();
				long timeElapsed = end - start;
				System.out.println("Ping = "+timeElapsed);
				} catch (Exception x) {
				System.out.println(x);
				break;
				}
				}
		
		r.close();
		s.close();
		
	}
	public static int findTemp(String s) {
		return s.indexOf("+");
	}
	public static void image() throws IOException {
		byte[] txdata = new byte[128];
		byte[] rxdata = new byte[1024];
		DatagramSocket s = new DatagramSocket();
		DatagramSocket r = new DatagramSocket(48031);
		InetAddress hostAddress = InetAddress.getByName("ithaki.eng.auth.gr");
		String image = "M5253UDP=1024";
		System.out.println(image);
		txdata = image.getBytes();
		DatagramPacket p = new DatagramPacket(txdata,txdata.length,hostAddress,38031);
		DatagramPacket q = new DatagramPacket(rxdata,rxdata.length);
		r.setSoTimeout(2000);
		File file = new File("Image.jpg");
	     
	    	 s.send(p);
	    	 FileOutputStream fos = new FileOutputStream(file);
			for (;;) {
				try {
				r.receive(q);
				fos.write(rxdata);
				} catch (Exception x) {
				System.out.println(x);
				break;
				}
				}
		
		r.close();
		s.close();
		fos.close();
		System.out.println("Process <<image request>> finished");
	}
	 
	public static void sound() throws IOException, LineUnavailableException, InterruptedException {
		byte[] txdata = new byte[128];
		byte[] rxdata = new byte[128];
		DatagramSocket s = new DatagramSocket();
		DatagramSocket r = new DatagramSocket(48019);
		InetAddress hostAddress = InetAddress.getByName("ithaki.eng.auth.gr");
		String image = "A9815F460";
		System.out.println(image);
		txdata = image.getBytes();
		DatagramPacket p = new DatagramPacket(txdata,txdata.length,hostAddress,38019);
		DatagramPacket q = new DatagramPacket(rxdata,rxdata.length);
		r.setSoTimeout(2000);
		
		 AudioFormat format = new AudioFormat(4000,8,1,true,false);
		 SourceDataLine sourceLine = AudioSystem.getSourceDataLine(format);
		 sourceLine.open();
		 BlockingQueue<byte[]> out = new LinkedBlockingQueue<byte[]>();
	    	
		 	s.send(p);
		 	
		 		
	 Thread playThread = new Thread() {
		 @Override public void run() {
			 while(true) {
				 sourceLine.start();
				  
					try {
						byte[] tmp = out.poll(100,TimeUnit.MILLISECONDS);
						if(tmp==null) {
							sourceLine.stop();
							break;
						}
						sourceLine.write(tmp, 0, 128);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						break;
					}
										
				}
			}
		 
	 
	};
	Thread downloadThread = new Thread() {
		 @Override public void run() {			 
			 for(;;) {
					try {	
					byte[] tmp = new byte[256];
					
					r.receive(q);
					
					for(int i=0;i<128;i++) {						
							byte firstNumber = (byte) ((rxdata[i] >> 4) & (byte) 0x0F);
						    byte secondNumber = (byte) (rxdata[i] & 0x0F);
						    tmp[i] = (byte)(firstNumber-8);
						    tmp[i*2+1] = (byte)(secondNumber-8);
						
					}
					
					out.put(tmp);
					//System.out.println(message);
					} catch (Exception x) {
					System.out.println(x);
					break;
					}
					}
		 }
		 
	 
	};
	downloadThread.start();
	playThread.start();
	downloadThread.join();
	playThread.join(); 
	sourceLine.close();
	r.close();
	s.close();
	System.out.println("ended");
	}
}
