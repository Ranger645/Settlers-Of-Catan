package soc.code.renderPackage;

/**
 * This class is a simple console window that can be altered in several ways.
 * @author Ranger645
 */
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import soc.code.runtimePackage.GameRuntime;

/**
 * This is the class I use as an independant console for each client. Eclipse
 * can only have one client at a time so I had to make a new one to support
 * multiple interfaces and clients connected to one server at one time.
 * 
 * @author Greg
 */
public class ConsoleWindow extends JFrame {

	// box that the user types commands or chat into
	private JTextField inputTerminal;
	// box that displays server side info relevant to server commander
	JTextArea outputConsole;
	// last command that has been inputed to the console:
	private String lastCommand = "";
	private boolean newCommand = false;

	public ConsoleWindow() {
		// This is the default size of the console window
		this.setSize(400, 300);
		// other settings to set for the JFrame:
		this.setTitle("Console Window");
		// if the console window is closed, then the program will also closed,
		// you may want to fix this at some point
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setAlwaysOnTop(true);

		// initializing the textfield and textarea that will be used for Input
		// and output of the console:
		inputTerminal = new JTextField("connectTo localhost", 25);
		inputTerminal.setBackground(Color.BLACK);
		inputTerminal.setForeground(Color.GREEN);
		outputConsole = new JTextArea(15, 25);
		outputConsole.setAutoscrolls(true);
		outputConsole.setBackground(Color.BLACK);
		outputConsole.setForeground(Color.GREEN);
		outputConsole.setEditable(false);

		// temporary actionListener for detecting commands being entered:
		inputTerminal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String response = inputTerminal.getText();
				lastCommand = response;
				inputTerminal.selectAll();
				newCommand = true;
			}
		});

		// adding the IO components to the frame itself:
		this.getContentPane().add(new JScrollPane(outputConsole), "North");
		this.getContentPane().add(inputTerminal, "Center");

		// outputting initialization message:
		outputConsole.append("Console Initialized.\n");

		this.setVisible(true);
	}

	// the custom output stream that allows input from System.out to be
	// redirected to the console window:
	public class CustomOutputStream extends OutputStream {
		private JTextArea textArea;

		public CustomOutputStream(JTextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void write(int b) throws IOException {
			// redirects data to the text area
			textArea.append(String.valueOf((char) b));
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}

	public JTextArea getOutputConsole() {
		return outputConsole;
	}

	public JTextField getInputField() {
		return inputTerminal;
	}

	/**
	 * This method sets the System.out printstream to print to this console
	 * instead of the default.
	 */
	public void setSystemOut() {
		PrintStream printer = new PrintStream(new CustomOutputStream(outputConsole));
		System.setOut(printer);
	}

	public String getNextCommand() {
		while (!newCommand)
			GameRuntime.sleepMillis(100);

		return getLastCommand();
	}

	// gets the last command so the main class can access the last command
	// entered:
	public String getLastCommand() {
		String last = lastCommand;
		lastCommand = "";
		newCommand = false;
		return last;
	}

}