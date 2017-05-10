package soc.code.renderPackage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import soc.code.logicPackage.Board;
import soc.code.logicPackage.BuildSite;
import soc.code.logicPackage.Player.PlayerInventory;
import soc.code.logicPackage.Tile;
import soc.code.multiplayerPackage.ClientSetup;

/**
 * This class will be responsible for rendering the game itself based on the
 * board object and the Player objects. It will also pass down the mouse events
 * from the GUI JFrame to the UI that is stored inside of this panel. At the end
 * of this class' paint method will be a call to the UI's paint method. The
 * class will also be responsible for initializing and controlling the UI
 * Object.
 * 
 * @author Greg
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

	// the reference to the board used in this game.
	private Board gameBoard = null;
	// the client manager that will be used to access the painting parts of each
	// player:
	private ClientSetup clientManager = null;

	// this coordinate is the top left x,y coordinate of the actual tiles. There
	// are no tiles connected to it becuase it is a hexagon but it is the lowest
	// x and lowest y that any of the tiles are drawn in.
	private Point tileZeroPoint = null;

	private BuildSite selectedBuildSite = null;
	private BuildSite hovoredBuildSite = null;

	private final Color BACKGROUND_COLOR = new Color(68, 199, 255);

	// constructor requires reference to board that will be generated.
	public GamePanel(Board GB, ClientSetup clientManager) {
		gameBoard = GB;
		this.clientManager = clientManager;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		// setting the size and border spacing:
		this.setSize(Tile.TILE_WIDTH * 6, Tile.TILE_WIDTH * 6);
		tileZeroPoint = new Point(this.getWidth() / 2 - Tile.TILE_WIDTH * 5 / 2 - 1,
				this.getHeight() / 2 - Tile.TILE_WIDTH * 5 / 2 - 1);

		// initializing the default selected buildsite to not produce null
		// pointer errors:
		selectedBuildSite = new BuildSite(0, 0, false);
		hovoredBuildSite = new BuildSite(0, 0, false);
		// setting the positions of each of the buildsites:
		setBuildSitePostitions(gameBoard.getBuildSites());

		// initializing the images:
		Tile.initializeResourceImages();
	}

	/**
	 * This method will be called when the board is initialized and will be in
	 * charge of setting the x and y positions of each of the build sites in the
	 * array.
	 * 
	 * @param sites
	 *            the array lists that need to have their contents' positions
	 *            set.
	 */
	private void setBuildSitePostitions(ArrayList<ArrayList<BuildSite>> sites) {

		int rowCount = 0; // the row that the program is currently setting.
		// this number is the y distance between each of the consecutive
		// buildsites in one row.
		int jaggedSpacingDistanceApart = (int) (Math.tan(Math.toRadians(30)) * (Tile.TILE_WIDTH / 2));
		// x spacing in between tiles:
		int xSpacingBetweenBuildSites = Tile.TILE_WIDTH / 2;

		for (int i = -5; i < 6; i += 2) {
			// will help alternate the jagged Spacing:
			int jaggedSpacingMultiplier = 1;
			if (i > 0)
				jaggedSpacingMultiplier = 0;
			for (int n = 0; n < sites.get((i + 5) / 2).size(); n++) {
				// setting the x and y coordinates of the buildsites:
				sites.get((i + 5) / 2).get(n).setX((int) (tileZeroPoint.getX() - Tile.TILE_WIDTH / 4
						+ Math.abs(i) * Tile.TILE_WIDTH / 4 + n * xSpacingBetweenBuildSites));
				sites.get((i + 5) / 2).get(n)
						.setY((int) (tileZeroPoint.getY() // the zeroing y value
															// of the board.
								// The row that the program is assigning
								// coordinates to:
								+ ((i + 5) / 2) * (jaggedSpacingDistanceApart + Tile.TILE_HEIGHT / 2)
								// creating the jagged rows:
								+ jaggedSpacingMultiplier * jaggedSpacingDistanceApart));
				// alternating the jagged space Multiplyer:
				if (jaggedSpacingMultiplier == 0)
					jaggedSpacingMultiplier = 1;
				else
					jaggedSpacingMultiplier = 0;
			}
		}
	}

	public void paint(Graphics g) {

		// drawing the background:
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		// drawing the tiles:
		this.drawTiles(g, false);

		// drawing the roads between build sites:
		this.drawRoads(g, gameBoard.getBuildSites());

		// drawing the build sites:
		this.drawBuildSites(g, gameBoard.getBuildSites(), 12);

		// drawing the numbers on top of the tiles:
		this.drawTileNumbers(g);

		// drawing the player inventories on the client's screen.
		this.drawPlayerInventories(g);
	}

	/**
	 * Draws the inventories of the players towards the left side of the screen
	 * in order that they connected.
	 */
	public void drawPlayerInventories(Graphics g) {
		// drawing the inventory of each player.
		for (int i = 0; i < clientManager.getAllPlayers().length; i++)
			clientManager.getAllPlayers()[i].getInventory().paint(g, this.getWidth() - PlayerInventory.WIDTH,
					PlayerInventory.HEIGHT * i);
	}

	private void drawRoads(Graphics g, ArrayList<ArrayList<BuildSite>> sites) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(8));
		for (int j = 0; j < sites.size(); j++)
			for (int k = 0; k < sites.get(j).size(); k++) {
				int[] currentRoadIDs = sites.get(j).get(k).getRoadIDValues();
				BuildSite[] adjacentBuildSites = gameBoard.getAdjacentBuildSites(k, j);
				for (int i = 0; i < currentRoadIDs.length; i++)
					if (currentRoadIDs[i] >= 0) {
						// A road will be painted.

						// setting the proper color:
						g2.setColor(clientManager.getAllPlayers()[currentRoadIDs[i]].getPreferedColor());

						// drawing the line from one site to the other:
						if (adjacentBuildSites[i] != null)
							g2.drawLine(sites.get(j).get(k).getX(), sites.get(j).get(k).getY(),
									adjacentBuildSites[i].getX(), adjacentBuildSites[i].getY());
					}
			}
	}

	/**
	 * This method will draw the resource gain numbers on top of the tiles. The
	 * numbers will come from the array of tile objects itself.
	 * 
	 * @param g
	 *            the graphics that the numbers will be drawn on.
	 */
	private void drawTileNumbers(Graphics g) {
		// setting the font of the numbers:
		g.setFont(new Font("Arial", Font.BOLD, 24));

		// The extremes:
		final int STARTING_X = Tile.TILE_WIDTH / 2;
		final int STARTING_Y = Tile.TILE_HEIGHT / 2;
		int currentXRow = STARTING_X;

		// Drawing the individual hexes:
		for (int i = -2; i < gameBoard.numberOfRows() - 2; i++) {
			currentXRow = STARTING_X + Math.abs(i) * Tile.TILE_WIDTH / 2 - Tile.TILE_WIDTH;
			for (int n = 0; n < gameBoard.numberOfColumns(i + 2); n++) {
				// these are the middle x and y values of the text that has to
				// be displayed.
				int currentTileX = (int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX());
				int currentTileY = (int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY());

				// if it is a desert it shouldn't draw any number:
				if (gameBoard.getTileAt(i + 2, n).getResourceNumber() > 0) {
					// painting the background circle for the numbers:
					g.setColor(new Color(255, 241, 188));
					int circleRadius = 20;
					g.fillOval(currentTileX - circleRadius, currentTileY - circleRadius, circleRadius * 2,
							circleRadius * 2);
					g.setColor(Color.BLACK);
					g.drawOval(currentTileX - circleRadius, currentTileY - circleRadius, circleRadius * 2,
							circleRadius * 2);

					// changing the color of the number based on its value.
					// Sixes and eights are bright red while twos and twelves
					// are black.
					if (gameBoard.getTileAt(i + 2, n).getResourceNumber() < 7)
						g.setColor(new Color(
								Math.min((gameBoard.getTileAt(i + 2, n).getResourceNumber() - 2) * 64, 255), 0, 0));
					else
						g.setColor(new Color(Math.min(
								(Math.abs((gameBoard.getTileAt(i + 2, n).getResourceNumber() - 12)) * 64), 255), 0, 0));

					// painting the actual text of the numbers:
					drawCenteredText(g, Integer.toString(gameBoard.getTileAt(i + 2, n).getResourceNumber()),
							currentTileX, currentTileY);
				}
			}
		}
	}

	/**
	 * This method will draw given text on the given graphics object that has
	 * its center at the given x and y coordinates.
	 * 
	 * @param g
	 *            the graphics that the text will be drawn on.
	 * @param text
	 *            the text that will be drawn
	 * @param x
	 *            the x coordinate of the center of the text.
	 * @param y
	 *            the y coordinate of the center of the text.
	 */
	private void drawCenteredText(Graphics g, String text, int x, int y) {
		// Get the FontMetrics
		FontMetrics metrics = g.getFontMetrics(g.getFont());
		// Determine the X coordinate for the text
		int xString = x - (metrics.stringWidth(text)) / 2;
		// Determine the Y coordinate for the text (note we add the ascent, as
		// in java 2d 0 is top of the screen)
		int yString = y + metrics.getAscent() / 2 - 2;
		// Draw the String
		g.drawString(text, xString, yString);
	}

	/**
	 * This method is in charge of drawing the build sites and making them the
	 * appropriate colors based on what is built on them and if they are being
	 * selected/are selected or not.
	 * 
	 * @param g
	 *            the graphics that the build sites are to be drawn on.
	 * @param sites
	 *            the array of build sites to be drawn.
	 * @param radius
	 *            the radius of each of the build site circles.
	 */
	private void drawBuildSites(Graphics g, ArrayList<ArrayList<BuildSite>> sites, int radius) {
		// drawing the build sites to test their placments:
		Graphics2D g2 = (Graphics2D) g;
		for (int j = 0; j < sites.size(); j++)
			for (int k = 0; k < sites.get(j).size(); k++) {

				// always trying to paint the settlements even if it is
				// selected.
				if (sites.get(j).get(k).getBuildingType() == 1) {
					// Drawing the settlements with the color that the player
					// that they are is.
					g.setColor(clientManager.getAllPlayers()[sites.get(j).get(k).getPlayerID()].getPreferedColor());
					g.fillOval(sites.get(j).get(k).getX() - radius, sites.get(j).get(k).getY() - radius, radius * 2,
							radius * 2);
					g.setColor(Color.BLACK);
					g2.setStroke(new BasicStroke(1));
					g.drawOval(sites.get(j).get(k).getX() - radius, sites.get(j).get(k).getY() - radius, radius * 2,
							radius * 2);
				}

				// always trying to paint the settlements even if it is
				// selected.
				if (sites.get(j).get(k).getBuildingType() == 2) {
					// Drawing the settlements with the color that the player
					// that they are is.
					g.setColor(clientManager.getAllPlayers()[sites.get(j).get(k).getPlayerID()].getPreferedColor());
					fillHexagon(g2, radius * 3 / 2, sites.get(j).get(k).getX(), sites.get(j).get(k).getY());
					g.setColor(Color.BLACK);
					g2.setStroke(new BasicStroke(1));
					drawHexagon(g2, radius * 3 / 2, sites.get(j).get(k).getX(), sites.get(j).get(k).getY());
				}

				if (sites.get(j).get(k) == selectedBuildSite) {
					g2.setColor(Color.BLUE);
					g2.setStroke(new BasicStroke(5));
					g2.drawOval(sites.get(j).get(k).getX() - radius, sites.get(j).get(k).getY() - radius, radius * 2,
							radius * 2);
				} else if (sites.get(j).get(k) == hovoredBuildSite) {
					g2.setColor(Color.PINK);
					g2.setStroke(new BasicStroke(3));
					g2.drawOval(sites.get(j).get(k).getX() - radius, sites.get(j).get(k).getY() - radius, radius * 2,
							radius * 2);
				}
			}
		g2.setStroke(new BasicStroke(1));
	}

	/**
	 * This method is responsible for drawing the tile array on top of the
	 * passed graphics object. It can either draw colored hexagons representing
	 * the different resource tiles or it can draw the actual images of each of
	 * the tiles. This mode is specified by the drawTileImages boolean variable.
	 * 
	 * @param g
	 *            the graphics that the tiles should be drawn on.
	 * @param drawTileImages
	 *            if true, then the program will draw the images from the hard
	 *            drive; if false, it will draw colored hexes to represent each
	 *            of the tiles.
	 */
	private void drawTiles(Graphics g, boolean drawTileImages) {
		if (drawTileImages) {

		} else {
			// The extremes:
			final int STARTING_X = Tile.TILE_WIDTH / 2;
			final int STARTING_Y = Tile.TILE_HEIGHT / 2;
			int currentXRow = STARTING_X;

			// Drawing the individual hexes:
			for (int i = -2; i < gameBoard.numberOfRows() - 2; i++) {
				currentXRow = STARTING_X + Math.abs(i) * Tile.TILE_WIDTH / 2 - Tile.TILE_WIDTH;
				for (int n = 0; n < gameBoard.numberOfColumns(i + 2); n++) {
					switch (gameBoard.getTileAt(i + 2, n).getType()) {
					case WOOD:
						g.drawImage(Tile.getT_WoodImage(),
								(int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX() - Tile.TILE_WIDTH / 2),
								(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()
										- Tile.TILE_HEIGHT / 2),
								null);
						break;
					case WHEAT:
						g.drawImage(Tile.getT_WheatImage(),
								(int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX() - Tile.TILE_WIDTH / 2),
								(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()
										- Tile.TILE_HEIGHT / 2),
								null);
						break;
					case BRICK:
						g.drawImage(Tile.getT_BrickImage(),
								(int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX() - Tile.TILE_WIDTH / 2),
								(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()
										- Tile.TILE_HEIGHT / 2),
								null);
						break;
					case ORE:
						g.drawImage(Tile.getT_OreImage(),
								(int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX() - Tile.TILE_WIDTH / 2),
								(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()
										- Tile.TILE_HEIGHT / 2),
								null);
						break;
					case SHEEP:
						g.drawImage(Tile.getT_SheepImage(),
								(int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX() - Tile.TILE_WIDTH / 2),
								(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()
										- Tile.TILE_HEIGHT / 2),
								null);
						break;
					case DESERT:
						g.drawImage(Tile.getT_DesertImage(),
								(int) ((currentXRow += Tile.TILE_WIDTH) + tileZeroPoint.getX() - Tile.TILE_WIDTH / 2),
								(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()
										- Tile.TILE_HEIGHT / 2),
								null);
						break;
					}
					drawTile((Graphics2D) g, Tile.TILE_HEIGHT / 2, (int) ((currentXRow) + tileZeroPoint.getX()),
							(int) (STARTING_Y + (i + 2) * 3 * Tile.TILE_HEIGHT / 4 + tileZeroPoint.getY()));
				}
			}
		}
	}

	/**
	 * This method draws a regular hexagon with the given center x,y point and
	 * the given side length on the specified graphics object. The hexagon it
	 * draws has one of its points facing down.
	 * 
	 * @param g2
	 *            the graphics that the hexagon will be drawn on.
	 * @param sideLength
	 *            the side length of the regular hexagon
	 * @param centerY
	 *            the y value of the center of the hexagon
	 * @param centerX
	 *            the x value of the center of the hexagon.
	 */
	private void drawTile(Graphics2D g2, int sideLength, int centerY, int centerX) {
		int[] xValues = { centerX - sideLength, centerX - sideLength / 2, centerX + sideLength / 2,
				centerX + sideLength, centerX + sideLength / 2, centerX - sideLength / 2 };
		double yOffset = sideLength * Math.sqrt(3) / 2;
		int[] yValues = { centerY, (int) (centerY - yOffset), (int) (centerY - yOffset), centerY,
				(int) (centerY + yOffset), (int) (centerY + yOffset) };
		// g2.fillPolygon(yValues, xValues, 6);
		g2.setColor(Color.GRAY);
		g2.setStroke(new BasicStroke(6));
		g2.drawPolygon(yValues, xValues, 6);
		g2.setStroke(new BasicStroke(1));
	}

	/**
	 * This method draws a regular hexagon with the given center x,y point and
	 * the given side length on the specified graphics object. The hexagon it
	 * draws has one of its points facing down.
	 * 
	 * @param g2
	 *            the graphics that the hexagon will be drawn on.
	 * @param sideLength
	 *            the side length of the regular hexagon
	 * @param centerY
	 *            the y value of the center of the hexagon
	 * @param centerX
	 *            the x value of the center of the hexagon.
	 */
	private void fillHexagon(Graphics2D g2, int sideLength, int centerY, int centerX) {
		int[] xValues = { centerX - sideLength, centerX - sideLength / 2, centerX + sideLength / 2,
				centerX + sideLength, centerX + sideLength / 2, centerX - sideLength / 2 };
		double yOffset = sideLength * Math.sqrt(3) / 2;
		int[] yValues = { centerY, (int) (centerY - yOffset), (int) (centerY - yOffset), centerY,
				(int) (centerY + yOffset), (int) (centerY + yOffset) };
		g2.fillPolygon(yValues, xValues, 6);
	}

	/**
	 * This method draws a regular hexagon with the given center x,y point and
	 * the given side length on the specified graphics object. The hexagon it
	 * draws has one of its points facing down.
	 * 
	 * @param g2
	 *            the graphics that the hexagon will be drawn on.
	 * @param sideLength
	 *            the side length of the regular hexagon
	 * @param centerY
	 *            the y value of the center of the hexagon
	 * @param centerX
	 *            the x value of the center of the hexagon.
	 */
	private void drawHexagon(Graphics2D g2, int sideLength, int centerY, int centerX) {
		int[] xValues = { centerX - sideLength, centerX - sideLength / 2, centerX + sideLength / 2,
				centerX + sideLength, centerX + sideLength / 2, centerX - sideLength / 2 };
		double yOffset = sideLength * Math.sqrt(3) / 2;
		int[] yValues = { centerY, (int) (centerY - yOffset), (int) (centerY - yOffset), centerY,
				(int) (centerY + yOffset), (int) (centerY + yOffset) };
		g2.drawPolygon(yValues, xValues, 6);
	}

	public BuildSite getSelectedBuildSite() {
		return selectedBuildSite;
	}

	public BuildSite getHovoredBuildSite() {
		return hovoredBuildSite;
	}

	public Color getBACKGROUND_COLOR() {
		return BACKGROUND_COLOR;
	}

	public Board getGameBoard() {
		return gameBoard;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		selectedBuildSite = gameBoard.getBuildSiteAtPoint(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		hovoredBuildSite = gameBoard.getBuildSiteAtPoint(e.getX(), e.getY());
	}

}