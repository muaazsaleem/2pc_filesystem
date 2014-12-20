package pk.edu.seecs.cs332;



import java.util.*;
import java.io.*;
import java.net.*;

public class ReplicatedFS {
  
  
  public static ArrayList<String> serverNames;
  public static  ArrayList<Integer> ports;
  public static int numberOfServers;
  public static int dropRate;
  
  private BufferedReader getFileReader(String filename){
    try{
        return new BufferedReader(new FileReader(new File(filename)));
    }
    catch (Exception e) {
      System.out.println("getFileReader() failed cuz of "+ e);
      return getFileReader(filename);
    }

  }

  private void readInServers(String configFileName){
    BufferedReader reader = getFileReader(configFileName);
    serverNames = new ArrayList<String>();
    ports = new ArrayList<Integer>();
    System.out.println("Files contents are: ");
    try {
            String line = null;
            line = reader.readLine();
            while(line != null){
              
              numberOfServers++;
              System.out.println(line);
              String[] details=line.split(" ");
              serverNames.add(details[0]);
              ports.add(Integer.parseInt(details[1]));
              line = reader.readLine();
            }
    }
    catch (Exception e) {
      System.out.println("Couldn't read in Server details cuz of "+ e);
    }
  }
  public ReplicatedFS(String configFileName, int packetLoss) throws IOException {
	  
    numberOfServers = 0;
    dropRate = packetLoss;
    readInServers(configFileName); 
        
    System.out.println("server ports are: ");
    for(Integer port : ports)
    	System.out.println(port);
      
  }
  
  
  public RemoteFile open(String filename) throws UnknownHostException , IOException {
	  
	  return new RemoteFile(filename);
  }
}



