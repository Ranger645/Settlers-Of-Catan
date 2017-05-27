package soc.code.logicPackage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the player class. The player will be responsible for storing all
 * biological info about the player including their name and color, as well as
 * be able to load a player from a bio file and store thier inventory.
 * 
 * @author Greg
 */
public class Player {

	private String username = "";
	private Color preferedColor = null;

	// this is the inventory of this player that will store all of its resource
	// cards and development cards and victory point counter.
	private PlayerInventory inventory = null;

	public Player(String username) {
		this.username = username;
		preferedColor = Color.BLACK;
		inventory = new PlayerInventory();
	}

	public PlayerInventory getInventory() {
		return inventory;
	}

	public void setPreferedColor(int r, int g, int b) {
		preferedColor = new Color(r, g, b);
		inventory = new PlayerInventory();
	}

	public Color getPreferedColor() {
		return preferedColor;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * This method creates a player object with the same attributes of the
	 * player attribute file in the archive folder. It returns a new player
	 * object.
	 * 
	 * @return the player that is read from the player bio file.
	 */
	public static Player readPlayerFile() {
		File bioFile = new File("archive//bio.txt");
		try {
			// parsing the file:
			Scanner bioScanner = new Scanner(bioFile);
			ArrayList<String> lines = new ArrayList<String>();
			while (bioScanner.hasNextLine())
				lines.add(bioScanner.nextLine());

			// creating the player:
			Player p = new Player(lines.get(0) + "_" + (int) (Math.random() * 1000));
			System.out.println("Created local player " + p.getUsername() + ".");

			// parsing the color from its line:
			// <r>,<g>,<b>
			int r = Integer.parseInt(lines.get(1).substring(0, lines.get(1).indexOf(",")));
			lines.set(1, lines.get(1).substring(lines.get(1).indexOf(",") + 1));
			int g = Integer.parseInt(lines.get(1).substring(0, lines.get(1).indexOf(",")));
			lines.set(1, lines.get(1).substring(lines.get(1).indexOf(",") + 1));
			int b = Integer.parseInt(lines.get(1));

			// setting the color of the player object:
			// p.setPreferedColor(r, g, b);

			// for now the colors will be random.
			p.setPreferedColor((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
			System.out.println("Setting Color to " + p.getPreferedColor().getRed() + ", "
					+ p.getPreferedColor().getGreen() + ", " + p.getPreferedColor().getBlue());

			return p;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Unable to find player bio file. Replace file and restart the program.");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Determines whether this object is equal to p
	 * 
	 * @param p
	 *            - the object to test equality with
	 * @return true if equal false if not equal.
	 */
	public boolean equals(Player p) {
		boolean equal = true;
		if (!p.username.equals(username))
			equal = false;

		if (!p.getPreferedColor().equals(preferedColor))
			equal = false;

		return equal;
	}

	/**
	 * This class is for keeping track of all the things
	 * 
	 * @author fossg
	 */
	public class PlayerInventory {

		// Resource cards will be stored as different amounts of each type. Each
		// type will be stored in this array under their type index. Wood = 0,
		// Wheat = 1, Sheep = 2, Brick = 3, Ore = 4.
		private int[] numOfResourceCards = null;

		// The background image that will be drawn as the backdrop to the
		// player's inventory.
		private BufferedImage inventoryBackground = null;

		public static final int WIDTH = 300;
		public static final int HEIGHT = 120;

		public PlayerInventory() {
			numOfResourceCards = new int[5];
			for(int i = 0; i < 5; i++)
				numOfResourceCards[i] = 2;

			// initializing the image and the graphics to edit the background
			// image.
			inventoryBackground = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D) inventoryBackground.getGraphics();

			Color generalColor = null;
			int rgbSum = getPreferedColor().getBlue() + getPreferedColor().getGreen() + getPreferedColor().getRed();
			if (rgbSum < 255 * 3 / 2)
				generalColor = Color.WHITE;
			else
				generalColor = Color.BLACK;

			g2.setColor(generalColor);
			g2.fillRect(0, 0, WIDTH, HEIGHT);

			int backInset = 4;
			g2.setColor(getPreferedColor());
			g2.fillRect(backInset, backInset, WIDTH - backInset * 2, HEIGHT - backInset * 2);

			g2.setColor(generalColor);
			g2.setFont(new Font("arial", Font.PLAIN, 16));
			g2.drawString(getUsername(), 5, 20);
			g2.setStroke(new BasicStroke(backInset));
			g2.drawLine(0, 20 + backInset + 2, WIDTH, 20 + backInset + 2);
		}

		/**
		 * Paints the players inventory at the given point on the given graphics
		 * object. Only paints the faces of the cards if reveal cards equals
		 * true.
		 * 
		 * @param g
		 * @param x
		 * @param y
		 * @param revealCards
		 */
		public void paint(Graphics g, int x, int y, boolean revealCards) {
			// drawing the background of the inventory:
			g.drawImage(inventoryBackground, x, y, null);
			int cardStartX = 6 + x;
			int cardY = 32 + y;

			// System.out.println("My Cards:");
			// for (int i = 0; i < numOfResourceCards.length; i++)
			// System.out.println(i + ") " + numOfResourceCards[i]);

			// drawing the data displaying how many cards each player has:
			if (revealCards) {
				// counting the number of cards that have to be drawn in the
				// inventory:
				int numberOfCards = 0;
				for (int i = 0; i < numOfResourceCards.length; i++)
					for (int n = 0; n < numOfResourceCards[i]; n++)
						numberOfCards++;

				int xDistance = 0;

				if (numberOfCards == 1)
					cardStartX += WIDTH / 2 - Tile.getC_CardBackImage().getWidth() / 2;
				else
					// distance in between x values of cards equals (totalWidth
					// - cardwidth)/(card# - 1)
					xDistance = ((WIDTH - 10) - Tile.getC_CardBackImage().getWidth()) / (numberOfCards - 1);

				// Drawing the backs of the cards if this is another player
				// besides the one for this client.
				int cardX = cardStartX;
				for (int i = 0; i < numOfResourceCards.length; i++)
					for (int n = 0; n < numOfResourceCards[i]; n++) {
						g.drawImage(Tile.typeValueToCardImage(i), cardX, cardY, null);
						cardX += xDistance;
					}
			} else {
				// counting the number of cards that have to be drawn in the
				// inventory:
				int numberOfCards = 0;
				for (int i = 0; i < numOfResourceCards.length; i++)
					for (int n = 0; n < numOfResourceCards[i]; n++)
						numberOfCards++;

				int xDistance = 0;

				if (numberOfCards == 1)
					cardStartX += WIDTH / 2 - Tile.getC_CardBackImage().getWidth() / 2;
				else
					// distance in between x values of cards equals (totalWidth
					// - cardwidth)/(card# - 1)
					xDistance = ((WIDTH - 10) - Tile.getC_CardBackImage().getWidth()) / (numberOfCards - 1);

				// Drawing the backs of the cards if this is another player
				// besides the one for this client.
				int cardX = cardStartX;
				for (int i = 0; i < numOfResourceCards.length; i++)
					for (int n = 0; n < numOfResourceCards[i]; n++) {
						g.drawImage(Tile.getC_CardBackImage(), cardX, cardY, null);
						cardX += xDistance;
					}
			}
		}

		public void setNumResourceCard(int cardValue, int numOfCards) {
			numOfResourceCards[cardValue] = numOfCards;
		}

		public int[] getNumOfResourceCards() {
			return numOfResourceCards;
		}
	}

}
