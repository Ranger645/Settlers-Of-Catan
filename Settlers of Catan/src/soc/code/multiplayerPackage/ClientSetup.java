package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JOptionPane;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
import soc.code.logicPackage.Player;
import soc.code.logicPackage.Tile;
import soc.code.renderPackage.GUI;
import soc.code.runtimePackage.GameRuntime;

/**
 * This class is meant to handle the multiplayer connection to the host for this
 * client. It will have methods for sending and recieving data to and from the
 * host (the server).
 * 
 * @author Greg
 */
public class ClientSetup extends Thread {

	private Socket clientSocket = null;
	private Player localPlayer;
	private boolean isReady = false;

	private Player[] allPlayers = null;
	private int playerIndex = -1;

	private boolean alive = true;

	// The variable for keeping track of whether or not it is this player's
	// turn.
	private boolean isTurn = false;
	// the varibale that will keep track of whether or not the user is in the
	// process of clicking roll dice.
	private boolean areDiceRolled = true;

	// The data sucker that will handle all incoming data seperatly and store it
	// in an array list of strings.
	private DataSucker dataSucker = null;

	// This is the local copy of the game board variable. It is updated in this
	// class because incoming changes from the server will be recieved here.
	private Board gameBoard = null;

	// needs a copy of the GUI so it can use it as a focus for different message
	// popups.
	private GUI guiReference = null;

	public ClientSetup(Player p, GUI mainGUI) {
		localPlayer = p;
		guiReference = mainGUI;
	}

	public void connectToHost(String ipAddress) {
		try {
			System.out.println("Attempting to connect to " + ipAddress + " on port: " + HostSetup.SERVER_PORT + "...");

			clientSocket = new Socket(ipAddress, HostSetup.SERVER_PORT);
			// initializing the data sucker:
			dataSucker = new DataSucker(clientSocket);

			System.out.println("Successfully connected to Host: " + ipAddress + " on port: " + HostSetup.SERVER_PORT);
			System.out.println("Replying to Connection Tests...");
			this.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Unable to connect to the host: Host not found.");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setGuiReference(GUI guiReference) {
		this.guiReference = guiReference;
	}

	public void run() {

		while (alive) {

			String data = dataSucker.getNextLine();

			doServerCommand(data);

			try {
				this.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("Killing the thread...");
		// the only time it breaks out of this while loop is if the socket
		// disconnects from the host.;
	} // end run method

	private void doServerCommand(String data) {
		// The following set of if statements contaion the programming
		// for what a client should do when a host sends a particular
		// message to it. They will all be individual if statments.

		if (data.contains("winner:")) {
			int winnerIndex = Integer.parseInt(data.substring(data.indexOf(":") + 1));
			System.out.println("\n" + "[SERVER MESSAGE] " + allPlayers[winnerIndex].getUsername() + " HAS WON THE GAME!!!");
			GameRuntime.playingGame = false;
		}

		// This means it is the beginning of the game and the server wants this
		// client to select a build site to build a settlment and start building
		// a road. When the client is finished with this, it will send the same
		// message back to the server.
		if (data.equals("selectBeginning")) {
			System.out.println("[SERVER MESSAGE] Select a build site to build a settlment.");

			// waiting for the gui reference to be set so the program can access
			// it for getting the first settlements.
			while (guiReference == null)
				try {
					this.sleep(20);
					System.out.println("Hello");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			// Setting the selected build site to null so it can get the next
			// one selected.
			guiReference.buildOpenningSettlement(playerIndex);

			// After it has built the settlement and the road, it has to reply
			// to the server so the server knows it is done.
			ConnectionHelper.printString("selectBeginning", clientSocket);
		}

		// This means another player has requested a trade with this one and now
		// this client must decide if it can go through.
		if (data.contains("trade:")) {
			doTrade(data);
		}

		// This means the server needs to updated the player specified in the
		// incoming data.
		if (data.contains("Player:")) {
			// getting the index of the player to recieve:
			int playerIndex = Integer.parseInt(data.substring(data.indexOf(":") + 1, data.indexOf(":") + 2));

			// turning the recieved data into the updated player:
			ConnectionHelper.recievePlayerInventory(allPlayers[playerIndex], data);
		}

		// This means that the client should now start the intro to their turn.
		// It means the program will basically wait for the user to press the
		// "roll dice button".
		if (data.equals("rolldice")) {
			System.out.println("Either play a Development Card or Roll the Dice.");
			areDiceRolled = false;
		}

		// The server is sending build sites to the client to update the build
		// site array.
		if (data.equals("buildsites")) {
			// updating the build site arrays inside of the client gameboard.
			// During this player's turn, this local game board will have its
			// build sites copied to the main game board located inside the main
			// class.
			gameBoard.overwriteBuildSites(ConnectionHelper.recieveBuildSites(dataSucker));
		}

		if (data.equals("buildsite")) {
			// Recieving just one build site from the server if just that one
			// was changed.
			ConnectionHelper.recieveBuildSite(dataSucker, gameBoard.getBuildSites());
		}

		// this just means that the message that the server is sending
		// should be displayed in the client's console window.
		if (data.length() > 1 && data.substring(0, 2).equals("//"))
			System.out.println("[SERVER MESSAGE] " + data.substring(2));

		// This if statement is for if the host is requesting the ping of
		// the client.
		if (data.equals("ping")) {
			ConnectionHelper.recievePingRequest(clientSocket);
		}

		// This is for when the client is sending basic information about
		// the player to the server. This information includes the player's
		// username, desired color (in RGB).
		if (data.equals("playerbio")) {
			sendPlayer();
		}

		// This is a command from the host that signals the client to
		// disconnect. This could be for a number of different reasons. Most
		// likely, the host shut down the game or manually kicked the client
		// from the game.
		if (data.equals("disconnect")) {
			try {
				clientSocket.close();
				alive = false;
				System.out.println("You have been disconnected from the server.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// [ERROR] Unable to close the socket.
				e.printStackTrace();
			}
		}

		// this is the command for starting the game. The host will send out
		// this message and waits for the readiness of each of the players.
		if (data.equals("startingGame")) {
			startGameProcess();
		}

		// this means that the server is telling this client that it is this
		// client's turn to play the game. That means that they can do all of
		// the normal stuff that happens during their turn.
		if (data.equals("startturn")) {
			isTurn = true;
		}
	}

	private void doTrade(String data) {
		int[] allValues = ConnectionHelper.decodeTradeRequest(data);
		// the values of that are involved in the array.
		int[] tradeValues = Arrays.copyOfRange(allValues, 2, 12);
		// Now it has to actually check to see if the trade request
		// is valid:
		boolean isRequestValid = true;
		for (int i = 0; i < getLocalPlayer().getInventory().getNumOfResourceCards().length; i++)
			if (getLocalPlayer().getInventory().getNumOfResourceCards()[i] < tradeValues[i + 5])
				isRequestValid = false;

		String dialog = "Player " + allPlayers[allValues[0]].getUsername() + " is offering ";
		for (int i = 0; i < 5; i++)
			if (tradeValues[i] > 0)
				dialog += tradeValues[i] + " " + Tile.idToString(i) + ", ";
		dialog = dialog.substring(0, dialog.length() - 2);

		dialog += " for ";

		for (int i = 0; i < 5; i++)
			if (tradeValues[i + 5] > 0)
				dialog += tradeValues[i + 5] + " " + Tile.idToString(i) + ", ";
		dialog = dialog.substring(0, dialog.length() - 2);
		dialog += ".";

		if (isRequestValid) {
			// Then the user is presented with options to say yes or no to
			// the trade...
			dialog += " Would you like to accept?";
			int userChoice = JOptionPane.showConfirmDialog(guiReference, dialog, "Trade Dialog",
					JOptionPane.YES_NO_OPTION);
			if (userChoice == 0) {
				System.out.println("Trade Accepted");
				ConnectionHelper.sendTradeRequest(allValues[1], allValues[0], tradeValues, clientSocket);
			} else
				System.out.println("Trade Declined.");
		} else {
			// Then the user is presented with just an option to say no to
			// the trade...
			dialog += " Unfortunatly, we cannot afford this price... yet";
			JOptionPane.showMessageDialog(guiReference, dialog);
		}
	}

	/**
	 * Sends the build sites that are stored on this client to the server to
	 * overwrite the servers build site arrays.
	 */
	public void sendUpdatedBuildSites() {
		ConnectionHelper.sendBoardBuildSites(gameBoard, clientSocket);
	}

	/**
	 * This method sends the player data to the server for their local storage.
	 */
	private void sendPlayer() {
		// The data is printed in this order:
		// USERNAME:<username>
		// COLOR:<preferedRed>,<preferedGreen>,<preferedBlue>
		System.out.println("Sending BIO file...");
		ConnectionHelper.printString("USERNAME:" + localPlayer.getUsername(), clientSocket);
		ConnectionHelper.printString("COLOR:" + localPlayer.getPreferedColor().getRed() + ","
				+ localPlayer.getPreferedColor().getGreen() + "," + localPlayer.getPreferedColor().getBlue(),
				clientSocket);
		System.out.println("BIO file send.");
	}

	/**
	 * Sends an updated version of the players inventory to the server so the
	 * server can distribute it to the other clients.
	 */
	public void sendPlayerInventory() {
		ConnectionHelper.sendPlayerInventory(localPlayer, playerIndex, clientSocket);
	}

	private void startGameProcess() {
		System.out.println("Host has initiated the start of the game, type \"ready\" to ready up for the game.");
		while (!isReady) // waiting for the user to ready up.
			try {
				this.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		// telling the server that this client is ready.
		ConnectionHelper.printString("ready", clientSocket);
		System.out.println("You are now ready for the game, recieving game data...");

		// recieving the tile data and using it to initialize the game board
		// from the server's incoming message.
		gameBoard = initializeGameBoard();
		System.out.println("Tile data Recieved, Board initialized.");

		// recieving the other player data from the server and initializing the
		// array of all the players.
		allPlayers = initializeAllPlayers();
		System.out.println("Other Player Data Received, startup transmission finished.");
	}

	private Player[] initializeAllPlayers() {
		ArrayList<Player> p = new ArrayList<Player>();

		// getting teh number of players:
		String numberOfPlayersTransmission = dataSucker.getNextLineWith("P:");
		int numberOfPlayers = Integer.parseInt(numberOfPlayersTransmission.substring(2));

		// recieving each player transmission.
		for (int i = 0; i < numberOfPlayers; i++) {
			// getting the transmission.
			String playerTransmission = dataSucker.getNextLine();
			String userName = playerTransmission.substring(0, playerTransmission.indexOf("|"));
			playerTransmission = playerTransmission.substring(playerTransmission.indexOf("|") + 1);

			// getting the red value of the color
			int r = Integer.parseInt(playerTransmission.substring(0, playerTransmission.indexOf("|")));
			playerTransmission = playerTransmission.substring(playerTransmission.indexOf("|") + 1);

			// getting the green value of the color
			int g = Integer.parseInt(playerTransmission.substring(0, playerTransmission.indexOf("|")));
			playerTransmission = playerTransmission.substring(playerTransmission.indexOf("|") + 1);

			// getting the blue value of the color
			int b = Integer.parseInt(playerTransmission);

			// creating the player with the given username:
			p.add(new Player(userName));
			// setting the preffered color of the player
			p.get(p.size() - 1).setPreferedColor(r, g, b);
			System.out.println("Recieved player: " + userName + ".");
		}

		// going through the players and finding which one the local player is
		// and setting the local player reference to be the same as the same
		// player in the array.
		for (int i = 0; i < p.size(); i++)
			if (p.get(i).equals(localPlayer)) {
				localPlayer = p.get(i);
				System.out.println("Your client Index is " + i);
				playerIndex = i;
			}

		Player[] pArr = new Player[p.size()];
		for (int i = 0; i < p.size(); i++)
			pArr[i] = p.get(i);

		return pArr;
	}

	/**
	 * Ends the turn locally and tells the server to also end the turn.
	 */
	public void endTurn() {
		isTurn = false;
		ConnectionHelper.printString("endturn", clientSocket);
	}

	/**
	 * This method creates the gameboard based on the tile values sent by the
	 * server.
	 * 
	 * @return the board that is to be rendered on this client.
	 */
	private Board initializeGameBoard() {
		System.out.println("Recieving Tile Data from the Host...");
		String[] tileData = new String[19];
		for (int i = 0; i < 19; i++) {
			// gets the next line with the propper identifier for mass data
			// transmission.
			tileData[i] = dataSucker.getNextLineWith("|");
		}
		Board b = new Board(tileData);
		return b;
	}

	/**
	 * @return whethor or not the socket bound to this host has been
	 *         disconnected or not.
	 */
	public boolean getAliveState() {
		return alive;
	}

	/**
	 * The user has clicked the roll dice button and now the server needs to
	 * know to roll the dice.
	 */
	public void rollDice() {
		System.out.println("You have rolled the Dice...");
		areDiceRolled = true;
		ConnectionHelper.printString("rolleddice", clientSocket);
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}

	public Player getLocalPlayer() {
		return localPlayer;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public boolean areDiceRolled() {
		return areDiceRolled;
	}

	public void setAreDiceRolled(boolean areDiceRolled) {
		this.areDiceRolled = areDiceRolled;
	}

	public int getPlayerIndex() {
		return playerIndex;
	}

	public Player[] getAllPlayers() {
		return allPlayers;
	}

	public void setAllPlayers(Player[] allPlayers) {
		this.allPlayers = allPlayers;
	}

	public void setLocalPlayer(Player localPlayer) {
		this.localPlayer = localPlayer;
	}

	public boolean isTurn() {
		return isTurn;
	}

	public void setTurn(boolean isTurn) {
		this.isTurn = isTurn;
	}

	public Board getGameBoard() {
		return gameBoard;
	}

	public void setGameBoard(Board gameBoard) {
		this.gameBoard = gameBoard;
	}

	public void sendUpdatedBuildSite(int x, int y) {
		// sends the specified build site to the server.
		ConnectionHelper.sendBuildSite(gameBoard.getBuildSites().get(y).get(x), clientSocket, x, y);
	}

}
