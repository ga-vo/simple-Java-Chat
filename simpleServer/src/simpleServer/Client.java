package simpleServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client implements Runnable {
	Server server;
	private String nick;
	Socket sck;
	DataInputStream in;
	DataOutputStream out;
	private boolean flag;
	private boolean remove;

	public Client(String nick, Socket sck, Server server) {
		this.setNick(nick);
		this.sck = sck;
		this.server = server;
		this.flag = true;
		this.remove = false;
		try {
			this.in = new DataInputStream(sck.getInputStream());
			this.out = new DataOutputStream(sck.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		String msg;
		Pattern patternPrivate;
		Matcher matcherPrivate;
		int a;
		while (this.flag) {
			try {
				msg = in.readUTF();

				patternPrivate = Pattern.compile(
						"^/private[\s]+[[a-z]+|[0-9]+]+[[a-z]+|[0-9]|[\s]*|[-!$%^&*()_+|~=`{}\\[\\]:\";'<>?,.\\/]*|[A-zÀ-ú]*]+$",
						Pattern.CASE_INSENSITIVE);
				matcherPrivate = patternPrivate.matcher(msg);
				if (matcherPrivate.find()) {
					a = msg.toLowerCase().indexOf("/private") + "/private".length();
					while (msg.charAt(a) == ' ') {
						a++;
					}
					int b = a;
					while (msg.charAt(b) != ' ') {
						b++;
					}
					String user = msg.substring(a, b);
					String message = msg.substring(b);
					this.server.privateMessage(this, user, message);
				} else {
					System.out.println(this.getNick() + ": " + msg);
					if (msg.equals("/quit")) {
						System.out.println("> " + this.getNick() + " se ha desconectado");
						sendMsg("> " + this.getNick() + " se ha desconectado");
						this.server.closeConnection(this);
						break;
					}
					sendMsg(this.getNick() + ": " + msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (e.getMessage() != null) {
					if (e.getMessage().equals("Socket closed")) {
						// String msgE = e.getMessage();
						if (this.remove) {
							System.out.println("Conexión con " + this.getNick() + " finalizada");
						}
					} else {
						System.out.println("Error en closed()");
						if (e.getMessage().equals("Connection reset")) {
							System.out.println("Conexión con " + this.getNick() + " se ha perdido");
							this.server.removeFromClientes(this);
						} else {
							System.out.println("Error en reset()");
							e.printStackTrace();
						}
					}
				} else {
					e.printStackTrace();
				}
				break;
			}
		}

	}

	public void sendMsg(String msg) {
		this.server.newMessage(msg);
		this.server.broadcast();
	}

	public void receiveMsg(String msg) {
		try {
			this.out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		this.flag = false;
		try {
			this.sck.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public boolean isRemove() {
		return remove;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}
}