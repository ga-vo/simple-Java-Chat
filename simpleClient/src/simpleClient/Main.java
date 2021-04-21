package simpleClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Simple Client-Side console Chat!
 * The SimpleClient program implements
 * an console chat application using sockets and implements threads.
 * <p>
 *
 * @author ga-vo
 * @version 1.0
 * @since 2021-03-10
 */

public class Main {

	/**
	 * This is the main method that initiates the connection 
	 * with the server and handles the messages entered by the user.
	 * 
	 * @param args Unused.
	 */
	public static void main(String[] args) {
		Socket sck;
		DataInputStream in;
		DataOutputStream out;
		
		//Server ip
		String Host = "localhost";
		//Server port
		final int PORT = 9090;
		
		Scanner sc = new Scanner(System.in);
		//Define a new Reader
		Reader reader;
		try {
			String msg;
			System.out.println("Enter your name: ");
			msg = sc.nextLine();
			
			//Create a new Socket with server's ip and port
			sck = new Socket(Host, PORT);
			
			//Create Input and Output Streams
			in = new DataInputStream(sck.getInputStream());
			out = new DataOutputStream(sck.getOutputStream());
			
			//Initialize new Reader
			reader = new Reader(sck, in);
			
			//Create a new Thread
			Thread t = new Thread(reader);
			
			//Start the thread
			t.start();
			
			//Send Nick to server
			out.writeUTF(msg);

			/**Continuously reads user messages and sends them
			 * to the server 
			 */
			while (true) {
				msg = sc.nextLine();
				out.writeUTF(msg);
				/** If the user enters '/quit' call stop() method from Reader
				 *  and break the cycle.
				 */
				if (msg.equals("/quit")) {
					reader.stop();
					break;
				}
			}

		} catch (IOException e) {
			/** Handles the exception of losign the connection
			 * with server
			 */
			if (e.getMessage().equals("An established connection was aborted by the software in your host machine")
					|| e.getMessage().equals("Socket closed")) {
				System.out.println("Connection with the server terminated");
			} else {
				System.out.println(e.toString());
				e.printStackTrace();
			}
		}
		sc.close();

	}
	
	
	
	/** Reader class, it's used to continuously read messages 
	 * received from the server.
	 *
	 * @author ga-vo
	 * @version 1.0
	 * @since 2021-03-10
	 * @see Runnable
	 */
	private static class Reader implements Runnable {
		Socket sck;
		DataInputStream in;
		boolean flag;
		
		/** Creates an reader with the specified socket and Datainputstream.
		 * @param sck Connection socket from server
		 * @param in Stream for data input.
		*/
		public Reader(Socket sck, DataInputStream in) {
			this.sck = sck;
			this.in = in;
			this.flag = true;
		}

		
		/** Continuously reads messages from the server
		*/
		@Override
		public void run() {
			String msgReceive;
			try {
				while (flag) {
					msgReceive = in.readUTF();
					System.out.println(msgReceive);
					
					/** If the message received is 'Remove from the server'
					 * stop the thread, warn the user and try to close the socket connection
					*/
					if (msgReceive.equals("> Remove from the server") || msgReceive.equals("> Server closed")) {
						this.stop();
						System.out.println("Press enter");
						try {
							this.sck.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

				}
				
				//Close socket connection
				this.sck.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**Sets the flag to false
		 * It's used to stop the execution
		 */
		public void stop() {
			flag = false;
		}

	}

}
