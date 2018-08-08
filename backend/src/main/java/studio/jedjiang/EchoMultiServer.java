package studio.jedjiang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoMultiServer {
	private ServerSocket serverSocket;

	public void start(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		while (true)
			new EchoClientHandler(serverSocket.accept()).start();
	}

	public void stop() throws IOException {
		serverSocket.close();
	}

	private static class EchoClientHandler extends Thread {
		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;

		public EchoClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {

			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				while (true) {
					//m Thread.currentThread().wait(5000);
					out.println("hello world");
				 }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		EchoMultiServer server = new EchoMultiServer();
		server.start(6666);
	}
}
