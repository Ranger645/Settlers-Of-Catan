package soc.code.logicPackage;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
	 * @author fossg This class is for keeping track of all the things
	 */
	public class PlayerInventory {

		// Resource cards will be stored as different amounts of each type. Each
		// type will be stored in this array under their type index. Wood = 0,
		// Wheat = 1, Sheep = 2, Brick = 3, Ore = 4.
		private int[] numOfResourceCards = null;
		
		public static final int WIDTH = 300;
		public static final int HEIGHT = 100;

		public PlayerInventory() {
			numOfResourceCards = new int[5];
		}

		/**
		 * Paints the players inventory at the given point on the given graphics
		 * object.
		 * 
		 * @param g
		 * @param x
		 * @param y
		 */
		public void paint(Graphics g, int x, int y) {
			g.setColor(preferedColor);
			g.fillRect(x, y, WIDTH, HEIGHT);
			g.setColor(Color.GRAY);
			g.drawString(getUsername(), x + 5, y + 20);
		}

	}

}
