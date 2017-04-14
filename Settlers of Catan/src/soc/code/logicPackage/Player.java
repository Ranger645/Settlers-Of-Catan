package soc.code.logicPackage;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Player {

	private String username = "";
	private Color preferedColor = null;

	public Player(String username) {
		this.username = username;
		preferedColor = Color.BLACK;
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
			System.out.println("Creating local player " + lines.get(0) + ".");
			Player p = new Player(lines.get(0));

			// parsing the color from its line:
			// <r>,<g>,<b>
			int r = Integer.parseInt(lines.get(1).substring(0, lines.get(1).indexOf(",")));
			lines.set(1, lines.get(1).substring(lines.get(1).indexOf(",") + 1));
			int g = Integer.parseInt(lines.get(1).substring(0, lines.get(1).indexOf(",")));
			lines.set(1, lines.get(1).substring(lines.get(1).indexOf(",") + 1));
			int b = Integer.parseInt(lines.get(1));

			// setting the color of the player object:
			p.setPreferedColor(r, g, b);

			return p;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("[ERROR] Unable to find player bio file. Replace file and restart the program.");
			e.printStackTrace();
		}
		return null;
	}

	private class Builder {

	}

}
