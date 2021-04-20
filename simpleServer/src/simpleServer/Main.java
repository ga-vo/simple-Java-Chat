package simpleServer;

public class Main {

	public static void main(String[] args) {
		final int PORT = 9090;

		Server server = new Server(PORT);
		server.runServer();

	}

}
