package soc.code.logicPackage;

import java.awt.Point;
import java.util.ArrayList;

/**
 * The board class' main function is to store the array of tiles and the array
 * of build sites. Both will be multidimensional arraylists. It is also
 * responsible for initializing the board either from scratch or overwriting its
 * current build sites with updated ones.
 * 
 * @author Greg
 */
public class Board {

	/*
	 * This is an array list of array lists of tiles. It is essentially a multi
	 * dimensional array list so that each list inside of the parent list can
	 * have different sizes. The sizes of the rows from top to bottom are 3, 4,
	 * 5, 4, 3.
	 */
	private ArrayList<ArrayList<Tile>> gameBoard = null;

	/*
	 * This is the array list of array lists of points on the board. It is
	 * organized into the rows of points that exist on the board. The number of
	 * rows of points is the number of rows of tiles + 1. The references to each
	 * of the points in this array are also stored in each tile object.
	 */
	private ArrayList<ArrayList<BuildSite>> boardBuildSites = null;

	// FINAL VARIABLES:
	// this variable is a quick reference for the total number of tiles in the
	// board:
	private static final int TOTAL_TILES = 19;
	// each of the numbers of the different types of tiles:
	private static final int NUMBER_OF_WOOD = 4;
	private static final int NUMBER_OF_SHEEP = 4;
	private static final int NUMBER_OF_WHEAT = 4;
	private static final int NUMBER_OF_ORE = 3;
	private static final int NUMBER_OF_BRICK = 3;

	public Board() {
		initializeTileLists();
		initializeBuildSites();
	}

	/**
	 * Copies the player value and building type variables from the passed array
	 * into the buildsite array that is storred inside this Board object.
	 * 
	 * @param newSites
	 *            - the arraylist of arraylists to be set to the one indide this
	 *            object.
	 */
	public void overwriteBuildSites(ArrayList<ArrayList<BuildSite>> newSites) {
		for (int i = 0; i < newSites.size(); i++)
			for (int n = 0; n < newSites.get(i).size(); n++) {
				boardBuildSites.get(i).get(n).setBuildingType(newSites.get(i).get(n).getBuildingType());
				boardBuildSites.get(i).get(n).setPlayerID(newSites.get(i).get(n).getPlayerID());
				for (int j = 0; j < boardBuildSites.get(i).get(n).getRoadIDValues().length; j++)
					boardBuildSites.get(i).get(n).setRoadIDValue(i, newSites.get(i).get(n).getRoadIDValues()[i]);
			}
	}

	/**
	 * Checks to see if the build site at the given x and y coordinates can have
	 * a settlement built on it.
	 * 
	 * @param x
	 * @param y
	 * @return true if it is ok to build or false if there are other settlments
	 *         on this tile or directly adjacent.
	 */
	public boolean isValidSettlementSpot(int x, int y) {
		boolean isValid = true;

		// Testing the adjacent build sites.
		for (BuildSite site : getAdjacentBuildSites(x, y)) {
			if (site != null && site.getBuildingType() > 0)
				isValid = false;
		}

		return isValid;
	}

	/**
	 * Returns whether or not the two specified coordinates are adjacent to each
	 * other in the array of build sites.
	 * 
	 * @param site1
	 *            - the x and y coordinates of the first site.
	 * @param site2
	 *            - the x and y coordinates of the second site.
	 * @return true if they are adjacent, false if they are not adjacent.
	 */
	public boolean areBuildSitesAdjacent(Point site1, Point site2) {
		BuildSite[] adjacentSites = getAdjacentBuildSites((int) site1.getX(), (int) site1.getY());
		boolean isAdjacent = false;
		for (int i = 0; i < adjacentSites.length; i++)
			if (adjacentSites[i] != null && adjacentSites[i].getArrX() == site2.getX()
					&& adjacentSites[i].getArrY() == site2.getY())
				isAdjacent = true;
		return isAdjacent;
	}

	/**
	 * Gets the adjacent build site references.
	 * 
	 * @return the references to the build sites adjacent to the given build
	 *         site position.
	 */
	public BuildSite[] getAdjacentBuildSites(int x, int y) {
		BuildSite[] adjacentSites = null;
		ArrayList<BuildSite> adjacentSiteList = new ArrayList<BuildSite>();

		// getting the left most site.
		if (x - 1 >= 0)
			adjacentSiteList.add(boardBuildSites.get(y).get(x - 1));
		else
			adjacentSiteList.add(null);

		// Getting the middle site.
		// Prepping the x value to be the same as the adjacent build site above
		// or below the current build site.
		int xOffset = 0;
		if (boardBuildSites.get(y).get(x).isPointUp() && y - 1 >= 0) {
			xOffset = (boardBuildSites.get(y - 1).size() - boardBuildSites.get(y).size()) / 2;
			adjacentSiteList.add(boardBuildSites.get(y - 1).get(x + xOffset));
		} else if (!boardBuildSites.get(y).get(x).isPointUp() && y + 1 < boardBuildSites.size()) {
			xOffset = (boardBuildSites.get(y + 1).size() - boardBuildSites.get(y).size()) / 2;
			adjacentSiteList.add(boardBuildSites.get(y + 1).get(x + xOffset));
		} else
			adjacentSiteList.add(null);

		// getting the right most build site.
		if (x + 1 < boardBuildSites.get(y).size())
			adjacentSiteList.add(boardBuildSites.get(y).get(x + 1));
		else
			adjacentSiteList.add(null);

		adjacentSites = new BuildSite[3];
		for (int i = 0; i < adjacentSites.length; i++)
			adjacentSites[i] = adjacentSiteList.get(i);

		return adjacentSites;
	}

	/**
	 * This constructor builds a board with the specified tile data.
	 * 
	 * @param tileData
	 *            the tile data.
	 */
	public Board(String[] tileData) {
		// initializing the tile array and setting the outside max size to 5.
		// This is the height of the multi dimensional arraylist.
		gameBoard = new ArrayList<ArrayList<Tile>>(5); // this is [y][x] form.
		// setting the maximum sizes of all of the arraylists:
		for (int i = -2; i < 3; i++)
			gameBoard.add(new ArrayList<Tile>(5 - Math.abs(i)));

		int dataCount = 0;
		for (int i = 0; i < gameBoard.size(); i++)
			for (int n = 0; n < 5 - Math.abs(i - 2); n++) {
				// Addressing each of the tiles' slots
				String resType = "";
				// while it is still a letter...
				while (((int) tileData[dataCount].charAt(0)) > 57) {
					resType += tileData[dataCount].substring(0, 1);
					tileData[dataCount] = tileData[dataCount].substring(1);
				}

				// initializing the tiles and setting their resource types:
				if (resType.equals("wood"))
					gameBoard.get(i).add(new Tile(Tile.RESOURCE_TYPE.WOOD));
				else if (resType.equals("wheat"))
					gameBoard.get(i).add(new Tile(Tile.RESOURCE_TYPE.WHEAT));
				else if (resType.equals("brick"))
					gameBoard.get(i).add(new Tile(Tile.RESOURCE_TYPE.BRICK));
				else if (resType.equals("sheep"))
					gameBoard.get(i).add(new Tile(Tile.RESOURCE_TYPE.SHEEP));
				else if (resType.equals("ore"))
					gameBoard.get(i).add(new Tile(Tile.RESOURCE_TYPE.ORE));
				else if (resType.equals("desert"))
					gameBoard.get(i).add(new Tile(Tile.RESOURCE_TYPE.DESERT));

				// setting the resource type
				gameBoard.get(i).get(n).setResourceNumber(
						Integer.parseInt(tileData[dataCount].substring(0, tileData[dataCount].length() - 1)));

				// incrementing the data counter...
				dataCount++;
			}

		// initializing the build sites.
		initializeBuildSites();
	}

	/**
	 * This method initializes the lists of tiles from scratch or overwrites all
	 * existing tiles.
	 */
	private void initializeTileLists() {
		// initializing the tile array and setting the outside max size to 5.
		// This is the height of the multi dimensional arraylist.
		gameBoard = new ArrayList<ArrayList<Tile>>(5); // this is [y][x] form.

		// setting the maximum sizes of all of the arraylists:
		for (int i = -2; i < 3; i++)
			gameBoard.add(new ArrayList<Tile>(5 - Math.abs(i)));

		// Generating all of the tiles from scratch:
		Tile[] allTiles = new Tile[TOTAL_TILES];

		int count = 0;
		int lockedCount = 0;
		// adding the wood tiles:
		while (count < NUMBER_OF_WOOD)
			allTiles[count++] = new Tile(Tile.RESOURCE_TYPE.WOOD);
		lockedCount = count;
		// adding the wheat tiles:
		while (count < NUMBER_OF_WHEAT + lockedCount)
			allTiles[count++] = new Tile(Tile.RESOURCE_TYPE.WHEAT);
		lockedCount = count;
		// adding the sheep tiles:
		while (count < NUMBER_OF_SHEEP + lockedCount)
			allTiles[count++] = new Tile(Tile.RESOURCE_TYPE.SHEEP);
		lockedCount = count;
		// adding the ore tiles:
		while (count < NUMBER_OF_ORE + lockedCount)
			allTiles[count++] = new Tile(Tile.RESOURCE_TYPE.ORE);
		lockedCount = count;
		// adding the brick tiles:
		while (count < NUMBER_OF_BRICK + lockedCount)
			allTiles[count++] = new Tile(Tile.RESOURCE_TYPE.BRICK);
		// setting the last tile to desert tile:
		allTiles[count] = new Tile(Tile.RESOURCE_TYPE.DESERT);

		// shuffling the list of tiles:
		for (int i = 0; i < allTiles.length; i++) {
			// swapping the tile with a random one:
			int random = (int) (Math.random() * allTiles.length);
			Tile holder = allTiles[i];
			allTiles[i] = allTiles[random];
			allTiles[random] = holder;
		}

		// setting the resource numbers of the tiles in order after it has been
		// shuffled:
		int tileResourceNumberCount = 0;
		// getting a local copy of the array of numbers in order:
		int[] tileNumbers = getTileNumbers();
		for (int i = 0; i < allTiles.length; i++)
			// setting the tile resource number:
			if (allTiles[i].getType() != Tile.RESOURCE_TYPE.DESERT)
				allTiles[i].setResourceNumber(tileNumbers[tileResourceNumberCount++]);

		// going through the gameBoard's multidimensional arraylist to set each
		// value to one of the shuffled ones.
		// count = 0;
		// for (int i = -2; i < 3; i++)
		// for (int n = 0; n < 5 - Math.abs(i); n++)
		// gameBoard.get(i + 2).add(allTiles[count++]);
		setGameBoard(allTiles);
	}

	/**
	 * This method will add the tile objects to the game board 2D array in a
	 * spiral pattern.
	 * 
	 * @param allTiles
	 *            - the tiles to add to the gameBoard array.
	 */
	private void setGameBoard(Tile[] allTiles) {
		gameBoard.get(0).add(allTiles[0]);
		gameBoard.get(0).add(allTiles[1]);
		gameBoard.get(0).add(allTiles[2]);

		gameBoard.get(1).add(allTiles[11]);
		gameBoard.get(1).add(allTiles[12]);
		gameBoard.get(1).add(allTiles[13]);
		gameBoard.get(1).add(allTiles[3]);

		gameBoard.get(2).add(allTiles[10]);
		gameBoard.get(2).add(allTiles[17]);
		gameBoard.get(2).add(allTiles[18]);
		gameBoard.get(2).add(allTiles[14]);
		gameBoard.get(2).add(allTiles[4]);

		gameBoard.get(3).add(allTiles[9]);
		gameBoard.get(3).add(allTiles[16]);
		gameBoard.get(3).add(allTiles[15]);
		gameBoard.get(3).add(allTiles[5]);

		gameBoard.get(4).add(allTiles[8]);
		gameBoard.get(4).add(allTiles[7]);
		gameBoard.get(4).add(allTiles[6]);
	}

	/**
	 * This method initializes the build sites and adds them to the tiles that
	 * they are adjacent to.
	 */
	private void initializeBuildSites() {
		// -----------------------------------BUILD_SITES--------------------------------------//
		// This next part of the constructor will handle the creation of the
		// build sites on the board. It will also handle the distribution of
		// them into their proper adjacent tiles. Therefore, up to 6 separate
		// tiles should all contain the reference to each individual build site
		// as well as the multidimensional arraylist of buildsites.

		// initializing the array of build sites. It has the same number of rows
		// as the gameboard arraylist of tiles plus 1. It is in [y][x] form.
		boardBuildSites = new ArrayList<ArrayList<BuildSite>>(6);

		// the pointUp/pointdown variable that needs to be different for each
		// build site. False = down, True = up.
		boolean pointDirection = false;
		// initializing each of the build sites:
		int arraySetValue = 0;
		for (int i = -5; i < 6; i += 2) {
			// initialising the 1D array lists:
			boardBuildSites.add(new ArrayList<BuildSite>(12 - Math.abs(i)));
			// setting the starting point direction which is different if it is
			// creating the top of the board versus the bottom of the board.
			if (i < 0)
				pointDirection = false;
			else
				pointDirection = true;
			for (int n = 0; n < 12 - Math.abs(i); n++) {
				// initializing the build sites and adding them to the array
				// lists.
				boardBuildSites.get(arraySetValue).add(new BuildSite(n, (i + 5) / 2, pointDirection));
				// alternating the point direction.
				pointDirection = !pointDirection;
			}
			arraySetValue++;
		}

		// Adding each of the top buildsites to the tiles that they need to be
		// added to:
		for (int i = 0; i < gameBoard.size(); i++)
			addTiles(gameBoard.get(i), boardBuildSites.get(i), boardBuildSites.get(i + 1));
	}

	/**
	 * This method adds the appropriate build sites to the appropriate tiles in
	 * all the array lists.
	 * 
	 * @param tileArray
	 * @param topSites
	 * @param bottomSites
	 */
	private void addTiles(ArrayList<Tile> tileArray, ArrayList<BuildSite> topSites, ArrayList<BuildSite> bottomSites) {
		if (topSites.size() < bottomSites.size())
			for (int n = 0; n < tileArray.size(); n++) {
				// adding the three build sites on top of the tile to the tile's
				// build site array.
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2));
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2 + 1));
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2 + 2));

				// adding the bottom values:
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2 + 1));
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2 + 2));
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2 + 3));
			}
		else if (topSites.size() > bottomSites.size())
			for (int n = 0; n < tileArray.size(); n++) {
				// adding the three build sites on the bottom of the tile to the
				// tile's
				// build site array.
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2));
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2 + 1));
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2 + 2));

				// adding the top values:
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2 + 1));
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2 + 2));
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2 + 3));
			}
		else
			for (int n = 0; n < tileArray.size(); n++) {
				// adding the three build sites on the bottom of the tile to the
				// tile's
				// build site array.
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2));
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2 + 1));
				tileArray.get(n).addTileBuildSite(bottomSites.get(n * 2 + 2));

				// adding the top values:
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2));
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2 + 1));
				tileArray.get(n).addTileBuildSite(topSites.get(n * 2 + 2));
			}
	}

	public Tile getTileAt(int row, int col) {
		return gameBoard.get(row).get(col);
	}

	public ArrayList<ArrayList<Tile>> getTileArray() {
		return gameBoard;
	}

	/**
	 * @return the number of rows in the board array.
	 */
	public int numberOfRows() {
		return gameBoard.size();
	}

	/**
	 * Returns the number of columns in the given row. Since it is a jagged
	 * array, not all are the same for each row.
	 * 
	 * @param row
	 * @return
	 */
	public int numberOfColumns(int row) {
		return gameBoard.get(row).size();
	}

	/**
	 * gets the build site nearest to the x and y coordinates given by the
	 * method message.
	 * 
	 * @param x
	 * @param y
	 * @return - the build site closest to the x and y coordinates.
	 */
	public BuildSite getBuildSiteAtPoint(int x, int y) {
		BuildSite toReturn = null;
		int distance = 100000;
		for (int i = 0; i < boardBuildSites.size(); i++)
			for (int n = 0; n < boardBuildSites.get(i).size(); n++) {
				int distanceSquared = (int) (Math.pow(boardBuildSites.get(i).get(n).getX() - x, 2)
						+ Math.pow(boardBuildSites.get(i).get(n).getY() - y, 2));
				if (distanceSquared < distance) {
					distance = distanceSquared;
					toReturn = boardBuildSites.get(i).get(n);
				}
			}
		return toReturn;
	}

	public ArrayList<ArrayList<BuildSite>> getBuildSites() {
		return boardBuildSites;
	}

	/**
	 * Generates the proper number order for the tile numbers
	 * 
	 * @returns the array containing the proper order of the tile numbers
	 */
	private static int[] getTileNumbers() {
		int[] tileNumbers = new int[18];
		tileNumbers[0] = 5;
		tileNumbers[1] = 2;
		tileNumbers[2] = 6;
		tileNumbers[3] = 3;
		tileNumbers[4] = 8;
		tileNumbers[5] = 10;
		tileNumbers[6] = 9;
		tileNumbers[7] = 12;
		tileNumbers[8] = 11;
		tileNumbers[9] = 4;
		tileNumbers[10] = 8;
		tileNumbers[11] = 10;
		tileNumbers[12] = 9;
		tileNumbers[13] = 4;
		tileNumbers[14] = 5;
		tileNumbers[15] = 6;
		tileNumbers[16] = 3;
		tileNumbers[17] = 11;
		return tileNumbers;
	}
}