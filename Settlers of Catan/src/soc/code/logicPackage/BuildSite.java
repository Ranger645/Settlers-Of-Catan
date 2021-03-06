package soc.code.logicPackage;

/**
 * This is the buildsite class. It is responsible for storing what is on and who
 * is on a buildsite. It also contains methods for building different buildings
 * and roads.
 * 
 * @author Greg
 */
public class BuildSite {

	// These are points for determining where the build site is located in
	// different circumstances. The x and y are points on the board while the
	// arrx and arry are points in an array.
	private int x, y, arrX, arrY = 0;

	// This is the variable that determines what type of building is located on
	// this point. A 0 is no building, a 1 is a settlment, and a 2 is a city.
	private int buildingType = 0;

	// This is the variable that stores the ID of the player that this buildsite
	// belongs to. A -1 means that it does not belong to any player because
	// nothing has beeen built on it yet.
	private int playerID = -1;

	// This array of 3 integers will store which player ID owns the road in a
	// given direction off of this build site. The order for direction in the
	// array is 0 = left, 1 = middle, 2 = right. The middle can either be
	// pointed up or down.
	private int[] roadIDValues = null;

	// The values for each direction:
	public static final int ROAD_LEFT_ID = 0;
	public static final int ROAD_MIDDLE_ID = 1;
	public static final int ROAD_RIGHT_ID = 2;
	// specifies whether the triangle created by the 3 edges coming off of this
	// build site points up or down.
	private boolean pointUp = false;

	public BuildSite(int xPos, int yPos, boolean pointUp) {
		arrX = xPos;
		arrY = yPos;
		this.pointUp = pointUp;
		// Initializing the roadID Values. It always has 3 values.
		roadIDValues = new int[3];
		for (int i = 0; i < roadIDValues.length; i++)
			roadIDValues[i] = -1;
	}

	/**
	 * Tests to see if the player can build a road on this tile.
	 * 
	 * @param playerIndex
	 *            - the player that will build the road.
	 * @return true if it is ok to build a road for this player or false if it
	 *         is not ok to build.
	 */
	public boolean canBuildRoad(int playerIndex) {
		boolean isValid = true;

		// testing to make sure there is an open road position.
		int openRoads = 0;
		for (int i : roadIDValues)
			if (i < 0)
				openRoads++;
		if (openRoads == 0)
			return false;

		if (playerID == playerIndex)
			return true;

		// Testing if there is a settlement on the buildsite of the appropriate
		// color.
		if (playerID != playerIndex && playerID > -1)
			isValid = false;

		// testing to see if there is another road connected to this site.
		isValid = false;
		for (int i : roadIDValues)
			if (i == playerIndex)
				isValid = true;

		return isValid;
	}

	/**
	 * Sets the road specified by the given direction to the given player ID
	 * 
	 * @param roadDirection
	 * @param playerID
	 */
	public void setRoadIDValue(int roadDirection, int playerID) {
		roadIDValues[roadDirection] = playerID;
	}

	public int getArrX() {
		return arrX;
	}

	public void setArrX(int arrX) {
		this.arrX = arrX;
	}

	public int getArrY() {
		return arrY;
	}

	public void setArrY(int arrY) {
		this.arrY = arrY;
	}

	public int[] getRoadIDValues() {
		return roadIDValues;
	}

	public boolean isPointUp() {
		return pointUp;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setBuildingType(int buildingType) {
		this.buildingType = buildingType;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public int getBuildingType() {
		return buildingType;
	}

	public int getPlayerID() {
		return playerID;
	}

	public boolean buildSettlement(int playerID) {
		if (this.playerID == -1) {
			this.playerID = playerID;
			this.buildingType++;
			return true;
		} else
			return false;
	}

	public boolean buildCity(int playerID) {
		if (this.getPlayerID() == playerID && this.buildingType == 1) {
			this.buildingType++;
			return true;
		} else
			return false;
	}

}
