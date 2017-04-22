package soc.code.logicPackage;

public class Die {

	/**
	 * This method gets the sum of the dice rolls based on the given number of
	 * dice and number of sides on each dice.
	 * 
	 * @param numberOfDice
	 *            the number of dice
	 * @param diceFaceNumber
	 *            the number of sides on each dice
	 * @return the sum of all the rolled dice.
	 */
	public static int getDiceRoll(int numberOfDice, int diceFaceNumber) {
		int sum = 0;
		for (int i = 0; i < numberOfDice; i++)
			sum += Math.random() * diceFaceNumber + 1;
		return sum;
	}

}
