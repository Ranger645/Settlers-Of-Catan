package soc.code.renderPackage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import soc.code.logicPackage.Tile;

public class TradeGUI extends JFrame implements ActionListener {

	private JSpinner[] myCards, theirCards;
	private JLabel[] resourceLabels;
	private JButton cancelButton, submitButton;

	private int submittedStatus = 0;

	public TradeGUI() {
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setSize(400, 230);
		this.setAlwaysOnTop(true);

		myCards = new JSpinner[5];
		theirCards = new JSpinner[5];
		resourceLabels = new JLabel[5];

		submitButton = new JButton("Propose Trade");
		submitButton.addActionListener(this);
		cancelButton = new JButton("Cancel Trade");
		cancelButton.addActionListener(this);

		int spacing = 20;
		Box totalBox = Box.createVerticalBox();
		Box topBox = Box.createHorizontalBox();
		topBox.add(Box.createHorizontalStrut(50));
		topBox.add(new JLabel("My Offer"));
		topBox.add(Box.createHorizontalStrut(20));
		topBox.add(new JLabel("Their Offer"));
		totalBox.add(topBox);

		for (int i = 0; i < myCards.length; i++) {
			myCards[i] = new JSpinner();
			theirCards[i] = new JSpinner();
			resourceLabels[i] = new JLabel(Tile.idToString(i));

			Box currentBox = Box.createHorizontalBox();
			currentBox.add(resourceLabels[i]);
			currentBox.add(Box.createHorizontalStrut(spacing));
			currentBox.add(myCards[i]);
			currentBox.add(Box.createHorizontalStrut(spacing));
			currentBox.add(theirCards[i]);
			totalBox.add(Box.createVerticalStrut(spacing / 5));
			totalBox.add(currentBox);
		}

		totalBox.add(Box.createVerticalStrut(spacing / 5));
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(cancelButton);
		buttonBox.add(Box.createHorizontalStrut(50));
		buttonBox.add(submitButton);
		totalBox.add(buttonBox);

		JPanel p = new JPanel();
		p.add(totalBox);

		this.add(p);
	}

	/**
	 * Waits for the user to select a trade or not. Returns true if the user
	 * presses submit or false if they press cancel.
	 * 
	 * @param playerToTradeWith
	 *            - the username of the player to trade with
	 * @param parentFrame
	 *            - the frame that this smaller trade frame is bound to
	 * @return true if the user hits submit or false if they hit cancel.
	 */
	public boolean waitForSelection(String playerToTradeWith, JFrame parentFrame) {
		this.setVisible(true);
		this.setTitle("Proposing Trade with " + playerToTradeWith);
		this.setBounds(parentFrame.getBounds().x + parentFrame.getWidth() / 2 - this.getWidth() / 2,
				parentFrame.getBounds().y + parentFrame.getHeight() / 2 - this.getHeight() / 2, this.getWidth(),
				this.getHeight());

		while (submittedStatus == 0)
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		;

		this.setVisible(false);

		if (submittedStatus == -1) {
			submittedStatus = 0;
			return false;
		} else {
			submittedStatus = 0;
			return true;
		}
	}

	/**
	 * @return an array of number of cards to give. It does the five types of
	 *         cards to give first then the five to recieve.
	 */
	public int[] getCreatedTrade() {

		int[] tradeArray = new int[10];

		int i = 0;
		for (JSpinner spinner : myCards)
			tradeArray[i++] = (int) spinner.getValue();
		for (JSpinner spinner : theirCards)
			tradeArray[i++] = (int) spinner.getValue();
		
		return tradeArray;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getSource().toString() + "Has been clicked.");
		if (e.getSource() == submitButton)
			submittedStatus = 1;
		else if (e.getSource() == cancelButton)
			submittedStatus = -1;
	}

}