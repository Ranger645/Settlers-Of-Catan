package soc.code.logicPackage;

import java.util.ArrayList;

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

		// going through the gameBoard's multidimensional arraylist to set each
		// value to one of the shuffled ones.
		count = 0;
		for (int i = -2; i < 3; i++)
			for (int n = 0; n < 5 - Math.abs(i); n++)
				gameBoard.get(i + 2).add(allTiles[count++]);
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

		// initializing each of the build sites:
		int arraySetValue = 0;
		for (int i = -5; i < 6; i += 2) {
			// initialising the 1D array lists:
			boardBuildSites.add(new ArrayList<BuildSite>(12 - Math.abs(i)));
			for (int n = 0; n < 12 - Math.abs(i); n++)
				// initializing the build sites and adding them to the array
				// lists.
				boardBuildSites.get(arraySetValue).add(new BuildSite(0, 0));
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

	public ArrayList<ArrayList<BuildSite>> getBuildSites() {
		return boardBuildSites;
	}
}