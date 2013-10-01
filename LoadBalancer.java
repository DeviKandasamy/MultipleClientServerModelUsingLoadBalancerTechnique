package FinalProject;

import java.net.*; 
import java.util.*;
import java.io.*;
import java.util.concurrent.*;

class ConnectionTable {
	static Semaphore serverListLock = new Semaphore(1);
	static Semaphore getNextServer = new Semaphore(1);
	
	
	private static int rollingCounter = 0;
	
	synchronized static  int getServerPort()
	{
		//Round - Robin
		if (Servers.size() == 0 )
			return -1;
		
		serverInfo s=  Servers.get(rollingCounter);
		rollingCounter = (rollingCounter +1)% Servers.size();
		return s.connectionPort;
	}

	//static Lock serverListLock = new ReentrantLock();
	static Vector<serverInfo> Servers = new Vector<serverInfo>();

	synchronized static void addServer(serverInfo newServer) {
		System.out.println(LoadBalancer.getCurrentTime() + "::" + "Adding a server entry");
		Servers.add(newServer);
	}
	synchronized static void removeServer(serverInfo serverDown) {
		System.out.println(LoadBalancer.getCurrentTime() + "::" + "Removing a server entry");
		Servers.remove(serverDown);
	}
};
public class LoadBalancer
{
	
	ServerSocket lbSocket;
	Socket lbAcceptSocket;
	DataInputStream lbSocketStream;
	//ArrayList serverPorts = new ArrayList();
	LoadBalancer(int clientSidePort, int serverSidePort)throws IOException{
		lbSocket=new ServerSocket(clientSidePort);	// create server socket

		//Start ServerIntro thread to listen for Incoming server Connections
		ServerIntroThread serverIntrothreadObj = new ServerIntroThread(serverSidePort);
		Thread serverIntroThread = new Thread(serverIntrothreadObj);
		serverIntroThread.start();

		//Start StatusChecker thread to periodically check status of servers which are in the ConnectionTable
		StatusCheckerThread statusCheckerthreadObj = new StatusCheckerThread();
		Thread statusCheckerThread = new Thread(statusCheckerthreadObj);
		statusCheckerThread.start();

		System.out.println(getCurrentTime() + "::" + "Starting Loadbalancer - listening to client connections");
		while(true){
			String name;
			lbAcceptSocket = lbSocket.accept();
			lbSocketStream = new DataInputStream(lbAcceptSocket.getInputStream());
			//System.out.println(" Client connection incoming");
			int chosenServerPort = ConnectionTable.getServerPort();
			if (chosenServerPort == -1) {
				lbAcceptSocket.close();
				continue;
			}
			ConnectionThread cThreadObj = new ConnectionThread(lbAcceptSocket, chosenServerPort);
			// Pick a server socket to use with this client connection
			//Create a Thread, call Thread start
			Thread  connThread = new Thread(cThreadObj);
			connThread.start();
		}
	}
	static String getCurrentTime()
	{
		Calendar now = Calendar.getInstance();
		int h = now.get(Calendar.HOUR_OF_DAY);
		int m = now.get(Calendar.MINUTE);
		int s = now.get(Calendar.SECOND);
		String str = "<LoadBalancer> " +  h + ":" + m + ":" + s;
		return str;
	}
	
	public static void main(String[] args){
		try {
			// ClientSide Port, Server Side Port
			new LoadBalancer( 38000, 26000);
			//new LoadBalancer(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ConnectionThread implements Runnable{
	Socket clientSocket;
	Socket serverSocket;
	DataInputStream clientIn, serverIn;
	DataOutputStream clientOut, serverOut;

	ConnectionThread(Socket s, int serverPort) {
		clientSocket = s;
		try {
			serverSocket = new Socket("localhost", serverPort);
			System.out.println("Server port : " + serverPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run() {
		String s;
		System.out.println(LoadBalancer.getCurrentTime() + "::" +"Connected to Server, ready to proxy client server connection");
		try {
			clientIn = new DataInputStream(clientSocket.getInputStream());
			clientOut = new DataOutputStream(clientSocket.getOutputStream());
			serverIn = new DataInputStream(serverSocket.getInputStream());
			serverOut = new DataOutputStream(serverSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//while (true) {
		if (true) {
			byte[] bytestream;
			try {
				s = clientIn.readUTF();
				System.out.println(LoadBalancer.getCurrentTime() + "::" +"Client sent "+ s + ", forwarding this to the server");
				serverOut.writeUTF(s);
				
				s = serverIn.readUTF();
				System.out.println(LoadBalancer.getCurrentTime() + "::" +"Server sent "+ s + ", forwarding this to the client");
				clientOut.writeUTF(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	

	}
}
class serverInfo {
	int statusPort, connectionPort;
	int connectionCount;

	public serverInfo (int connPort, int statPort ) {
		statusPort = statPort;
		connectionPort =connPort;
	}

	int getStatusPort() {
		return statusPort;
	}

	int getConnPort() {
		return connectionPort;
	}
};

class ServerIntroThread implements Runnable{
	ServerSocket introSocket;
	Socket introAcceptSocket;
	DataInputStream serverIn;
	DataOutputStream serverOut;

	ServerIntroThread(int introListenPort ) {
		try {
			introSocket = new ServerSocket(introListenPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		System.out.println(LoadBalancer.getCurrentTime() + "::" +"Starting Server Intro Thread");
		while(true){
			String serverConnPort = null, serverStatusPort = null;
			try {
				introAcceptSocket = introSocket.accept();
				//System.out.println("Intro from server!!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				serverIn = new DataInputStream(introAcceptSocket.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				serverConnPort = serverIn.readUTF();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				serverStatusPort = serverIn.readUTF();
				System.out.println("Status port :" + serverStatusPort);
				System.out.println(LoadBalancer.getCurrentTime() + "::" +" Registering New Server @ Listening port :" + serverConnPort + ", Status Port :"+serverStatusPort );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				//System.out.println("ServerIntro: Before Acquire");
				ConnectionTable.serverListLock.acquire();
				//System.out.println("ServerIntro: After acquire");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverInfo newServer = new serverInfo(Integer.parseInt(serverConnPort), Integer.parseInt(serverStatusPort));

			ConnectionTable.addServer(newServer);
			//System.out.println("ServerIntro: Before release");
			ConnectionTable.serverListLock.release();
			//System.out.println("ServerIntro: After release");
		}
	}
}
class StatusCheckerThread implements Runnable{
	Socket statusSocket;
	DataInputStream serverIn;
	DataOutputStream serverOut;
	StatusCheckerThread() {
	}
	void statusConnectToServers() {
		String status;
		serverInfo server = null;
		Iterator serverIter = ConnectionTable.Servers.iterator();
		while (serverIter.hasNext()) {
			try {
				server = (serverInfo) serverIter.next();
				statusSocket = new Socket("localhost", server.getStatusPort());
				serverIn=new DataInputStream(statusSocket.getInputStream());
				serverOut=new DataOutputStream(statusSocket.getOutputStream());
				status = serverIn.readUTF();
				serverOut.writeUTF(" LoadBalancer OK");
				System.out.println(LoadBalancer.getCurrentTime() + "::" + "Server at listenport " + server.getConnPort() + "sent  a " + status);
				statusSocket.close();
			} catch ( IOException e) {
				// Server is down, remove this from the connection table.
				System.out.println(LoadBalancer.getCurrentTime() + "::" + " Server listening at port " + server.getConnPort()+ "is down");
				ConnectionTable.removeServer(server);
				break;
			}
		}
	}

	public void run() {
		System.out.println(LoadBalancer.getCurrentTime() + "::" +"Starting Status checker thread");

		serverInfo server;
		String status;
		while(true) {
			try {
				//System.out.println("StatusChecker: Before Acquire");
				ConnectionTable.serverListLock.acquire();
				//System.out.println("StatusChecker: After acquire");
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Iterator serverIter = ConnectionTable.Servers.iterator();
			statusConnectToServers();
				///System.out.println("StatusChecker: Before release");
				ConnectionTable.serverListLock.release();
				//System.out.println("StatusChecker: After release");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}




