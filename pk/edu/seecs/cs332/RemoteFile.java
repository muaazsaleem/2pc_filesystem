package pk.edu.seecs.cs332;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class RemoteFile {
    Socket [] clientSocket; 
    private String filename;
    public Random rn;
    
    public RemoteFile(String _filename) {
        filename = _filename;
        rn = new Random();
        connectToServers();
        sendFilname(_filename);
        
    }

    public int writeBlock(byte[] data, int offset){
        System.out.println("Writing the block");
        if(data.length<=512)
        {
                System.out.println("Data: "+ new String(data));
                tPC(new String(data), offset);
        }
             
        return 0;
    }

    public int commit() {
        tPC("commit", -1);
        return 0;
    }

    public void abort() {
        tPC("abort", -1);
    }

    public int close(){
        
        for(int server=0; server<clientSocket.length; server++) //close sockets
            try{
                clientSocket[server].close();
            }
            catch(Exception e){
                System.out.println("close(): Couldn't close Server "+ server
                                    + " cuz of "+e  );
            }
        return 0;
    }


    public String getName() {
        return filename;
    }

    private void connectToServers(){
        clientSocket = new Socket[ReplicatedFS.serverNames.size()]; 
        
        for(int k=0; k<clientSocket.length; k++)
        {
            try{
                clientSocket[k] = 
                            new Socket(ReplicatedFS.serverNames.get(k),
                                       ReplicatedFS.ports.get(k));  
                clientSocket[k].setSoTimeout(100);
            }
            catch (Exception e) {
                System.out.println("Couldn't connect to Server: "+ k +
                                    "cuz of "+ e);
            }

        }
    }
    
    private void sendFilname(String filename){
        try{
            for(int server=0; server<clientSocket.length; server++) //Send filname across servers
                send(server,filename,-1);
            for(int server=0; server<clientSocket.length; server++) // receive acks
                System.out.println(receive(server));
        }
        catch (Exception e) {
            System.out.println("Couldn't send file names cuz of"+ e);
        }
    }

    void send(int i, String data, int offset) throws IOException {
    	DataOutputStream out = new DataOutputStream(clientSocket[i].getOutputStream());
    	int answer = rn.nextInt(100);
    	if( answer > ReplicatedFS.dropRate){
    	   out.writeUTF((Integer.toString(offset)+";"+ data));
    	}
    	else{
    		
        	System.out.println("Packet Dropped");
    	}
    	
    }
    
    String receive(int i) throws IOException {
    	DataInputStream input= new DataInputStream(clientSocket[i].getInputStream());
		return new String(input.readUTF()); 
    }
    
    
    private void tPC(String data, int offset){
        for(int server=0; server<clientSocket.length; server++){
            try{
                send(server, new String(data), offset);
            }
            catch(Exception e){
                System.out.println("tPC(): Couldn't send to server cuz of" + e);
            }
        }
                
        for(int server=0; server<clientSocket.length; server++){
            boolean loop = true;
            while(loop == true){
                try{
                    System.out.println(receive(server));
                    loop = false;
                }
                catch(Exception e){
                    try{
                        send(server, data, offset);
                        loop = true;
                    }
                    catch (Exception ae) {
                        System.out.println("tPC(): Couldn't send to server cuz of" + ae);
                    }
                }
            }
        }
    }
    
        
    
}