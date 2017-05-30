package soc.code.logicPackage;

import java.util.ArrayList;
import java.util.Arrays;

import soc.code.multiplayerPackage.ClientConnection;

public class PendingTrade {

	private int proposalPlayerIndex = -1;
	private int replierPlayerIndex = -1;

	private int[] tradeCards = null;

	public PendingTrade(int[] ints) {
		proposalPlayerIndex = ints[0];
		replierPlayerIndex = ints[1];

		tradeCards = new int[10];
		for (int i = 0; i < 10; i++)
			tradeCards[i] = ints[i + 2];
	}

	/**
	 * Makes the trade between the two player indicies in the given client
	 * connection arraylist.
	 * 
	 * @param clientConnectionList
	 *            - the array of client connection that houses the players.
	 */
	public void makeTrade(ArrayList<ClientConnection> clientConnectionList) {
		Player proposedPlayer = clientConnectionList.get(proposalPlayerIndex).getPlayer();
		Player receivedPlayer = clientConnectionList.get(replierPlayerIndex).getPlayer();
		System.out.println("Making Trade: " + Arrays.toString(tradeCards));

		for (int i = 0; i < receivedPlayer.getInventory().getNumOfResourceCards().length; i++) {
			// adding the cards and subtracting the cards where nessesary to
			// achieve a fair trade.
			proposedPlayer.getInventory().setNumResourceCard(i,
					proposedPlayer.getInventory().getNumOfResourceCards()[i] + tradeCards[i + 5]);
			proposedPlayer.getInventory().setNumResourceCard(i,
					proposedPlayer.getInventory().getNumOfResourceCards()[i] - tradeCards[i]);
			receivedPlayer.getInventory().setNumResourceCard(i,
					receivedPlayer.getInventory().getNumOfResourceCards()[i] + tradeCards[i]);
			receivedPlayer.getInventory().setNumResourceCard(i,
					receivedPlayer.getInventory().getNumOfResourceCards()[i] - tradeCards[i + 5]);
		}
	}

	/**
	 * Tells whether or not the given pending trade values are equal to this one.
	 * 
	 * @param t
	 *            - the pending trade to compare this one to to determine
	 *            equality.
	 * @return true if they are equal or false if they are not equal.
	 */
	public boolean equals(PendingTrade t) {
		for (int i = 0; i < tradeCards.length; i++)
			if (tradeCards[i] != t.getTradeCards()[i])
				return false;

		return true;
	}

	/**
	 * Tells whether or not the given pending trade has the two players swapped
	 * from this one or not.
	 * 
	 * @param t
	 *            - the pending trade to compare this one.
	 * @return true if the proposing player of one is equal to the replying
	 *         player of the other and vise versa.
	 */
	public boolean isCounterPart(PendingTrade t) {
		if (t.getProposalPlayerIndex() != replierPlayerIndex || t.getReplierPlayerIndex() != proposalPlayerIndex)
			return false;

		return true;
	}

	public int[] getTradeCards() {
		return tradeCards;
	}

	public int getProposalPlayerIndex() {
		return proposalPlayerIndex;
	}

	public int getReplierPlayerIndex() {
		return replierPlayerIndex;
	}

}
