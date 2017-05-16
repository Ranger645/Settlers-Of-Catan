package soc.code.multiplayerPackage;

import java.awt.Point;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
import soc.code.logicPackage.Player;

/**
 * This class is for housing helper methods to assist in data spitting and data
 * sucking via sockets and server sockets.
 * 
 * @author developer
 */
public class ConnectionHelper {

	// static ConsoleWindow CW = new ConsoleWindow();

	/**
	 * This method sends data for a player contained in a string to the given
	 * socket. It also must have the index of the player. This method is used by
	 * the server to update all of the clients' local arrays of players. It is
	 * also used by the clients on their turns to update the server and all the
	 * other clients' player arrays when they build somthing. The syntax for
	 * sending a player is as follows: Player:<index>,<numOfWood>,<numOfWheat>,
	 * <numOfSheep>,<numOfBrick>,<numOfOre>,<devcard1>,<devcard2>...
	 * <devcard(n)>|
	 * 
	 * @param p
	 *            - the player to send
	 * @param index
	 *            - the index of the player to send
	 * @param toSend
	 *            - the socket to send the player to.
	 */
	public static void sendPlayerInventory(Player p, int index, Socket toSendSocket) {
		// creating the message intro
		String toSend = "Player:" + index + ",";

		// adding the number of resources to the message
		for (int i = 0; i < p.getInventory().getNumOfResourceCards().length; i++)
			toSend += p.getInventory().getNumOfResourceCards()[i] + ",";

		// adding the message terminator:
		toSend += "|";
	}

	/**
	 * This method updates the given player object with the data that is stored
	 * in the data variable that just came in from a socket.
	 * 
	 * @param p
	 *            - the player to update
	 * @param data
	 *            - the data that the player needs to have.
	 */
	public static void recievePlayerInventory(Player p, String data) {
		// cutting off the initial identification message:
		data = data.substring(data.indexOf(":") + 2);

		// recieving the resources numbers:
		for (int i = 0; i < p.getInventory().getNumOfResourceCards().length; i++) {
			String nextValue = data.substring(0, data.indexOf(","));
			data = data.substring(data.indexOf(",") + 1);
			p.getInventory().setNumResourceCard(i, Integer.parseInt(nextValue));
		}

		// receiving the development cards:
	}

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
		ConnectionHelper.printString("buildsites", toSendSocket);

		// sending all of the actual build sites.
		int x = 0;
		int y = 0;
		for (ArrayList<BuildSite> arr : gameBoard.getBuildSites()) {
			for (BuildSite i : arr) {
				String messageToSend = "BS";
				messageToSend += i.getPlayerID() + ",";
				messageToSend += i.getBuildingType() + ",";
				messageToSend += x + ",";
				messageToSend += y + "|";
				ConnectionHelper.printString(messageToSend, toSendSocket);
				x++;
			}
			x = 0;
			y++;
		}
	}

	public static void sendBuildSite(BuildSite BS, Socket toSendSocket, int x, int y) {
		// this triggers the reciever to start the build site reading process.
		ConnectionHelper.printString("buildsite", toSendSocket);

		// sending one build site.
		String messageToSend = "BS";
		// sending the basic build site data:
		messageToSend += BS.getPlayerID() + ",";
		messageToSend += BS.getBuildingType() + ",";
		// sending the x and y coordinates of this build site:
		messageToSend += x + ",";
		messageToSend += y + ",";
		// sending the updated road ID values:
		messageToSend += BS.getRoadIDValues()[BuildSite.ROAD_LEFT_ID] + ",";
		messageToSend += BS.getRoadIDValues()[BuildSite.ROAD_MIDDLE_ID] + ",";
		messageToSend += BS.getRoadIDValues()[BuildSite.ROAD_RIGHT_ID] + "|";

		// sending all of the above compiled data.
		ConnectionHelper.printString(messageToSend, toSendSocket);
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
		// the pointUp/pointdown variable that needs to be different for each
		// build site. False = down, True = up.
		boolean pointDirection = false;
		// initializing each of the build sites:
		int arraySetValue = 0;
		for (int i = -5; i < 6; i += 2) {
			// setting the starting point direction which is different if it is
			// creating the top of the board versus the bottom of the board.
			if (i < 0)
				pointDirection = false;
			else
				pointDirection = true;
			// initialising the 1D array lists:
			buildSites.add(new ArrayList<BuildSite>(12 - Math.abs(i)));
			for (int n = 0; n < 12 - Math.abs(i); n++) {
				// getting the data out of the transmitted string about the
				// build site.
				buildSites.get(arraySetValue).add(new BuildSite(0, 0, pointDirection));
				String currentSiteDataLine = ConnectionHelper.readLine(toRecieveSocket);
				// setting the player ID:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1).setPlayerID(
						Integer.parseInt(currentSiteDataLine.substring(0, currentSiteDataLine.indexOf(","))));
				// setting the building type:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1)
						.setBuildingType(Integer.parseInt(currentSiteDataLine
								.substring(currentSiteDataLine.indexOf(",") + 1, currentSiteDataLine.length() - 1)));
				// alternating the point direction.
				pointDirection = !pointDirection;
			}
			arraySetValue++;
		}
		return buildSites;
	}

	/**
	 * 
	 * @param ds
	 * @return
	 */
	public static ArrayList<ArrayList<BuildSite>> recieveBuildSites(DataSucker ds) {
		ArrayList<ArrayList<BuildSite>> buildSites = new ArrayList<ArrayList<BuildSite>>();

		// creating an array to store all the buildsites as they come in.
		String[] allReadSites = new String[54];

		for (int i = 0; i < allReadSites.length; i++) {
			// System.out.println(i + ". " + ds.getNextBuildSite());
			allReadSites[i] = ds.getNextBuildSite();
		}

		// After the previous while loop has executed, there should be an array
		// of indevidual commands for each build site.
		// the pointUp/pointdown variable that needs to be different for each
		// build site. False = down, True = up.
		boolean pointDirection = false;
		// Extracting the build sites from the commands that create them.
		int arraySetValue = 0;
		int currentSiteIndex = 0;
		for (int i = -5; i < 6; i += 2) {
			// setting the starting point direction which is different if it is
			// creating the top of the board versus the bottom of the board.
			if (i < 0)
				pointDirection = false;
			else
				pointDirection = true;
			// initialising the 1D array lists:
			buildSites.add(new ArrayList<BuildSite>(12 - Math.abs(i)));
			for (int n = 0; n < 12 - Math.abs(i); n++) {
				// getting the data out of the transmitted string about the
				// build site.
				buildSites.get(arraySetValue).add(new BuildSite(0, 0, pointDirection));
				String currentSiteDataLine = allReadSites[currentSiteIndex++];
				// setting the player ID:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1).setPlayerID(
						Integer.parseInt(currentSiteDataLine.substring(0, currentSiteDataLine.indexOf(","))));
				// setting the building type:
				buildSites.get(arraySetValue).get(buildSites.get(arraySetValue).size() - 1)
						.setBuildingType(Integer.parseInt(currentSiteDataLine
								.substring(currentSiteDataLine.indexOf(",") + 1, currentSiteDataLine.length() - 1)));
				// alternating the point direction.
				pointDirection = !pointDirection;
			}
			arraySetValue++;
		}
		return buildSites;
	}

	/**
	 * Recieves one build site from the specified data sucker and puts it into
	 * the specified array list of build sites at the appropriate place.
	 * 
	 * @param ds
	 * @return
	 */
	public static Point recieveBuildSite(DataSucker ds, ArrayList<ArrayList<BuildSite>> currentBuildSites) {
		// getting the newest build site command:
		String buildSiteCommand = ds.getNextBuildSite();

		// getting the player ID
		int playerID = Integer.parseInt(buildSiteCommand.substring(0, buildSiteCommand.indexOf(",")));
		buildSiteCommand = buildSiteCommand.substring(buildSiteCommand.indexOf(",") + 1);

		// getting the building type:
		int buildingType = Integer.parseInt(buildSiteCommand.substring(0, buildSiteCommand.indexOf(",")));
		buildSiteCommand = buildSiteCommand.substring(buildSiteCommand.indexOf(",") + 1);

		// getting the x value of the changed build site.
		int x = Integer.parseInt(buildSiteCommand.substring(0, buildSiteCommand.indexOf(",")));
		buildSiteCommand = buildSiteCommand.substring(buildSiteCommand.indexOf(",") + 1);

		// getting the y value of the changed build site.
		int y = Integer.parseInt(buildSiteCommand.substring(0, buildSiteCommand.indexOf(",")));
		buildSiteCommand = buildSiteCommand.substring(buildSiteCommand.indexOf(",") + 1);

		int left = Integer.parseInt(buildSiteCommand.substring(0, buildSiteCommand.indexOf(",")));
		buildSiteCommand = buildSiteCommand.substring(buildSiteCommand.indexOf(",") + 1);

		int middle = Integer.parseInt(buildSiteCommand.substring(0, buildSiteCommand.indexOf(",")));
		buildSiteCommand = buildSiteCommand.substring(buildSiteCommand.indexOf(",") + 1);

		int right = Integer.parseInt(buildSiteCommand.substring(0, buildSiteCommand.indexOf("|")));

		System.out.println("(" + left + ", " + middle + ", " + right + ")");

		// changing the values inside of the build site array.
		currentBuildSites.get(y).get(x).setPlayerID(playerID);
		currentBuildSites.get(y).get(x).setBuildingType(buildingType);
		currentBuildSites.get(y).get(x).setRoadIDValue(BuildSite.ROAD_LEFT_ID, left);
		currentBuildSites.get(y).get(x).setRoadIDValue(BuildSite.ROAD_MIDDLE_ID, middle);
		currentBuildSites.get(y).get(x).setRoadIDValue(BuildSite.ROAD_RIGHT_ID, right);

		return new Point(x, y);
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