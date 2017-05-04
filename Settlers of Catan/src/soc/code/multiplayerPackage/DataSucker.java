package soc.code.multiplayerPackage;

import java.net.Socket;
import java.util.ArrayList;

import soc.code.logicPackage.BuildSite;

/**
 * This class' purpose is to suck up all the lines of incoming data that it can
 * from a given socket and store all that data in an ArrayList of Strings.
 * 
 * @author Greg
 */
public class DataSucker extends Thread {

	private Socket suckerSocket = null;
	private ArrayList<String> suckedCommands = null;
	private ArrayList<String> buildSiteMessages = null;
	private boolean isAlive = true;

	public DataSucker(Socket toSuckFrom) {
		suckerSocket = toSuckFrom;
		suckedCommands = new ArrayList<String>();
		buildSiteMessages = new ArrayList<String>();
		this.start();
	}

	public void run() {
		while (isAlive) {

			// Adding the incoming data to the buffer.
			String line = ConnectionHelper.readLine(suckerSocket);

			// if it is a build site command it goes in one section if it is a
			// command it goes in the other section.
			if (line.length() > 1 && line.substring(0, 2).equals("BS"))
				buildSiteMessages.add(line);
			else
				suckedCommands.add(line);

			try {
				this.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getNextLine() {
		// waiting for data to come in...
		while (suckedCommands.size() < 1)
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		// returning the data.
		return suckedCommands.remove(0);
	}
	
	public String getNextBuildSite(){
		while(buildSiteMessages.size() < 1)
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return buildSiteMessages.remove(0);
	}

	/**
	 * Gets the array of build site messages that have been recieved from the
	 * server.
	 * 
	 * @return
	 */
	public String[] getBuildSiteMessages() {
		String[] bsArr = new String[buildSiteMessages.size()];
		for (int i = 0; i < bsArr.length; i++)
			bsArr[i] = buildSiteMessages.remove(0);
		return bsArr;
	}

	public ArrayList<String> getSuckedLines() {
		return suckedCommands;
	}

}
