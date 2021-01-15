import java.net.*; 
import java.io.*;
import java.util.*;
import java.nio.file.*;

class UDPSender{
	private String host; 
	private int port;
	private String file, fileName; 
	private byte[][] dataArc;

	
	public UDPSender(String sHost, String sPort, String sFile) {
		this.host = sHost;
		this.port = Integer.parseInt(sPort);
		this.file = sFile; 
		this.dataArc = new byte[500][];
		for(int i = 0; i < 500; ++i)
			this.dataArc[i] = null;
		this.fileName = sFile.substring(sFile.lastIndexOf("\\")+1);
		System.out.println("File name: ["+this.fileName+"]"); 
	}
	
	public int send() {
		int delta = 512, pos = 0, number = 0, l, n = 0; 
		byte[] dataTotal = new byte[0], data = new byte[delta], dataSer;
		Path path;
		Random random = new Random();
		
		System.out.println("File: "+this.file); 
		try {
			path = Paths.get(this.file);
			dataTotal =  Files.readAllBytes(path);
		} catch (IOException ex) {
			System.err.println("send (file) IO: "+ex); 
			return -1; 
		}
		
		while(pos < dataTotal.length) {
			dataSer = null;
			if(dataTotal.length - pos > delta) {
				l= delta;
			} else {
				l = dataTotal.length - pos;
			}
			System.arraycopy(dataTotal, pos, data, 0, l);
			number++;
			UDPMessage udm = new UDPMessage(number, data, l, "FILE", this.fileName);
			System.out.println(number+":"+l+":"+pos); 
			pos += l;
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = null;
			try {
				objectOutputStream = new ObjectOutputStream(bos);   
				objectOutputStream.writeObject(udm);
				objectOutputStream.flush();
				dataSer = bos.toByteArray();
			} catch (IOException ex) {
				System.err.println("send (ser) IO: "+ex); 
				return -1; 
			} finally {
				try { bos.close(); } catch (IOException ex) {}
			}
			if(dataSer != null) {
				dataArc[number-1] = dataSer;
			}
		}
		System.out.println("form message array: "+number); 

		System.out.println("sending to: "+this.host+"/"+this.port); 
		while(n >= 0) {
			n = getActualN(random.nextInt(499));
			if(n >= 0 && dataArc[n] != null) {
				System.out.println("Send: "+(n+1)); 
				sendMessage(dataArc[n]); 
				dataArc[n] = null;
			}
		}
		
		/*for(int i = 0; i < number; ++i) {
			System.out.println("Send: "+(i+1)); 
			sendMessage(dataArc[i]); 
		}*/
		return number; 
	}
	
	private int getActualN(int num) {
		int n;

		if(dataArc[num] != null)
			return num;
		for(int i = 1; i < 500; ++i) {
			if(i + num >= 500)
				n = i + num - 500;
			else
				n = i + num;
			if(dataArc[n] != null)
				return n;
		}
		return -1;
	}

	private void sendMessage(byte[] data){ 
		try{
			InetAddress addr = InetAddress.getByName(host);
			DatagramPacket pack = new DatagramPacket(data, data.length, addr, port); 
			DatagramSocket ds = new DatagramSocket(); 
			ds.send(pack); 
			Thread.sleep(100);
			ds.close();
		} catch(IOException e){
			System.err.println("sendMessage IO: "+e); 
		} catch(InterruptedException e){
			System.err.println("sendMessage Int: "+e); 
		} 
	} 
}
