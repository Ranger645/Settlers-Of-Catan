package soc.code.renderPackage;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
import soc.code.logicPackage.Player.PlayerInventory;
import soc.code.logicPackage.Tile;
import soc.code.multiplayerPackage.ClientSetup;
import soc.code.multiplayerPackage.ConnectionHelper;

/**
 * This class is the frame that will hold the game and all Graphical features
 * involved with the game. It will hold the panels necessary for displaying the
 * GameSetup JPanel and the Game JPanel. It will Also store high level methods
 * for controlling these and switching between them. Finally, it will be
 * responsible for funneling the mouse events down to the Game JPanel and
 * subsequently the UI Object Inside of the Game JPanel
 * 
 * @author Greg
 */
public class GUI extends JFrame implements ActionListener, KeyListener {

	private GamePanel mainPanel = null;
	private boolean isHostGUI = false;
	private ClientSetup clientManager = null;

	private int IOStatus = 0; // 0 if all disabled, 1 if rolling dice, 2 if all
								// enabled.
	private JMenuItem buildSettlement, endTurn, buildCity, buildRoad, rollDice, buyDevCard, playDevCard = null;
	private JMenuItem[] tradeButtons = null;

	// keeps track of whether the user is in the process of selecting a road.
	private boolean selectingRoad = false;

	public GUI(Board gameBoard, ClientSetup clientManager, boolean isHost) {

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(Tile.TILE_WIDTH * 6 + PlayerInventory.WIDTH, Tile.TILE_WIDTH * 6);
		this.setTitle("Stettlers of Catan");
		// this.setResizable(false);
		
		this.isHostGUI = isHost;
		this.clientManager = clientManager;

		mainPanel = new GamePanel(gameBoard, clientManager);
		this.add(mainPanel);

		if (!isHostGUI) {

			JMenuBar menuBar = new JMenuBar();
			menuBar.setPreferredSize(new Dimension(this.getWidth(), 25));

			JMenu commandMenu = new JMenu("Commands");

			buildSettlement = new JMenuItem("Build Settlement");
			buildSettlement.addActionListener(this);

			buildCity = new JMenuItem("Build City");
			buildCity.addActionListener(this);

			buildRoad = new JMenuItem("Build Road");
			buildRoad.addActionListener(this);

			rollDice = new JMenuItem("Roll Dice");
			rollDice.addActionListener(this);

			buyDevCard = new JMenuItem("Buy Development Card");
			buyDevCard.addActionListener(this);

			playDevCard = new JMenuItem("Play Development Card");
			playDevCard.addActionListener(this);

			endTurn = new JMenuItem("End Turn");
			endTurn.addActionListener(this);

			JMenu tradeMenu = new JMenu("Trade");
			tradeButtons = new JMenuItem[clientManager.getAllPlayers().length];
			for (int i = 0; i < tradeButtons.length; i++) {
				tradeButtons[i] = new JMenuItem(
						(i + 1) + ". Trade with " + clientManager.getAllPlayers()[i].getUsername());
				tradeButtons[i].addActionListener(this);
				tradeMenu.add(tradeButtons[i]);
				// Permenently disabling this player's own trade button.
				if (clientManager.getAllPlayers()[i].getUsername().equals(clientManager.getLocalPlayer().getUsername()))
					tradeButtons[i].setEnabled(false);
			}

			this.addKeyListener(this);

			commandMenu.add(buildSettlement);
			commandMenu.add(buildCity);
			commandMenu.add(buildRoad);
			commandMenu.add(buyDevCard);
			commandMenu.add(playDevCard);
			commandMenu.add(rollDice);
			commandMenu.add(endTurn);

			menuBar.add(commandMenu);
			menuBar.add(tradeMenu);

			this.setJMenuBar(menuBar);

			// starting the IO disabled until it is the client's turn.
			closeIO();
		}

		// only shows the board if it is a client doing the controlling.
		if (!isHostGUI)
			this.setVisible(true);
	}

	/**
	 * This just opens the UI that is needed before the dice are rolled.
	 */
	public void openDiceRollUI() {
		IOStatus = 1;
		playDevCard.setEnabled(true);
		rollDice.setEnabled(true);
	}

	/**
	 * Will enable all of the components that the user can interact with
	 */
	public void openTurnIO() {
		IOStatus = 2;
		rollDice.setEnabled(false);
		buildSettlement.setEnabled(true);
		buildCity.setEnabled(true);
		buildRoad.setEnabled(true);
		buyDevCard.setEnabled(true);
		endTurn.setEnabled(true);
	}

	/**
	 * will disable all of the components that the user can interact with
	 */
	public void closeIO() {
		IOStatus = 0;
		rollDice.setEnabled(false);
		buildSettlement.setEnabled(false);
		buildCity.setEnabled(false);
		buildRoad.setEnabled(false);
		endTurn.setEnabled(false);
		playDevCard.setEnabled(false);
		buyDevCard.setEnabled(false);
	}

	public GamePanel getMainPanel() {
		return mainPanel;
	}

	public void setMainPanel(GamePanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public int getIOStatus() {
		return IOStatus;
	}

	public void setIOStatus(int newStatus) {
		this.IOStatus = newStatus;
	}

	/**
	 * Adds a road starting at the selected build site. It then waits for the
	 * user to select a VALID build site and then creates the road logically.
	 */
	public void addRoad() {
		// Making sure this is true so nothing else happens:
		System.out.println("Choose an adjacent build site to complete the road.");
		selectingRoad = true;
		buildRoad.setEnabled(true);
		buildRoad.setText("Cancel Road Construction");

		BuildSite roadStartSite = mainPanel.getSelectedBuildSite();

		// getting a selected road from the user as long as it takes to be a
		// valid road.
		boolean inValidSite = false;
		do {
			// waiting for the user to pick the road.
			mainPanel.setSelectedBuildSite(roadStartSite);
			while (mainPanel.getSelectedBuildSite() == roadStartSite && selectingRoad) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (selectingRoad) {
				// if the user did not cancel the build.
				inValidSite = !mainPanel.getGameBoard().areBuildSitesAdjacent(
						new Point(roadStartSite.getArrX(), roadStartSite.getArrY()),
						new Point(mainPanel.getSelectedBuildSite().getArrX(),
								mainPanel.getSelectedBuildSite().getArrY()));

				// displaying status message:
				if (inValidSite) {
					System.out.println("That build site is not adjacent to the selected Build Site.");
				} else
					System.out.println("Building Road from (" + roadStartSite.getArrX() + ", " + roadStartSite.getArrY()
							+ ") to (" + mainPanel.getSelectedBuildSite().getArrX() + ", "
							+ mainPanel.getSelectedBuildSite().getArrY() + ")");
			}

		} while (inValidSite);

		buildRoad.setText("Build Road");

		if (selectingRoad) {
			// if the user did not cancel the build.

			// After both build sites have been recieved, now they have to be
			// propperly formatted.
			BuildSite[] adjacentSites = mainPanel.getGameBoard().getAdjacentBuildSites(roadStartSite.getArrX(),
					roadStartSite.getArrY());

			if (adjacentSites[0] == mainPanel.getSelectedBuildSite()) {
				// Then the road has to be built to the left of the first
				// selected
				// build site.
				// Checking to make sure there is not already a road at the spot
				// the user wants to build on.
				if (!(roadStartSite.getRoadIDValues()[0] > -1 && roadStartSite.getRoadIDValues()[0] == mainPanel
						.getSelectedBuildSite().getRoadIDValues()[2])) {
					roadStartSite.setRoadIDValue(BuildSite.ROAD_LEFT_ID, clientManager.getPlayerIndex());
					mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_RIGHT_ID,
							clientManager.getPlayerIndex());
				} else {
					System.out.println("You cannot build a road there, there is already a road built there.");
				}
			}

			if (adjacentSites[2] == mainPanel.getSelectedBuildSite()) {
				// Then the road has to be built to the right of the first
				// selected build site.
				// Checking to make sure there is not already a road at the spot
				// the user wants to build on.
				if (!(roadStartSite.getRoadIDValues()[2] > -1 && roadStartSite.getRoadIDValues()[2] == mainPanel
						.getSelectedBuildSite().getRoadIDValues()[0])) {
					roadStartSite.setRoadIDValue(BuildSite.ROAD_RIGHT_ID, clientManager.getPlayerIndex());
					mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_LEFT_ID,
							clientManager.getPlayerIndex());
				} else {
					System.out.println("You cannot build a road there, there is already a road built there.");
				}
			}

			if (adjacentSites[1] == mainPanel.getSelectedBuildSite()) {
				// Then the road has to be built below or above the first
				// selected build site.
				// Checking to make sure there is not already a road at the spot
				// the user wants to build on.
				if (!(roadStartSite.getRoadIDValues()[1] > -1 && roadStartSite.getRoadIDValues()[1] == mainPanel
						.getSelectedBuildSite().getRoadIDValues()[1])) {
					roadStartSite.setRoadIDValue(BuildSite.ROAD_MIDDLE_ID, clientManager.getPlayerIndex());
					mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_MIDDLE_ID,
							clientManager.getPlayerIndex());
				} else {
					System.out.println("You cannot build a road there, there is already a road built there.");
				}
			}

			// sending the updated build sites.
			sendBuildSite(mainPanel.getSelectedBuildSite());
			sendBuildSite(roadStartSite);
		} else
			System.out.println("Road Construction Cancelled.");
	}

	/**
	 * Adds a road starting at the selected build site. It then waits for the
	 * user to select a VALID build site and then creates the road logically.
	 */
	public void addRoadNoThread() {
		// Making sure this is true so nothing else happens:
		System.out.println("Choose an adjacent build site to complete the road.");
		selectingRoad = true;

		BuildSite roadStartSite = mainPanel.getSelectedBuildSite();

		// getting a selected road from the user as long as it takes to be a
		// valid road.
		boolean inValidSite = false;
		do {
			// waiting for the user to pick the road.
			mainPanel.setSelectedBuildSite(roadStartSite);
			while (mainPanel.getSelectedBuildSite() == roadStartSite && selectingRoad) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (selectingRoad) {
				// if the user did not cancel the build.
				inValidSite = !mainPanel.getGameBoard().areBuildSitesAdjacent(
						new Point(roadStartSite.getArrX(), roadStartSite.getArrY()),
						new Point(mainPanel.getSelectedBuildSite().getArrX(),
								mainPanel.getSelectedBuildSite().getArrY()));

				// displaying status message:
				if (inValidSite) {
					System.out.println("That build site is not adjacent to the selected Build Site.");
				} else
					System.out.println("Building Road from (" + roadStartSite.getArrX() + ", " + roadStartSite.getArrY()
							+ ") to (" + mainPanel.getSelectedBuildSite().getArrX() + ", "
							+ mainPanel.getSelectedBuildSite().getArrY() + ")");
			}

		} while (inValidSite);

		buildRoad.setText("Build Road");

		if (selectingRoad) {
			// if the user did not cancel the build.

			// After both build sites have been recieved, now they have to be
			// propperly formatted.
			BuildSite[] adjacentSites = mainPanel.getGameBoard().getAdjacentBuildSites(roadStartSite.getArrX(),
					roadStartSite.getArrY());

			if (adjacentSites[0] == mainPanel.getSelectedBuildSite()) {
				// Then the road has to be built to the left of the first
				// selected
				// build site.
				// Checking to make sure there is not already a road at the spot
				// the user wants to build on.
				if (!(roadStartSite.getRoadIDValues()[0] > -1 && roadStartSite.getRoadIDValues()[0] == mainPanel
						.getSelectedBuildSite().getRoadIDValues()[2])) {
					roadStartSite.setRoadIDValue(BuildSite.ROAD_LEFT_ID, clientManager.getPlayerIndex());
					mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_RIGHT_ID,
							clientManager.getPlayerIndex());
				} else {
					System.out.println("You cannot build a road there, there is already a road built there.");
				}
			}

			if (adjacentSites[2] == mainPanel.getSelectedBuildSite()) {
				// Then the road has to be built to the right of the first
				// selected build site.
				// Checking to make sure there is not already a road at the spot
				// the user wants to build on.
				if (!(roadStartSite.getRoadIDValues()[2] > -1 && roadStartSite.getRoadIDValues()[2] == mainPanel
						.getSelectedBuildSite().getRoadIDValues()[0])) {
					roadStartSite.setRoadIDValue(BuildSite.ROAD_RIGHT_ID, clientManager.getPlayerIndex());
					mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_LEFT_ID,
							clientManager.getPlayerIndex());
				} else {
					System.out.println("You cannot build a road there, there is already a road built there.");
				}
			}

			if (adjacentSites[1] == mainPanel.getSelectedBuildSite()) {
				// Then the road has to be built below or above the first
				// selected build site.
				// Checking to make sure there is not already a road at the spot
				// the user wants to build on.
				if (!(roadStartSite.getRoadIDValues()[1] > -1 && roadStartSite.getRoadIDValues()[1] == mainPanel
						.getSelectedBuildSite().getRoadIDValues()[1])) {
					roadStartSite.setRoadIDValue(BuildSite.ROAD_MIDDLE_ID, clientManager.getPlayerIndex());
					mainPanel.getSelectedBuildSite().setRoadIDValue(BuildSite.ROAD_MIDDLE_ID,
							clientManager.getPlayerIndex());
				} else {
					System.out.println("You cannot build a road there, there is already a road built there.");
				}
			}

			// sending the updated build sites.
			sendBuildSite(mainPanel.getSelectedBuildSite());
			sendBuildSite(roadStartSite);
		} else
			System.out.println("Road Construction Cancelled.");
	}

	
	/**
	 * Trys to build a settlement at the selected build site.
	 */
	public boolean buildSettlement() {
		// making sure it is ok to build on this tile:
		if (mainPanel.getGameBoard().isValidSettlementSpot(mainPanel.getSelectedBuildSite().getArrX(),
				mainPanel.getSelectedBuildSite().getArrY())) {
			if (mainPanel.getSelectedBuildSite().buildSettlement(clientManager.getPlayerIndex())) {
				// printing status message
				System.out.println("Building settlement at (" + mainPanel.getSelectedBuildSite().getX() + ", "
						+ mainPanel.getSelectedBuildSite().getY() + ")");
				// updating build site array in the network
				sendBuildSite(mainPanel.getSelectedBuildSite());
				return true;
			} else {
				System.out.println("Failed to build settlement: There is already somthing built there.");
				return false;
			}
		} else {
			// printing status message.
			System.out.println("Failed to build settlement: There are buildings adjacent to the proposed build site.");
			return false;
		}
	}

	public void buildRoad() {
		// adding a road at the selected build site.
		if ((mainPanel.getSelectedBuildSite().canBuildRoad(clientManager.getPlayerIndex())) && !selectingRoad) {
			// Starting a thread so it doesn't hold up the graphics
			// thread.
			Thread addRoadThread = new Thread() {
				public void run() {
					addRoad();
					selectingRoad = false;
				}
			};
			addRoadThread.start();
		} else {
			selectingRoad = false;
		}
	}

	public void buildRoadNoThread() {
		// adding a road at the selected build site.
		if ((mainPanel.getSelectedBuildSite().canBuildRoad(clientManager.getPlayerIndex())) && !selectingRoad) {
			// Starting a thread so it doesn't hold up the graphics
			// thread.
			addRoadNoThread();
			selectingRoad = false;
		} else {
			selectingRoad = false;
		}
	}

	/**
	 * This method waits for the client to select the openning settlement and
	 * road and then sends updates it via the host.
	 */
	public void buildOpenningSettlement(int playerIndex) {
		// Deselecting all build sites.
		int currentBuildingCount = 0;
		do {
			BuildSite placeHolder = new BuildSite(-1, -1, false);
			this.mainPanel.setSelectedBuildSite(placeHolder);

			// waiting for the client to select a build site to build a
			// settlement
			// on.
			while (mainPanel.getSelectedBuildSite() == placeHolder)
				try {
					Thread.sleep(50);
					currentBuildingCount += 50;
					if (currentBuildingCount % 10000 == 0)
						System.out.println("Waiting for you to Select a build site: " + (currentBuildingCount / 1000)
								+ " seconds.");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		} while (!buildSettlement());

		// building the road off of the selected build site.
		buildRoadNoThread();
	}

	public JFrame getThisFrame() {
		return this;
	}

	/**
	 * This method is called when the player wants to trade with one of the
	 * players. The player to propose a trade to is given by the passed
	 * playerIndex variable.
	 * 
	 * @param playerIndex
	 *            - the player to propose a trade to.
	 */
	public void proposeTrade(final int playerIndex) {
		System.out.println("Proposing a trade with " + clientManager.getAllPlayers()[playerIndex].getUsername());
		// The thread that will handle getting what the user wants to trade.
		Thread tradeThread = new Thread() {
			public void run() {
				if (mainPanel.getTrader().waitForSelection(clientManager.getAllPlayers()[playerIndex].getUsername(),
						getThisFrame())) {
					int[] tradeValues = null;
					tradeValues = mainPanel.getTrader().getCreatedTrade();

					// Now it has to actually check to see if the trade request
					// is valid:
					boolean isRequestValid = true;
					for (int i = 0; i < clientManager.getLocalPlayer().getInventory()
							.getNumOfResourceCards().length; i++)
						if (clientManager.getLocalPlayer().getInventory().getNumOfResourceCards()[i] < tradeValues[i])
							isRequestValid = false;

					// If it is a valid trade request, sending the trade request
					// to the server.
					if (isRequestValid)
						ConnectionHelper.sendTradeRequest(clientManager.getPlayerIndex(), playerIndex, tradeValues,
								clientManager.getClientSocket());
					else
						System.out.println("You don't have enough cards to make that trade.");

				} else
					System.out.println("Trade Cancelled.");
			}
		};
		tradeThread.start();
	}

	/**
	 * Builds a city at the selected build site.
	 */
	public void buildCity() {
		// building a city.
		if (mainPanel.getSelectedBuildSite().buildCity(clientManager.getPlayerIndex())) {
			// printing statis message.
			System.out.println("Building City at (" + mainPanel.getSelectedBuildSite().getX() + ", "
					+ mainPanel.getSelectedBuildSite().getY() + ")");
			// updating build site array in the network
			sendBuildSite(mainPanel.getSelectedBuildSite());
		} else
			// printing statis message.
			System.out.println("Failed to build City.");
	}

	/**
	 * Plays a development card that the user selects.
	 */
	public void playDevCard() {
		// if the card is successfully played, then the button is disabled
		// because only one card can be played per turn.
		// playDevCard.setEnabled(false);
	}

	/**
	 * Updates the build site that is selected in the main game panel.
	 */
	public void sendBuildSite(BuildSite toSend) {
		// Finding the proper build site to send which needs to be
		// updated.
		for (int i = 0; i < clientManager.getGameBoard().getBuildSites().size(); i++)
			for (int n = 0; n < clientManager.getGameBoard().getBuildSites().get(i).size(); n++)
				if (clientManager.getGameBoard().getBuildSites().get(i).get(n) == toSend) {
					// telling the server to updated the build
					// sites.
					clientManager.sendUpdatedBuildSite(n, i);
					break;
				}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// A button on the gui has been pressed and therefore must have its
		// action executed. This means that it is this clients turn because
		// buttons are only enabled on the clients turn. Every time a button is
		// pressed the client must send the updated build site array lists to
		// the server so the server can distribute the updated ones to the
		// client.
		if (IOStatus == 1)
			if (e.getSource() == rollDice)
				clientManager.rollDice();
			else if (e.getSource() == playDevCard)
				playDevCard();

		if (IOStatus == 2) {
			if (e.getSource() == buildSettlement)
				buildSettlement();
			else if (e.getSource() == endTurn)
				// ends the turn.
				clientManager.endTurn();
			else if (e.getSource() == buildCity)
				buildCity();
			else if (e.getSource() == buildRoad)
				buildRoad();
			else if (e.getSource() == buyDevCard)
				;
			else if (e.getSource() == playDevCard)
				playDevCard();

			// Checking to see if a trade button has been pressed and then
			// opening the dialog to propose a trade to the user.
			for (int i = 0; i < tradeButtons.length; i++)
				if (e.getSource() == tradeButtons[i])
					proposeTrade(Integer.parseInt(tradeButtons[i].getText().substring(0, 1)) - 1);
		}

		clientManager.sendPlayerInventory();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// The shortcuts for all of the commands:
		if (IOStatus == 2)
			if (e.getKeyChar() == 'b')
				buildSettlement();
			else if (e.getKeyChar() == 'c')
				buildCity();
			else if (e.getKeyChar() == 'r')
				buildRoad();
			else if (e.getKeyChar() == 'e')
				clientManager.endTurn();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}