package soc.code.multiplayerPackage;

import java.net.Socket;
import java.util.ArrayList;

/**
 * This class' purpose is to suck up all the lines of incoming data that it can
 * from a given socket and store all that data in an ArrayList of Strings.
 * 
 * @author Greg
 */
public class DataSucker extends Thread {

	private Socket suckerSocket = null;
	private ArrayList<String> suckedLines = null;
	private boolean isAlive = true;

	public DataSucker(Socket toSuckFrom) {
		suckerSocket = toSuckFrom;
		suckedLines = new ArrayList<String>();
	}

	public void run() {
		while (isAlive) {
			// Adding the incoming data to the buffer.
			suckedLines.add(ConnectionHelper.readLine(suckerSocket));

			try {
				this.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getNextLine() {
		return suckedLines.remove(0);
	}

}
