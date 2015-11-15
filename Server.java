import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class Server {
	public ServerSocket server;
	public Socket client;
	public final int PORT = 18888;

	public static void main(String[] args) {
		Server thisServer = new Server();
		try {
			thisServer.server = new ServerSocket(thisServer.PORT);
			System.out.println("web server starts successfully");
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) { // keep server alive
			try {
				// accept client socket and start a connection
				thisServer.client = thisServer.server.accept();
				thisServer.new connection(thisServer.client).start();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public class connection extends Thread {
		Socket client;
		BufferedReader inFromClient;
		DataOutputStream outToClient;
		String requestMessageLine, fileName, fileType, getLine;
		FileInputStream inFile;
		File file;
		FileWriter writer;
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		int docontinue = 1;
		byte[] fileInBytes;

		public connection(Socket c) {
			client = c;
		}

		@SuppressWarnings("deprecation")
		public void run() {
			int numOfBytes = 0;
			byte[] fileInBytes = null;
			loop: while (!client.isClosed()) {
				try {
					writer = new FileWriter(new File("Log-Server.txt"), true);

					// if (inFromClient != null)
					// inFromClient.close();
					inFromClient = new BufferedReader(new InputStreamReader(
							client.getInputStream()));
					docontinue = 1;
					outToClient = new DataOutputStream(client.getOutputStream());
					getLine = inFromClient.readLine();
					outToClient.flush();
					if (getLine != null)
						requestMessageLine = getLine;
					else {
						break loop;
					}
					if (requestMessageLine.equalsIgnoreCase("exit")) {
						client.close();
						break loop;
					}
					// Server side display request
					System.out.println(requestMessageLine);
					StringTokenizer tokenizedLine = new StringTokenizer(
							requestMessageLine, " ");
					String request = tokenizedLine.nextToken(); // request type
					if ((request.equals("GET") || request.equals("HEAD") || request
							.equals("LMS")) && tokenizedLine.countTokens() > 0) {

						fileName = tokenizedLine.nextToken(); // request
						// file
						if (fileName.startsWith("/") == true)
							fileName = fileName.substring(1);
						file = new File(fileName);
						if (!file.exists()) {
							System.out.println("404");
							
							String f0f = "404 Not Found \r\n";
							outToClient.write(f0f.getBytes(), 0,
									(int) f0f.getBytes().length);

							client.shutdownOutput();
							docontinue = -1;
						}
						// write to log file
						if(docontinue > -1){
						writer.write(client.getInetAddress() + " "
								+ new Date().toLocaleString() + " " + fileName
								+ " " + request + "\r\n");
						writer.flush();
						// read from requested file, refer to exception for 404

						c1.setTime(new Date(file.lastModified()));
						numOfBytes = (int) file.length();
						
							inFile = new FileInputStream(fileName);
						fileInBytes = new byte[numOfBytes];
						inFile.read(fileInBytes);
						}
						// handle IMS
						if (request.equals("LMS")) {
							String reqTime1 = tokenizedLine.nextToken();
							String reqTime2 = tokenizedLine.nextToken();
							StringTokenizer tokentime1 = new StringTokenizer(
									reqTime1, " :-");
							StringTokenizer tokentime2 = new StringTokenizer(
									reqTime2, " :-");
							c2.set(Integer.parseInt(tokentime1.nextToken()),
									Integer.parseInt(tokentime1.nextToken()),
									Integer.parseInt(tokentime1.nextToken()),
									Integer.parseInt(tokentime2.nextToken()),
									Integer.parseInt(tokentime2.nextToken()),
									Integer.parseInt(tokentime2.nextToken()));
							if (c1.compareTo(c2) < 0) {
								// c1 = last modified time, c2 = request time,
								// if c1
								// is earlier,304
								outToClient.writeBytes("304 Not Modified"
										+ "\r\n");
								outToClient.flush();
								docontinue = 0;
							}
						}
						if (docontinue == 1) {
							// if file is found then 200 OK, else handled in
							// exceptions below
							outToClient.writeBytes("HTTP/1.1 200 OK \r\n");
							// get file type
							if (fileName.contains("."))
								fileType = fileName.split("\\u002E")[1];
							outToClient.writeBytes("Content-Type: " + fileType
									+ "\r\n");
							// get file length and write
							outToClient.writeBytes("Content-Length: "
									+ numOfBytes + "\r\n");

							outToClient.writeBytes("\r\n");
							// if is get, give the file
							if (request.equals("GET") || request.equals("LMS")) {
								// write last modified time
								if (request.equals("LMS")) {
									outToClient
											.writeBytes("Last Modified time: "
													+ c1.getTime()
															.toLocaleString());
								}

								outToClient.write(fileInBytes, 0, numOfBytes);
							}
						}

					} else
						// bad request if not started with GET HEAD or LMS
						outToClient.writeBytes("HTTP/1.1 400 Bad Request"
								+ "\r\n");
					System.out.println("here");
					outToClient.writeBytes("\r\n" + "end" + "\r\n\r\n");
					outToClient.flush();
					Thread.currentThread().sleep(1000);
				} // catch (FileNotFoundException e) {
					// try {
					// outToClient.writeBytes("HTTP/1.1 404 Not Found \r\n");
					// outToClient.writeBytes("Connection: Closes \r\n");
				// outToClient.writeBytes("404 NOT FOUND \r\n");
				// outToClient.flush();
				// client.shutdownOutput();
				// continue loop;
				// outToClient.close();

				// } catch (IOException e1) { // TODO Auto-generated catch
				// block
				// e1.printStackTrace();
				// }
				// }
				catch (SocketException e) {

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			try {
				outToClient.close();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
