package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
import soc.code.renderPackage.ConsoleWindow;

/**
 * This class is for housing helper methods to assist in data spitting and data
 * sucking via sockets and server sockets.
 * 
 * @author developer
 */
public class ConnectionHelper {

	// static ConsoleWindow CW = new ConsoleWindow();

	/**
	 * This method starts the tramsimssion with the user by sending the keyword
	 * "buildsites". Then, this method sends the build site information to the
	 * client saved in this object. It sends them linearly row by row in the
	 * following notation: <PLAYER_INDEX>,<BUILDING_TYPE>
	 * 
	 * @param gameBoard
	 * @param toSendSocket
	 */
	public static void sendBoardBuildSites(Board gameBoard, Socket toSendSocket) {
		// this triggers the reciever to start the build site reading process.
		ConnectionHelper.printString("buildsite", toSendSocket);

		// sending all of the actual build sites.
		for (ArrayList<BuildSite> arr : gameBoard.getBuildSites())
			for (BuildSite i : arr) {
				String messageToSend = "BS";
				messageToSend += i.getPlayerID() + ",";
				messageToSend += i.getBuildingType() + "|";
				ConnectionHelper.printString(messageToSend, toSendSocket);
			}
	}

	/**
	 * Recieves a transmission of buildsites from the given socket. Does not
	 * account for initial "buildsite" line from whoever is sending the data
	 * that identifies the data.
	 * 
	 * @param toRecieveSocket
	 *            - the socket to recieve the build sites on.
	 * @return - a arraylist of arraylists of the build sites that were
	 *         recieved.
	 */
	public static ArrayList<ArrayList<BuildSite>> recieveBuildSites(Socket toRecieveSocket) {
		ArrayList<ArrayList<BuildSite>> buildSites = new ArrayList<ArrayList<BuildSite>>();
		// initializing each of the build sites:
		int arraySetValue = 0;
		for (int i = -5; i < 6; i += 2) {
			// initialising the 1D array lists:
			buildSites.add(new ArrayList<BuildSite>(12 - Math.abs(i)));
			for (int n = 0; n < 12 - Math.abs(i); n++) {
				// getting the data out of the transmitted string about the
				// build site.
				buildSites.get(arraySetValue).add(new BuildSite(0, 0));
				String currentSiteDataLine = ConnectionHelper.readLine(toRecieveSocket);
				// setting the player ID:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1).setPlayerID(
						Integer.parseInt(currentSiteDataLine.substring(0, currentSiteDataLine.indexOf(","))));
				// setting the building type:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1)
						.setBuildingType(Integer.parseInt(currentSiteDataLine
								.substring(currentSiteDataLine.indexOf(",") + 1, currentSiteDataLine.length() - 1)));
			}
			arraySetValue++;
		}
		return buildSites;
	}

	public static ArrayList<ArrayList<BuildSite>> recieveBuildSites(DataSucker ds) {
		ArrayList<ArrayList<BuildSite>> buildSites = new ArrayList<ArrayList<BuildSite>>();

		// creating an array to store all the buildsites as they come in.
		String[] allReadSites = new String[54];
		int numBuildSites = 54;
		int currentSiteIndex = 0;

		// setting up the array of build site commands.
		while (currentSiteIndex < numBuildSites) {
			// Adding all of the current readSites to the array of final
			// Commands:
			String[] readSites = ds.getBuildSiteMessages();
			for (int i = 0; i < readSites.length; i++)
				allReadSites[currentSiteIndex++] = readSites[i].substring(2);
		}
		currentSiteIndex = 0;

		// After the previous while loop has executed, there should be an array
		// of indevidual commands for each build site.
		// Extracting the build sites from the commands that create them.
		int arraySetValue = 0;
		for (int i = -5; i < 6; i += 2) {
			// initialising the 1D array lists:
			buildSites.add(new ArrayList<BuildSite>(12 - Math.abs(i)));
			for (int n = 0; n < 12 - Math.abs(i); n++) {
				// getting the data out of the transmitted string about the
				// build site.
				buildSites.get(arraySetValue).add(new BuildSite(0, 0));
				String currentSiteDataLine = allReadSites[currentSiteIndex++];
				// setting the player ID:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1).setPlayerID(
						Integer.parseInt(currentSiteDataLine.substring(0, currentSiteDataLine.indexOf(","))));
				// setting the building type:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1)
						.setBuildingType(Integer.parseInt(currentSiteDataLine
								.substring(currentSiteDataLine.indexOf(",") + 1, currentSiteDataLine.length() - 1)));
			}
			arraySetValue++;
		}
		return buildSites;
	}

	/**
	 * This method will write the given string to the output stream of the given
	 * socket s.
	 * 
	 * @param toPrint
	 *            - the string that is going to be written to the output stream
	 *            of s
	 * @param s
	 *            - the socket that toPrint will be sent to.
	 */
	public static void printString(String toPrint, Socket s) {
		try {
			for (int i = 0; i < toPrint.length(); i++)
				// sending each individual character in ascii format:
				s.getOutputStream().write((int) toPrint.charAt(i));
			s.getOutputStream().write('\n');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method returns the next line of data that is sent from the sender.
	 * It returns a string representation of this data and does not contain an
	 * endline character (\n) at the end of it.
	 */
	public static String readLine(Socket s) {
		String readString = "";
		try {
			// The data is sent as integer values of the ascii representation of
			// each character of data. So these integers must be converted back
			// into letters in ascii format which is done here:
			char currentValue;
			do {
				currentValue = (char) s.getInputStream().read();
				readString += currentValue;
			} while (currentValue != '\n');
			// Chopping off the endline character at the end of the data
			// message.
			readString = readString.substring(0, readString.length() - 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// CW.getOutputConsole().append(readString + "\n");
		return readString;
	}

	/**
	 * Uses system time functions to calculate response time of the given socket
	 * object in milliseconds.
	 * 
	 * @param s
	 *            - the socket that server is pinging.
	 * @return the amount of time the client takes to respond in milliseconds.
	 */
	public static long getResponseTime(Socket s) {
		long ms = System.currentTimeMillis();

		// requesting ping...
		ConnectionHelper.printString("ping", s);
		// recieving ping response:
		ConnectionHelper.readLine(s);
		// calculating the actual time difference:
		ms -= System.currentTimeMillis();
		ms = Math.abs(ms);

		// sending the ping calculation to the client:
		ConnectionHelper.printString(String.valueOf(ms), s);

		return ms;
	}

	/**
	 * This method takes care of the situation in which the host requests a ping
	 * calculation to detect the strength of the connection. simply sends a
	 * message back to the server that says ping.
	 */
	public static void recievePingRequest(Socket s) {
		// System.out.println("Connection test request recieved, replying...");
		ConnectionHelper.printString("ping", s);
		// System.out.println("Connection test complete, ping = " +
		// ConnectionHelper.readLine(s));
	}

}
