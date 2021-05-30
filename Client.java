//package Client;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import java.io.*;

public class Client {
	static final int MAX_INPUT = 20000;
	static final int DEFAULT_PORT = 50000;

	public static void main(String args[]) {
		try {
			Socket sock = new Socket("localhost", DEFAULT_PORT);
			PrintStream output = new PrintStream(sock.getOutputStream());
			InputStream severIn = sock.getInputStream();
			String serverInput = "";
			String clientOut;
			Boolean gotData = false;
			boolean gotServers = false;
			Boolean sentJob = false;
			boolean ok = false;
			int max = 0;
			String largestServerName = "";
			List<List<String>> servers = new ArrayList<List<String>>();
			List<List<String>> jobs = new ArrayList<List<String>>();
			List<List<String>> serverData = new ArrayList<List<String>>();
			List<Integer> serverCPUCores = new ArrayList<Integer>();

			byte[] data;
			int count = 0;

			// Handshake Begins

			clientOut = "HELO" +"\n";
			output.print(clientOut);

			// https://stackoverflow.com/questions/19839172/how-to-read-all-of-inputstream-in-server-socket-java

			data = new byte[MAX_INPUT];
			count = severIn.read(data);

			// https://stackoverflow.com/questions/10475898/receive-byte-using-bytearrayinputstream-from-a-socket

			serverInput = serverMsg(count, data);

			System.out.println(serverInput);
			if (!serverInput.equals("OK" +"\n")) {
				output.print("QUIT"+"\n");
				sock.close();
			}

			clientOut = "AUTH "+System.getProperty("user.name")+"\n";
			output.print(clientOut);

			count = 0;
			data = new byte[MAX_INPUT];
			count = severIn.read(data);
			serverInput = serverMsg(count, data);

		
			if (!serverInput.equals("OK"+"\n")) {
				output.print("QUIT"+"\n");
				sock.close();
			}

			clientOut = "REDY"+"\n";
			output.print(clientOut);

			// Handshake completed. Loop begins now

			while (true) {

				count = 0;
				data = new byte[MAX_INPUT];
				count = severIn.read(data);
				serverInput = serverMsg(count, data);
				System.out.print(serverInput);
			

				// checks if there is no more jobs left to schedule and closes the socket if
				// true
				if (serverInput.equals("NONE"+"\n")) {
					output.print("QUIT"+"\n");
					sock.close();
					break;
				} else {

					// checks if the server has sent a job to be scheduled
					if (serverCmd(serverInput).equals("JOBN")) {
						

						jobs = createList(serverInput);

						String cpuCores = jobs.get(0).get(4);
						String memory = jobs.get(0).get(5);
						String disk = jobs.get(0).get(6);

						output.print("GETS Capable " + cpuCores + " " + memory + " " + disk+"\n");

						/*
						 * Checks if the server has sent a data command in response to a gets command
						 * sent by the client
						 */
					} else if (serverCmd(serverInput).equals("DATA")) {

						serverData = createList(serverInput);

						// checks if the client has recieved the data before sending the OK command
						if (serverData.size() >= 1) {

							output.print("OK"+"\n");
							gotData = true;
						}

						// checks if the client has recievied the data about the current capable servers
						// for the job
					} else if (gotData) {

						servers = createList(serverInput);

						// checks to see if the client has recieved data from all the capable servers
						// before issuing to OK command
						if (servers.size() == Integer.valueOf(serverData.get(0).get(1))) {

							if (ok == false) {
								output.print("OK"+"\n");
								ok = true;
							}
						}
						/*
						 * Checks if the largest server has already been calculated if not the code is
						 * executed to find the largest server. (only runs one)
						 */
						if (gotServers == false) {

							// iterates through the servers list and adds the cpu cores for each server
							// to a new list
							for (int i = 0; i < Integer.valueOf(serverData.get(0).get(1)); i++) {

								if (servers.size() > 1) {
									serverCPUCores.add(Integer.valueOf(servers.get(i).get(4)));
								}

								gotServers = true;
							}

							max = allToLargest(serverCPUCores);

							// iterates through the server list and returns the name of the server
							// that has the same number of CPU cores as the value stored in max
							for (int i = 0; i < servers.size(); i++) {

								if (Integer.valueOf(servers.get(i).get(4)) == max) {
									largestServerName = servers.get(i).get(0);
									break;
								}
							}
						}

						/*
						 * Checks if the server has sent the . command and if so replies with a SCHD
						 * command and schedules a job to the server
						 */
						if (serverCmd(serverInput).equals(".") && sentJob == false) {

							String JobId = jobs.get(0).get(2);

							output.print("SCHD " + JobId + " " + largestServerName + " " + "0"+"\n");
							sentJob = true;
						}
						/*
						 * checks if the server has sent a OK command and if true the client sends a
						 * REDY command telling the server it is ready for the next job
						 */
						if (serverInput.equals("OK"+"\n")) {

							output.print("REDY"+"\n");
							gotData = false;
							sentJob = false;
							ok = false;

						}
						/*
						 * checks if the server has sent a job complete command. if so the server
						 * responds with the REDY command
						 */
					} else if (serverCmd(serverInput).contains("JCPL")) {

						output.print("REDY"+"\n");
					}

				}
			
			}

		} catch (Exception e) {
			System.out.println(e);
		}

	}

	/*
	 * function takes in a list of the number of server CPU cores for each server.
	 * and returns the largest number of server cores in the list
	 */
	public static int allToLargest(List<Integer> serverCores) {

		int retval = 0;
		Integer max = 0;

		max = serverCores.get(0);

		for (int i = 0; i < serverCores.size(); i++) {

			if (serverCores.get(i) > max) {

				max = serverCores.get(i);
			}
		}
		retval = max;

		return retval;

	}

	public static String serverMsg(int count, byte[] data) {

		/*
		 * serverMsg() takes in the byte count and an instantiated byte data array that
		 * contains the byte stream of the server output. This function converts the
		 * byte stream into a legible string.
		 */

		byte[] serverArr = new byte[count];

		for (int i = 0; i < count; i++) {
			serverArr[i] = data[i];

		}

		String serverOutput = new String(serverArr, StandardCharsets.UTF_8);

		return serverOutput;

	}

	public static String serverCmd(String serverOutput) {

		/*
		 * serverCmd() takes the first character (in the case of '.') or word in a given
		 * server output and returns it. Useful for deciding what to do with a given
		 * server command.
		 * 
		 */

		String field = "";
		for (int i = 0; i < serverOutput.length(); i++) {
			Character c = serverOutput.charAt(i);

			if (c.equals('.')) {
				field = field + c;
				return field;
			}

			if (Character.isWhitespace(c) || (i == serverOutput.length() - 1)) {
				break;
			} else {
				field = field + c;
			}

		}
		return field;

	}

	public static List<List<String>> createList(String serverOutput) {

		/*
		 * Takes the server output (if it is a list of servers) and returns it as a List
		 * of string lists.
		 */

		List<List<String>> retval = new ArrayList<List<String>>();
		String field = "";
		List<String> aList = new ArrayList<String>();
		int count = 0;

		retval.add(aList);

		for (int i = 0; i < serverOutput.length(); i++) {
			Character c = serverOutput.charAt(i);

			if ((i == serverOutput.length() - 1)) {
				field = field + c;
				retval.get(count).add(field);
				break;
			}

			if (!String.valueOf(c).matches(".")) {
				// If the character is a new line, create a new list of strings.
				// https://stackoverflow.com/questions/25915073/detect-line-breaks-in-a-char
				retval.get(count).add(field);
				count++;
				// since /n is two characters

				field = "";
				List<String> bList = new ArrayList<String>();
				retval.add(bList);

			} else if (Character.isWhitespace(c) || (i == serverOutput.length() - 1)) {
				// stringCount++;
				retval.get(count).add(field);
				field = "";
			} else {
				field = field + c;
			}

		}

		return retval;
	}

}
