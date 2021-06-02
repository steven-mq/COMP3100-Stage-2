import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;




public class Client {

	static final int DEFAULT_PORT = 50000;

	// Connect to Local Host and Port
	public static void main(String args[]) throws IOException {
		Client client = new Client("localhost", DEFAULT_PORT);
		client.handshake();
	}


	
	private void handshake () {
	
		// populate arrayList of server
		ArrayList<Server> servers = new ArrayList<Server>();
		// arrayList for holding job
		ArrayList<Job> jobs = new ArrayList<Job>();
		boolean connected = true;
		
		// Start handshake
		sendMessage("HELO");
		readMessage();

		// Send user details
		sendMessage("AUTH " + System.getProperty("user.name"));
		readMessage();


		

		// Tells client it is ready to recieve commands
		sendMessage("REDY");

		//string to hold readMessage data
		String NewMsg = readMessage();
		
		// handshake Ends. Loop Begins..
		// Assign job to the largest available server
		
		while (connected){
			
			if (NewMsg.contains("JCPL")){ // Job completed
				sendMessage("REDY");	//  Server Ready
				NewMsg = readMessage(); 
			} else if (NewMsg.contains("NONE")){ // checks if there is no more jobs left to schedule
				connected = false;	// exit the program
				sendMessage("QUIT");
			}else {

				if (NewMsg.contains("OK")){ 
					sendMessage("REDY");
					NewMsg = readMessage(); 
				}

				 
				if (NewMsg.contains("JOBN")){ // initiate new job
					jobs.add(newJob(NewMsg)); // create job 
					sendMessage(bestFitJob(jobs.get(0)));
					NewMsg = readMessage();
					sendMessage("OK");

					// list of capable servers are added to arrayList of server objects
					NewMsg = readMessage();
					servers = createServer(NewMsg);
					sendMessage("OK");
					NewMsg = readMessage();

					// Initiate Scheduling algorithm
					sendMessage(BestFit(servers, jobs)); 
					NewMsg = readMessage();
					jobs.remove(0);
				} 
			} 
		}


		// End the handshake and close the connection
		try { 

			if (readMessage().contains("QUIT")){
				input.close();
				out.close();
				socket.close();
			}
				
		} catch (IOException i) {
		}

		// Exit the program
		System.exit(1);
	}
	
	// initialize socket and input output streams
	private static Socket socket = null;
	private BufferedReader input = null;
	private DataOutputStream out = null;
	private BufferedReader in = null;
	
	public Client(String address, int port) throws IOException {
		// connect to server
		connect(address, port);
		// takes input from keyboard
		input = new BufferedReader(new InputStreamReader(System.in));
		// sends output to the server
		out = new DataOutputStream(socket.getOutputStream());
		// gets input from server
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	// crates arrayList based on an XML File
	// used for stage 1
	public static ArrayList<Server> readXML(String fileName){
        ArrayList<Server> serverList = new ArrayList<Server>();
		
		try {
			// XML file to read
			File systemXML = new File(fileName);

			// Setup XML document parser
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(systemXML);

			// String converting to normalized form
			doc.getDocumentElement().normalize();
			NodeList servers = doc.getElementsByTagName("server");
			for (int i = 0; i < servers.getLength(); i++) {
				Element server = (Element) servers.item(i);

				// Parse all XML attributes to new Server object
				String type = server.getAttribute("type");
				int limit = Integer.parseInt(server.getAttribute("limit"));
				int bootupTime = Integer.parseInt(server.getAttribute("bootupTime"));
				float hourlyRate = Float.parseFloat(server.getAttribute("hourlyRate"));
				int coreCount = Integer.parseInt(server.getAttribute("coreCount"));
				int memory = Integer.parseInt(server.getAttribute("memory"));
				int disk = Integer.parseInt(server.getAttribute("disk"));
				
				Server s = new Server(type,limit,bootupTime,hourlyRate,coreCount,memory,disk);
				serverList.add(s);
			}

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return serverList;
    }

	
	// The optimized best fit algorithm to improve the overall performance of
	// the average turnaround time for the tasks assigned
	
	public String BestFit(ArrayList<Server> servers, ArrayList<Job> job){
	
		String svr = "";
		for (Server s: servers){ // iterate through the servers to find the best fit for the job assigned		 
			if (s.gtDiskSp() >= job.get(0).gtDisk() && s.gtCoresNum() >= job.get(0).gtCore() && s.gtMemAm() >= job.get(0).gtMemeory()){
			 	svr = s.gtjobType() + " " + s.gtserverID();
				return "SCHD " + job.get(0).gtJobID() + " " + svr;
			} else { 
				svr = servers.get(0).gtjobType() + " " + servers.get(0).gtserverID();
			}
		}
		return "SCHD " + job.get(0).gtJobID() + " " + svr;
	}

	// reads job info for the best suitable job for the server assigned 
	public String bestFitJob(Job j){
		
		return("GETS Capable " + j.gtCore() + " " + j.gtMemeory() + " " + j.gtDisk());
	}

	
	/*
		string input to create a new job object
	*/
	public Job newJob(String job){
		//removing unwanted data in the string
		job = job.trim();

		// split string up by white space
		String[] splitS = job.split("\\s+");

		Job newJB = new Job(Integer.parseInt(splitS[1]), Integer.parseInt(splitS[2]), Integer.parseInt(splitS[3]),  			   	Integer.parseInt(splitS[4]) ,Integer.parseInt(splitS[5]), Integer.parseInt(splitS[6]));

		// returns job object to fill arrayList
		return newJB;
	}

	private String readMessage () {
		// read information from string sent from server
		String strInfo = "";
		//calculates the size of to store the message sent from server
		char[] newVal = new char[((int)Short.MAX_VALUE)*2]; 
		try {
			in.read(newVal);
		} catch (IOException e) {
			e.printStackTrace();
		}
		strInfo = new String(newVal, 0, newVal.length);
		return strInfo;
	}

	//initiate connection of address to port
	private static void connect(String adr, int port) {
		double waitTime = 1;
		int setNum = 1;
		while (true) {
			try {
				// check if it is connected then close the loop
				System.out.println("Connecting to server at: " + adr + ":" + port);
				socket = new Socket(adr, port);
				break; 
			} catch (IOException e) {
				// reconnect failed, wait.
				waitTime = Math.min(30, Math.pow(2, setNum));
				setNum++;
				//System.out.println("Connection timed out, retrying in  " + (int) secondsToWait + " seconds ...");
				try {
					TimeUnit.SECONDS.sleep((long) waitTime);
				} catch (InterruptedException ie) {
					// interrupted
				}
			}
		}
	}

	// send message info the to best fit server
	private void sendMessage (String outStr) {
		byte[] msg = outStr.getBytes();
		try {
			out.write(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// reads server info for the best suitable sever for the job assigned 
	public ArrayList<Server> createServer(String server){

		// temp arrayList to store server
		ArrayList<Server> newSvr = new ArrayList<Server>();
		server = server.trim();// remove unwanted data in the string
		String[] newLine = server.split("\\r?\\n");
 		
		for (String line : newLine) {

			// split string up by white space
			String[] splitS = line.split("\\s+");

			Server s = new Server(splitS[0], Integer.parseInt(splitS[1]), splitS[2], Integer.parseInt(splitS[3]), Integer.parseInt(splitS[4]), Integer.parseInt(splitS[5]), Integer.parseInt(splitS[6]), Integer.parseInt(splitS[7]), Integer.parseInt(splitS[8]) );
			newSvr.add(s);
        }

		return newSvr;
	}
	
	
	
}
