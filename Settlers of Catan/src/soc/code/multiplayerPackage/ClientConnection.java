package soc.code.multiplayerPackage;

import java.awt.Point;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.PendingTrade;
import soc.code.logicPackage.Player;
import soc.code.logicPackage.Tile;

/**
 * This class is responsible for handling one connection to one client. It is an
 * individual thread that sucks all incoming data from the client and spits all
 * the outgoing data to this client.
 * 
 * @author Greg
 */
public class ClientConnection extends Thread {

	// the socket for this connection.
	private Socket clientSocket = null;

	// this is the a local copy of the player variable that will be updated when
	// new info comes into the server.
	private Player clientPlayer = null;

	// Each client connection stores a reference to the host manager so that
	// they can directly reference the other players and their connections.
	private HostSetup hostManager = null;

	// will determine if the player is ready or not:
	private boolean isReady = false;

	private DataSucker dataSucker = null;

	// The variable for keeping track of whether or not it is this player's
	// turn.
	private boolean isTurn = false;
	// The varibale that will also tell the server where the user is at in their
	// turn. If it is true then the user is still in phase 1, but if it is false
	// then they are in phase two and have already rolled the dice.
	private boolean rollingDice = false;

	public boolean isRollingDice() {
		return rollingDice;
	}

	private Board gameBoard = null;

	public ClientConnection(Socket s, Board b, DataSucker ds) {
		clientSocket = s;

		// Initializing the data sucker so that it constantly sucks new data.
		dataSucker = ds;

		gameBoard = b;
		initializePlayer();
	}

	public void run() {

		while (true) {

			// executing the client's command
			doClientCommand(dataSucker.getNextLine());

			try {
				this.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method does the given string command that is provided by the client.
	 * 
	 * @param data
	 *            - the command that the client wants executed.
	 */
	public void doClientCommand(String data) {

		// Then there is an incoming trade request.
		if (data.contains("trade:")) {
			if (hostManager.addTrade(new PendingTrade(ConnectionHelper.decodeTradeRequest(data))))
				System.out.println("Making Trade.");
			else {
				int[] dataInt = ConnectionHelper.decodeTradeRequest(data);
				int[] tradeValues = Arrays.copyOfRange(dataInt, 2, 12);
				// sending the trade request to the client.
				ConnectionHelper.sendTradeRequest(dataInt[0], dataInt[1], tradeValues,
						hostManager.getClientConnections().get(dataInt[1]).getClientSocket());
			}
		}

		// This means the client just built somthing or changed their resource
		// number for some reason so they are sending their updated inventory:
		if (data.contains("Player:")) {
			// recieving the updated player:
			ConnectionHelper.recievePlayerInventory(clientPlayer, data);

			// broadcasting this updated player to all the other clients:
			updateServerWidePlayerInventory();
		}

		// This means that the client has chosen to roll the dice and the server
		// should continue to the normal phase of the client's turn.
		if (data.equals("rolleddice")) {
			rollingDice = false;
		}

		// The client is sending build sites to the host to update the build
		// site array based on what the client has done during their turn.
		if (data.equals("buildsites")) {
			// updating the build site arrays inside of the client gameboard.
			// During this player's turn, this local game board will have its
			// build sites copied to the main game board located inside the main
			// class.
			gameBoard.overwriteBuildSites(ConnectionHelper.recieveBuildSites(dataSucker));

			// Then it updates all of the other player's buildsites excluding
			// the one that just sent the message to update the servers build
			// sites.
			hostManager.updateAllBuildSites(hostManager.getClientConnections().indexOf(this));
		}

		// the client is sending just one of the build sites.
		if (data.equals("buildsite")) {
			// Updating the arrays of build sites to change just the one
			// incoming build site.
			Point changedBuildSiteCoordinate = ConnectionHelper.recieveBuildSite(dataSucker, gameBoard.getBuildSites());

			// Then it updates all of the other player's buildsites excluding
			// the one that just sent the message to update the servers build
			// sites.
			hostManager.updateSingleBuildSite(hostManager.getClientConnections().indexOf(this),
					(int) changedBuildSiteCoordinate.getX(), (int) changedBuildSiteCoordinate.getY());
		}

		// this ends the client's turn.
		if (data.equals("endturn")) {
			isTurn = false;
		}

		// Then the client has pressed ready and is now ready to start the game.
		if (data.equals("ready")) {
			// initializing the client's board object by sending the hosts.
			sendBoardTiles(gameBoard);

			// then sending the other players:
			sendOtherPlayers();

			System.out.println("Player " + clientPlayer.getUsername() + " is ready.");
			isReady = true;
		}
	}

	/**
	 * Updates the inventory of this player on every client. It sends a message
	 * to each of the clients with the updated inventory. It is used if a client
	 * builds somthing or when the dice is rolled.
	 */
	public void updateServerWidePlayerInventory() {
		// going through each client and updating each of their copies of this
		// players inventory including the client who owns this player.
		for (ClientConnection i : hostManager.getClientConnections())
			ConnectionHelper.sendPlayerInventory(i.getPlayer(), hostManager.getClientConnections().indexOf(i),
					this.getClientSocket());

	}

	/**
	 * This method starts the turn by sending a message to the client and
	 * setting the is turn variable to true.
	 */
	public void startTurn() {
		isTurn = true;
		ConnectionHelper.printString("startturn", clientSocket);
	}

	/**
	 * This method begins the part before the actual turn starts where the user
	 * has to press the button to roll the dice.
	 */
	public void startDiceRollProcess() {
		rollingDice = true;
		ConnectionHelper.printString("rolldice", clientSocket);
	}

	public void startGameProcess(HostSetup hostManager, Board gameBoard) {
		// making sure the client is ready...
		ConnectionHelper.printString("startingGame", clientSocket);

		// setting the host manager reference so that this client can reference
		// the other player values.
		this.hostManager = hostManager;
	}

	/**
	 * This method sends the tiles in the gameboard to the client socket saved
	 * in this object. It sends it linearly row by row from top to bottom. Each
	 * tile appears to the client like <RES_TYPE><RES_NUMBER>
	 * 
	 * @param gameBoard
	 */
	private void sendBoardTiles(Board gameBoard) {
		/*
		 * In order to send the board, the program lays out the tiles one row at
		 * a time linearly. Each tile will be sent in the same notation. To send
		 * one tile, the host must send the tile resource type and the tile's
		 * resource number. It will spell out the actual word for the resource
		 * type and then the resource collection number. It ends the
		 * transmission with a | to tell the client what is in the transmission.
		 * Ex) wood8|
		 */
		System.out.println("Sending Tile data to " + clientPlayer.getUsername());

		for (ArrayList<Tile> arr : gameBoard.getTileArray())
			for (Tile i : arr) {
				String messageToSend = "";
				messageToSend += i.toString();
				messageToSend += i.getResourceNumber() + "|";
				ConnectionHelper.printString(messageToSend, clientSocket);
			}
	}

	/**
	 * Sends the other player values to the client that this connection handles.
	 * This is so that the client can render the board with all the proper
	 * player colors and usernames.
	 */
	private void sendOtherPlayers() {
		// In order to send each player, the methods will have to send the
		// username of the player and then the color of the player. It is spaced
		// out like so: <username>|<Red>|<Green>|<Blue>
		System.out.println("Sending Player data to " + clientPlayer.getUsername());

		// starting off by sending how many players there are:
		// P:<number of Players>
		ConnectionHelper.printString("P:" + hostManager.getClientConnections().size(), clientSocket);

		// going through and sending each player.
		for (int i = 0; i < hostManager.getClientConnections().size(); i++) {
			String toSend = hostManager.getClientConnections().get(i).getPlayer().getUsername() + "|";
			toSend += hostManager.getClientConnections().get(i).getPlayer().getPreferedColor().getRed() + "|";
			toSend += hostManager.getClientConnections().get(i).getPlayer().getPreferedColor().getGreen() + "|";
			toSend += hostManager.getClientConnections().get(i).getPlayer().getPreferedColor().getBlue();
			ConnectionHelper.printString(toSend, clientSocket);
		}
	}

	/**
	 * This method is responsible for disconnecting the client that this
	 * connction is attatched to. It sends a message to the client telling it to
	 * disconnect and displays messages to the user updating them on when they
	 * have been disconnected.
	 */
	public void disconnectClient() {
		ConnectionHelper.printString("disconnect", clientSocket);
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Unable to close socket " + clientSocket.getInetAddress());
			e.printStackTrace();
		}
		System.out.println(clientPlayer.getUsername() + " disconnected.");
	}

	/**
	 * This method gets the player's data from the client and then initializes
	 * the player object inside this class based on the data from the client.
	 */
	public void initializePlayer() {
		ConnectionHelper.printString("playerbio", clientSocket);

		String username = dataSucker.getNextLine();
		clientPlayer = new Player(username.substring(username.indexOf(":") + 1));
		System.out.println("Client at " + clientSocket.getInetAddress() + " sent Bio file...");
		System.out.println("Username: \"" + clientPlayer.getUsername() + "\"");

		// parsing the color from its line:
		// <r>,<g>,<b>
		String colorLine = dataSucker.getNextLine();
		int r = Integer.parseInt(colorLine.substring(colorLine.indexOf(":") + 1, colorLine.indexOf(",")));
		colorLine = colorLine.substring(colorLine.indexOf(",") + 1);
		int g = Integer.parseInt(colorLine.substring(0, colorLine.indexOf(",")));
		colorLine = colorLine.substring(colorLine.indexOf(",") + 1);
		int b = Integer.parseInt(colorLine);

		// setting the prefered color variable of the client:
		clientPlayer.setPreferedColor(r, g, b);
	}

	public Player getPlayer() {
		return clientPlayer;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

	public boolean isTurn() {
		return isTurn;
	}

	public void setTurn(boolean isTurn) {
		this.isTurn = isTurn;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public boolean getReadiness() {
		return isReady;
	}

	public Board getGameBoard() {
		return gameBoard;
	}

	public void setGameBoard(Board gameBoard) {
		this.gameBoard = gameBoard;
	}

}