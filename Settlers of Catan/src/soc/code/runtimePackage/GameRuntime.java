package soc.code.runtimePackage;

import java.util.Random;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.Die;
import soc.code.logicPackage.Player;
import soc.code.multiplayerPackage.ClientSetup;
import soc.code.multiplayerPackage.HostSetup;
import soc.code.renderPackage.ConsoleWindow;
import soc.code.renderPackage.GUI;

/**
 * This is the main driver class of the program. It contains the main method as
 * well as several private helper methods for doing different aspects of the
 * game and so the main method doesn't get too cluttered.
 * 
 * @author Greg
 */
public class GameRuntime {

	/* @formatter:off
	 * 
	 * Basic Steps for Main Method:
	 * 		Wait for all clients to connect by testing to see if the user clicks start game in the setup panel.
	 * 		Each time a client connects, send all data about the other players in the game to that client.
	 * 		Loop Through each Game Turn (which will consist of 1 turn for each player) until somebody wins.
	 * 		Player Turn:
	 * 			Play a Development Card (1 Maximum)	OR
	 * 			Role the dice straight away
	 * 			Distribute Resources based on Dice Roles
	 * 			Trade Resource Cards only 			OR
	 * 			Play 1 Development Card per turn 	OR
	 * 			Build any of the buildings
	 * 			Recalculate Victory Points of this Player (Longest Road and Largest Army too)
	 * 		Once the victory point win condition is reached, the clients will be brought to an end game screen
	 * 		and their archived statistic files will be updated.
	 * 
	 * If this program is a host, all user input will be handled on this machine.
	 * 
	 * If this program is a client, all user input will be sent to the server which will process the data and
	 * will update the various player objects on each other client and itself so all human players can keep real
	 * time track of what is progressing in the game. 
	 * 		
	 *@formatter:on
	 */

	// FINAL VARIABLES:
	public static final int MAX_PALYERS = 4;

	// DYNAMIC VARIABLES:
	// the game's console that will print any messages to the user handle the IO
	// side of game setup.
	static ConsoleWindow console = null;
	// the static Board variable that will store the game board.
	static Board gameBoard = null;
	// the JFrame that will house the board:
	static GUI gui = null;
	// a boolean variable in charge keeping track of whether or not there is a
	// game going on.
	static boolean playingGame = false;

	// The player that is running this game. Sometimes it will be the host and
	// sometimes it will be a client.
	static Player localPlayer = null;
	// this is the array of references to all of the host side player objects
	// will be.
	static Player[] playerArray = null;

	static boolean isHost = false;
	// these two variables are the connectivity and data managment objects for
	// if the player is a host or client. Only one can be initialized at one
	// time. The default is client.
	static HostSetup hostManager = null;
	static ClientSetup clientManager = null;

	public static void main(String[] args) {

		// initializing the console:
		console = new ConsoleWindow();
		// making it so that System.out.print() sends String to the console.
		console.setSystemOut();
		System.out.println("Console Linked to Game: Settlers of Catan.");

		// creating the default multiplayer utils, which is as a client:
		localPlayer = Player.readPlayerFile();
		createMultiplayerApparatus();
		// running through Game Setup with the user:
		preGameConsoleDialogue();

		if (isHost) {
			System.out.println(
					"Program acting as host of Game with " + hostManager.getClientConnections().size() + " Players.");

			// what the program is responsible for doing if it is a Host
			playerArray = compilePlayerObjects(); // initializing the local
													// players.
			// Passing the board to the players and making sure they are
			// ready to start the game.
			hostManager.startGameProcess(gameBoard);

			// waiting for everyone to be ready:
			int readinessCounter = 0;
			while (!hostManager.getClientReadiness()) {
				if (readinessCounter % 100 == 0)
					System.out.println(
							"Waiting for clients type \"ready\". Time: " + readinessCounter / 10 + " seconds.");

				readinessCounter++;
				sleepMillis(100);
			}

			// making the order of the players array.
			int[] playerOrder = new int[hostManager.getClientConnections().size()];
			for (int i = 0; i < playerOrder.length; i++)
				playerOrder[i] = i;

			// shuffling the order of the players
			for (int i = 0; i < playerOrder.length; i++) {
				int randInt = new Random().nextInt(playerOrder.length);
				int temp = playerOrder[randInt];
				playerOrder[randInt] = playerOrder[i];
				playerOrder[i] = temp;
			}

			// repainting:
			gui.repaint();

			// \\ MAIN GAME LOOP //\\
			int currentPlayer = 0;
			int playerOrderTracker = 0;

			// Displaying initial messages.
			hostManager.broadcast("Starting Game...");
			hostManager.broadcast("The Order of this game is as follows...");
			for (int i = 0; i < playerOrder.length; i++)
				hostManager.broadcast((i + 1) + ". " + playerArray[playerOrder[i]].getUsername());

			while (true/* Eventually this will be the win testing condition */) {

				// setting this turns player index based on the random order.
				currentPlayer = playerOrder[playerOrderTracker];

				// telling all the clients whose turn it is.
				hostManager.broadcast("It is now "
						+ hostManager.getClientConnections().get(currentPlayer).getPlayer().getUsername() + "'s turn.");

				// Telling the host manager to start the given players turn and
				// wait for it to be done while constantly updating the main
				// game board with the same values that the client is sending to
				// the game board that is stored in the client connection object
				// of the player whose turn it is.
				Board afterTurnBoard = hostManager.doTurn(currentPlayer, gameBoard, gui);

				// Making sure that the build sites in the main game board are
				// perfectly up to date before moving on to the next turn.
				// gameBoard.overwriteBuildSites(afterTurnBoard.getBuildSites());

				// This is the turn rotater.
				if (++playerOrderTracker >= playerArray.length)
					playerOrderTracker = 0;

				// Repainting the gui after everything the turn is over just for
				// good measure.
				gui.repaint();
			}

		} else {

			// waiting for the server to send the entirety of the gameboard
			// object so that the GUI can be initialized.
			System.out.println("Waiting for Server to send Board info...");
			while (clientManager.getGameBoard() == null)
				sleepMillis(20);

			// updating the gameBoard object inside this class so that the main
			// method can pass the reference to the GUI object.
			System.out.println("Creating GUI Window...");
			gameBoard = clientManager.getGameBoard();

			// passing the gameboard reference to the gui object.
			gui = new GUI(gameBoard, clientManager, false);

			while (true) {

				// controlling whether or not the IO should be enabled or
				// disabled epending on whether or not it is the client's turn.
				if (!clientManager.areDiceRolled() && gui.getIOStatus() != 1)
					gui.openDiceRollUI();
				else if (clientManager.isTurn() && clientManager.areDiceRolled() && gui.getIOStatus() != 2)
					gui.openTurnIO();
				else if (gui.getIOStatus() != 0 && !clientManager.isTurn() && clientManager.areDiceRolled())
					gui.closeIO();

				// This is just responsible for constantly updating the
				// gui window so the client has real time data.
				gui.repaint();

				// and then it sleeps to conserver processing power.
				sleepMillis(20);
			}

		}

		// after the game is over, it resets the game playing variable to false:
		// playingGame = false;
	}

	/**
	 * This method handles all of the commands that the user inputs into the
	 * console to set up the game. It returns when the user wants to start the
	 * game.
	 */
	private static void preGameConsoleDialogue() {
		do {
			String lastMessage = console.getNextCommand();
			resurectThread(); // attempting to resurrect the client thread if
								// need be.
			if (lastMessage.equals("") || lastMessage.equals("\n")) {
				System.out.println("[ERROR] Empty String.");
			} else if (lastMessage.equals("setHost")) {
				// setting the program running on this computer to the host of
				// the game.
				System.out.println("Setting this computer to Host Status.");
				isHost = true;
				createMultiplayerApparatus();
				hostManager.start();
			} else if (lastMessage.equals("setClient")) {
				// setting the program running on this computer to a client in
				// the game.
				System.out.println("Setting this computer to Client Status.");
				isHost = false;
				createMultiplayerApparatus();
			} else if (lastMessage.length() > 10 && lastMessage.substring(0, 10).equals("connectTo ")) {
				// connecting to the specified host if this program is a client:
				if (isHost)
					System.out.println("[ERROR] This is not a client.");
				else
					clientManager.connectToHost(lastMessage.substring(10));
			} else if (lastMessage.equals("acceptClients")) {
				// starting the search for clients:
				if (isHost)
					hostManager.start();
				else
					System.out.println("[ERROR] This is not a host.");
			} else if (lastMessage.equals("clear")) {
				// clearing the console window.
				console.getOutputConsole().setText("");
			} else if (lastMessage.equals("testBoard")) {
				// clearing the console window.
				gameBoard = new Board();
				gui = new GUI(gameBoard, clientManager, isHost);
			} else if (lastMessage.length() > 5 && lastMessage.substring(0, 4).equals("kick")) {
				// kicking a player from the server.
				if (isHost)
					if (hostManager.kickClient(lastMessage.substring(5, lastMessage.length())))
						System.out.println("Successfully kicked " + lastMessage.substring(5, lastMessage.length()));
					else
						System.out.println("[ERROR] Unable to kick the specified player, player not found.");
				else
					System.out.println("[ERROR] you do not have permission to kick someone, you are not a host.");
			} else if (lastMessage.equals("startGame") && isHost) {
				// starting the game...
				System.out.println("Starting the game...");
				gameBoard = new Board(); // initializing the game board
				gui = new GUI(gameBoard, clientManager, true); // initializing
																// the GUI
																// window.
				playingGame = true;

			} else if (lastMessage.equals("ready") && !isHost) {
				// readying up...
				System.out.println("you pressed ready.");
				clientManager.setReady(true);
				playingGame = true;
			} else
				System.out.println("[ERROR] Unrecognized Command: " + lastMessage);

			sleepMillis(50);
		} while (!playingGame);
	}

	/**
	 * This method creates either the host variable or the client variable and
	 * sets them other to null depending on the value of isHost (if true, create
	 * the host and set client to null and vise versa).
	 */
	private static void createMultiplayerApparatus() {
		if (isHost) {
			// creating the host:
			gameBoard = new Board();
			hostManager = new HostSetup(gameBoard);
			// setting the host to null:
			clientManager = null;
		} else {
			// creating the client:
			clientManager = new ClientSetup(localPlayer);
			// setting the host to null:
			hostManager = null;
		}
	}

	/**
	 * Sleeps the main thread for mils milliseconds.
	 * 
	 * @param mils
	 */
	public static void sleepMillis(int mils) {
		try {
			Thread.sleep(mils);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method resurrects a dead connection thread if it has been
	 * disconnected. Because the thread cannot be restarted, it must be
	 * reinitialized in order to be runnable again. This is only applicable to
	 * the client Setup because the client is the only one that can be
	 * disconnected in the lifecycle of the program.
	 */
	private static void resurectThread() {
		if (!isHost && !clientManager.getAliveState()) { // only applicable for
															// clients
			clientManager = new ClientSetup(localPlayer);
		}
	}

	private static Player[] compilePlayerObjects() {
		int numberOfPlayers = hostManager.getClientConnections().size();
		Player[] player = new Player[numberOfPlayers];
		for (int i = 0; i < numberOfPlayers; i++)
			player[i] = hostManager.getClientConnections().get(i).getPlayer();
		return player;
	}

}