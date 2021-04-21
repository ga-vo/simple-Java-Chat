package simpleServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
	ServerSocket server;
	Socket sck;
	DataInputStream in;
	DataOutputStream out;
	ArrayList<Client> clientes = new ArrayList<Client>();
	ArrayList<String> newMessages = new ArrayList<String>();
	private boolean flag;
	private int PORT;

	public Server(int port) {
		this.PORT = port;
		this.flag = true;
		try {
			this.server = new ServerSocket(this.PORT);
			System.out.println("Server initialized  on port: " + PORT);
		} catch (IOException e) {
			System.out.println("Cannot initialize the server, error: " + e.getMessage());
		}
	}

	public void runServer() {
		try {
			Lector lector = new Lector(this);
			Thread tL = new Thread(lector);
			tL.start();
			while (this.flag) {
				sck = server.accept();

				in = new DataInputStream(sck.getInputStream());
				out = new DataOutputStream(sck.getOutputStream());

				String msg = in.readUTF();
				if (msg.equals("/close")) {
					System.out.println("Server finalized");
					break;
				}
				System.out.println("> " + msg + " ha ingresado");
				this.newMessage("> " + msg + " ha ingresado");
				this.broadcast();
				out.writeUTF("Hola, bienvenido " + msg);
				Client cliente = new Client(msg, sck, this);
				clientes.add(cliente);
				Thread t = new Thread(cliente);
				t.start();
			}
		} catch (IOException e) {
			if (e.getMessage() != null) {
				if (e.getMessage().equals("Socket closed")) {
					System.out.println("Server Finalizado");
				} else {
					e.printStackTrace();
				}
			}else {
				e.printStackTrace();
			}
		}
	}

	public void broadcast() {

		for (String msg : newMessages) {

			for (Client c : clientes) {
				c.receiveMsg(msg);
			}
		}
		newMessages.clear();
	}

	public void newMessage(String msg) {
		this.newMessages.add(msg);
	}

	public void closeConnection(Client c) {
		c.close();
		clientes.remove(c);
	}

	public boolean getFlag() {
		return this.flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public void closeConnections() {
		for (Client c : clientes) {
			c.close();
		}
		clientes.clear();
	}

	public void removeClient(String name) {
		for (Client c : this.clientes) {
			if (c.getNick().equals(name)) {
				c.receiveMsg("> Remove from the server");
				c.setRemove(true);
				this.closeConnection(c);
				return;
			}
		}
		System.out.println(name + " no encontrado");
	}

	public void removeFromClientes(Client c) {
		this.clientes.remove(c);
	}

	public void privateMessage(Client remit, String name, String msg) {
		for (Client c : this.clientes) {
			if (c.getNick().equals(name)) {
				c.receiveMsg(remit.getNick() + " [Private Message]: " + msg);
				remit.receiveMsg(remit.getNick() + " [Private message to " + name + "]: " + msg);
				System.out.println(remit.getNick() + " [Private message to " + name + "]: " + msg);
				return;
			}
		}
		remit.receiveMsg(name + " no encontrado");
		System.out.println(name + " no encontrado");
	}

	protected void privateMessage(String name, String msg) {
		for (Client c : this.clientes) {
			if (c.getNick().equals(name)) {
				c.receiveMsg("> Server [Private Message]: " + msg);
				return;
			}
		}
		System.out.println(name + " no encontrado");
	}

	private class Lector implements Runnable {
		Server server;
		Scanner sc = new Scanner(System.in);

		public Lector(Server server) {
			this.server = server;
		}

		@Override
		public void run() {
			String c;
			Pattern patternRemove, patternSendAll, patternSend;
			Matcher matcherRemove, matcherSendAll, matcherSend;
			String name;
			while (this.server.getFlag()) {
				c = sc.nextLine();

				if (c.startsWith("/")) {
					patternRemove = Pattern.compile("/remove[\s]+[[a-z]+|[0-9]+]", Pattern.CASE_INSENSITIVE);
					patternSendAll = Pattern.compile(
							"^/sendall[\s]+[[a-z]+|[0-9]+|[\s]*|[-!$%^&*()_+|~=`{}\\[\\]:\";'<>?,.\\/]*|[A-zÀ-ú]*]+$",
							Pattern.CASE_INSENSITIVE);
					patternSend = Pattern.compile(
							"^/send[\s]+[[a-z]+|[0-9]+]+[[a-z]+|[0-9]|[\s]*|[-!$%^&*()_+|~=`{}\\[\\]:\";'<>?,.\\/]*|[A-zÀ-ú]*]+$",
							Pattern.CASE_INSENSITIVE);
					matcherRemove = patternRemove.matcher(c);
					matcherSendAll = patternSendAll.matcher(c);
					matcherSend = patternSend.matcher(c);
					int a;
					if (matcherRemove.find()) {
						a = c.toLowerCase().indexOf("/remove") + "/remove".length();
						while (c.charAt(a) == ' ') {
							a++;
						}
						name = c.substring(a);
						System.out.println("Removing " + name + "...");
						this.server.removeClient(name);

					} else {
						if (c.equals("/close")) {
							this.server.setFlag(false);
							this.server.newMessage("> Server closed");
							this.server.broadcast();
							closeConnections();
							try {
								this.server.server.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
							break;
						} else {
							if (matcherSendAll.find()) {
								a = c.toLowerCase().indexOf("/sendall") + "/sendall".length();
								while (c.charAt(a) == ' ') {
									a++;
								}
								String message = c.substring(a);
								System.out.println("> Server: " + message);
								this.server.newMessage("> Server :" + message);
								this.server.broadcast();
							} else {
								if (matcherSend.find()) {
									a = c.toLowerCase().indexOf("/send") + "/send".length();
									while (c.charAt(a) == ' ') {
										a++;
									}
									int b = a;
									while (c.charAt(b) != ' ') {
										b++;
									}
									String user = c.substring(a, b);
									String message = c.substring(b);
									System.out.println("> Server [Private message to " + user + "]: " + message);
									this.server.privateMessage(user, message);
								}
							}
						}
					}
				}
			}
		}

	}
}
