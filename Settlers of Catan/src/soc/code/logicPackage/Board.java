package soc.code.logicPackage;

import java.util.ArrayList;

public class Board {

	/*
	 * This is an array list of array lists of tiles. It is essentially a multi
	 * dimensional array list so that each list inside of the parent list can
	 * have different sizes.
	 */
	private ArrayList<ArrayList<Tile>> gameBoard = null;

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
}
