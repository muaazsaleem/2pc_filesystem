package pk.edu.seecs.cs332;

public class FSClient {

  public static void main(String args[]) throws Exception {
    String configFileName = "config.txt";
    ReplicatedFS replFS = new ReplicatedFS(configFileName, 0);

    String filename = "file.txt";
    RemoteFile remoteFile = replFS.open(filename);

    int byteOffset = 0;
    //Write incrementing numbers to the file.
    for (int loopCnt = 0; loopCnt < 128; loopCnt++) {
      String strData = new Integer(loopCnt).toString() + "\n";

      if ( remoteFile.writeBlock(strData.getBytes(), byteOffset) < 0 ) {
        System.out.println("Error writing to file " + remoteFile.getName() + " [LoopCnt=" + loopCnt + "]\n" );
        System.exit(-1);
      }

      byteOffset += strData.length();
    }

    //Actually store the file on all of the remote servers
    if (remoteFile.commit() < 0) {
      System.err.println( "Could not commit changes to file: " + remoteFile.getName() );
      System.exit(-1);
    }

    //Close the file: release resources and commit any changes.
    if (remoteFile.close() < 0) {
      System.err.println("Could not close file: " + remoteFile.getName());
      System.exit(-1);
    }

  }

}
