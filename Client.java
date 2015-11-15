import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class Client {
	public static final BufferedReader stdIn = new BufferedReader(
			new InputStreamReader(System.in));
	public static BufferedReader inFromServer;
	public static PrintWriter outToServer;
	public static File logClient;
	public static FileWriter writer;
	public static Socket Server;
	public static int doContinue = 1;

	public static void main(String[] args) {
		try {
			Server = new Socket("127.0.0.1", 18888);
			inFromServer = new BufferedReader(new InputStreamReader(
					Server.getInputStream()));
			outToServer = new PrintWriter(Server.getOutputStream(), true);

			// client side log file
			logClient = new File("Log-Client.txt");
			writer = new FileWriter(logClient, true);
			System.out.println("-->request webpage?");
			String fileName = stdIn.readLine();

			String userInput = "HEAD " + fileName + " HTTP/1.1 \r\n";
			passMsg(userInput);
			// time<IMS
			if (doContinue == 1) {
				for (int i = 0; i < 2; i++) {
					System.out.println("-->input a date");
					String IMStime = stdIn.readLine();
					userInput = "LMS " + fileName + " " + IMStime + " \r\n";
					passMsg(userInput);
				}
			}
			outToServer.println("exit");
			// time>IMS
			writer.close();
			stdIn.close();
			inFromServer.close();
			outToServer.close();
			Server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void passMsg(String userInput) {
		try {
			outToServer.println(userInput);
			writer.write(new Date().toLocaleString() + " "
					+ Server.getInetAddress() + ":" + Server.getPort() + " "
					+ userInput + "\r\n");
			writer.flush();
			while (true) { // display feedback
				String tmp = inFromServer.readLine();
				if (tmp.contains("end")) {
					break;
				}
				if (tmp.contains("404 Not Found")) {
					System.out.println(tmp);
					Server.close();
					doContinue = 0;
					break;
				}
				System.out.println(tmp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
