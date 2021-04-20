package simpleClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Socket sck;
		DataInputStream in;
		DataOutputStream out;

		String Host = "192.168.1.36";
		final int PORT = 9090;
		Scanner sc = new Scanner(System.in);
		Lector lector;
		try {
			String msg;
			System.out.println("Ingrese su nombre: ");
			msg = sc.nextLine();
			sck = new Socket(Host, PORT);
			in = new DataInputStream(sck.getInputStream());
			out = new DataOutputStream(sck.getOutputStream());
			lector = new Lector(sck, in);
			Thread t = new Thread(lector);
			t.start();
			out.writeUTF(msg);

			while (true) {
				msg = sc.nextLine();
				// System.out.println(msg);
				out.writeUTF(msg);
				if (msg.equals("/quit")) {
					lector.stop();
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (e.getMessage().equals("An established connection was aborted by the software in your host machine") || e.getMessage().equals("Socket closed")) {
				System.out.println("Conexión finalizada con el servidor");
			} else {
				System.out.println(e.toString());
				e.printStackTrace();
			}
		}
		sc.close();

	}

	private static class Lector implements Runnable {
		Socket sck;
		DataInputStream in;
		boolean flag;

		public Lector(Socket sck, DataInputStream in) {
			this.sck = sck;
			this.in = in;
			this.flag = true;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String msgReceive;
			try {
				while (flag) {
					msgReceive = in.readUTF();
					System.out.println(msgReceive);
					if (msgReceive.equals("> Remove from the server") || msgReceive.equals("> Server closed")) {
						this.stop();
						System.out.println("Press enter");
						try {
							this.sck.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}
				this.sck.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void stop() {
			flag = false;
		}

	}

}
