package soc.code.multiplayerPackage;

import java.io.IOException;
import java.net.Socket;

/**
 * This class is for housing helper methods to assist in data spitting and data
 * sucking via sockets and server sockets.
 * 
 * @author developer
 */
public class ConnectionHelper {

	/**
	 * This method will write the given string to the output stream of the given
	 * socket s.
	 * 
	 * @param toPrint
	 *            - the string that is going to be written to the output stream
	 *            of s
	 * @param s
	 *            - the socket that toPrint will be sent to.
	 */
	public static void printString(String toPrint, Socket s) {
		try {
			for (int i = 0; i < toPrint.length(); i++)
				// sending each individual character in ascii format:
				s.getOutputStream().write((int) toPrint.charAt(i));
			s.getOutputStream().write('\n');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method returns the next line of data that is sent from the sender.
	 * It returns a string representation of this data and does not contain an
	 * endline character (\n) at the end of it.
	 */
	public static String readLine(Socket s) {
		String readString = "";
		try {
			// The data is sent as integer values of the ascii representation of
			// each character of data. So these integers must be converted back
			// into letters in ascii format which is done here:
			char currentValue;
			do {
				currentValue = (char) s.getInputStream().read();
				readString += currentValue;
			} while (currentValue != '\n');
			// Chopping off the endline character at the end of the data
			// message.
			readString = readString.substring(0, readString.length() - 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return readString;
	}
	
	/**
	 * Uses system time functions to calculate response time of the given socket
	 * object in milliseconds.
	 * 
	 * @param s
	 *            - the socket that server is pinging.
	 * @return the amount of time the client takes to respond in milliseconds.
	 */
	public static long getResponseTime(Socket s) {
		long ms = System.currentTimeMillis();

		// requesting ping...
		ConnectionHelper.printString("ping", s);
		// recieving ping response:
		ConnectionHelper.readLine(s);
		// calculating the actual time difference:
		ms -= System.currentTimeMillis();
		ms = Math.abs(ms);

		// sending the ping calculation to the client:
		ConnectionHelper.printString(String.valueOf(ms), s);

		return ms;
	}
	

	/**
	 * This method takes care of the situation in which the host requests a ping
	 * calculation to detect the strength of the connection. simply sends a
	 * message back to the server that says ping.
	 */
	public static void recievePingRequest(Socket s) {
		//System.out.println("Connection test request recieved, replying...");
		ConnectionHelper.printString("ping", s);
		//System.out.println("Connection test complete, ping = " + ConnectionHelper.readLine(s));
	}

}
