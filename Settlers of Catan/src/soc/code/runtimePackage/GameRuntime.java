package soc.code.runtimePackage;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.Player;
import soc.code.multiplayerPackage.ClientSetup;
import soc.code.multiplayerPackage.HostSetup;
import soc.code.renderPackage.ConsoleWindow;
import soc.code.renderPackage.GUI;

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

		while (true) {
			if (isHost) {
				// what the program is responsible for doing if it is a Host

			} else {
				// what the program is responsible for doing if it is a client,
				// so
				// pretty much nothing... most of the game logic and host
				// communication is handled in the clientsetup thread.
			}
			gui.repaint();
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
			} else if (lastMessage.length() > 5 && lastMessage.substring(0, 4).equals("kick")) {
				// kicking a player from the server.
				if (isHost)
					if (hostManager.kickClient(lastMessage.substring(5, lastMessage.length())))
						System.out.println("Successfully kicked " + lastMessage.substring(5, lastMessage.length()));
					else
						System.out.println("[ERROR] Unable to kick the specified player, player not found.");
				else
					System.out.println("[ERROR] you do not have permission to kick someone, you are not a host.");
			} else if (lastMessage.equals("testBoard")) {
				gameBoard = new Board();
				gui = new GUI(gameBoard);
				break;
			} else if (lastMessage.equals("startGame") && isHost) {
				// starting the game...
				System.out.println("Starting the game...");
				gameBoard = new Board(); // initializing the game board
				gui = new GUI(gameBoard); // initializing the GUI window.
				//gui.setVisible(false);
				playerArray = compilePlayerObjects(); // initializing the local
														// players.
				hostManager.startGameProcess(gameBoard);

			} else if (lastMessage.equals("ready") && !isHost) {
				// readying up...
				System.out.println("you pressed ready.");
				clientManager.setReady(true);
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
			hostManager = new HostSetup();
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