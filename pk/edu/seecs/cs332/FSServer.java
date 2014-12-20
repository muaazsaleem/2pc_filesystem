package pk.edu.seecs.cs332;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.Iterator;

public class FSServer {

	static int offset=-1;
	static int nextoffset=-1;
	static ServerSocket listener;
	static Socket serverSocket;
	static int port;
	static File dir;

	private static void initialize(String _port, String _dir){
		port = Integer.parseInt(_port);
		dir = new File(_dir);

		try{
			listener = new ServerSocket(port);
			
		}
		catch(Exception e){
			System.out.println("initialize() failed with"+ e);
		}
	}

	private static void listen(){
		try{
			serverSocket = listener.accept();
		}
		catch (Exception e) {
			System.out.println("listen() failed cuz of: "+e);
		}
	}

	private static void respond( DataInputStream input, DataOutputStream output,
									String fileName) throws Exception{
		String data=null;
	    File tmp = new File(dir.getPath()+"\\"+ "tmp.txt");
		System.out.println("dir.getPath()"+"/"+ "tmp.txt");
		
		RandomAccessFile f= new RandomAccessFile(tmp,"rwd");
		
		boolean loop=true;
		while(loop==true)
		{		            
			System.out.println("offset:  "+FSServer.offset);
			String sentence = getOrignalData(input.readUTF());                 
			System.out.println("RECEIVED: " + sentence);
			System.out.println("offset:  "+FSServer.offset);
			data = sentence;
			output.writeUTF("ack");
			System.out.println("Waiting for File Data..");
			System.out.println("offset:  "+FSServer.offset);
			
			if(data!=null){
				switch (data) {
					case "close" : 
						loop = false;
						break;

					case "commit":
						System.out.println("Commit Received");
						f.close();
						File source = tmp;
						File dest = new File(dir.getPath()+"/"+fileName);

						try {
						 copyFile(source, dest);
						 source.delete();
						} catch (IOException e) {
						 e.printStackTrace();
						}

						loop = false;
						break;

					case "abort":
						tmp.delete();
						loop = false;
						break;
					default:
						System.out.println("Data received: "+data);
						System.out.println("offset:  "+FSServer.offset);
						f.seek(offset);
						f.write(data.getBytes(),0,data.length());	
				}
			}   
		}
	}
  
  public static void main(String args[]) throws IOException {
	  
	initialize( args[0], args[1]);
	if(dir.exists()){
			System.out.println(dir + " already exists!");
			return;
		}
		dir.mkdirs();  //Making folder to save files to.
    listen();
    
    System.out.println("Listening at! " + serverSocket.getLocalPort());
    System.out.println("Got client! at " + 
    								serverSocket.getRemoteSocketAddress());
    DataInputStream input =
    					new DataInputStream(serverSocket.getInputStream());
    DataOutputStream output = 
    					new DataOutputStream(serverSocket.getOutputStream());
    
    	
    System.out.println("offset:  "+FSServer.offset);
	String fileName = getOrignalData(new String(input.readUTF()));             
	System.out.println("offset:  "+FSServer.offset);
    System.out.println(fileName);
    
    output.writeUTF("ack");
    try{              
    	respond(input, output, fileName);
    }
    catch (Exception e) {
    	System.out.println("Respond failed cuz of " + e);
    }

	System.out.println("Server is going to close");
	  
    serverSocket.close();
                 
	
  }
  

  
  
 public static String getOrignalData(String d)
 {
	 String offset="";
	 String dataset="";
	 boolean fc=true;
	for(int j=0;j<d.length();j++)
	{
		if(d.toCharArray()[j]==';')
		{
			fc=false;
			j++;
		}
		
		if(fc)
		{
			offset+=d.toCharArray()[j];
			
		}
		else
		{
			dataset+=d.toCharArray()[j];
		}
		
	}
	System.out.println(offset);
	
	FSServer.offset=Integer.parseInt(offset);
	System.out.println("offset:  "+FSServer.offset);
	
	return dataset;	 
 }
  
  public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;
	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();

	        long count = 0;
	        long size = source.size();              
	        while((count += destination.transferFrom(source, count, size-count))<size);
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
  
  
  
  

}
