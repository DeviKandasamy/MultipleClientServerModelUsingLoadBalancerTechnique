package FinalProject;

import java.net.*; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Calendar;
import java.io.*;

public class Server {
	
	ServerSocket serverSocket;
	Socket lbConnectSocket, serverAcceptSocket;
	DataInputStream clientIn;
	DataOutputStream clientOut;
	
	Server(int serverListenPort, int lbPort, int statusPort )  {
		String s;

		//Connect to LoadBalancer first to introduce the server, send the Port no. that the server listens on for 
		// Data connections and Status query connections
		try {
			lbConnectSocket = new Socket("localhost", lbPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DataOutputStream lbOut = null;
		try {
			lbOut = new DataOutputStream(lbConnectSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			lbOut.writeUTF(Integer.toString(serverListenPort));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			lbOut.writeUTF(Integer.toString(statusPort));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			lbConnectSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Create Status thread
		StatusThread sThreadObj = new StatusThread(statusPort);
		Thread statusThread = new Thread(sThreadObj);
		statusThread.start();
		//Listen for incoming connections
		try {
			serverSocket = new ServerSocket(serverListenPort);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(timeStamp.getCurrentTime() + "::" +"Waiting for client connections");
		while (true) {
			try {
				serverAcceptSocket = serverSocket.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ServerConnectionThread cThreadObj = new ServerConnectionThread(serverAcceptSocket );
			// Pick a server socket to use with this client connection
			//Create a Thread, call Thread start
			Thread  connThread = new Thread(cThreadObj);
			connThread.start();
		}
	}
	
	public static void main(String[] args){

		//Server listen port, Load Balancer Server-listen port,  Server Status port
		new Server(50000, 26000, 34000);

		//new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));			

	}


}

class timeStamp{
	static String getCurrentTime()
	{
		Calendar now = Calendar.getInstance();
		int h = now.get(Calendar.HOUR_OF_DAY);
		int m = now.get(Calendar.MINUTE);
		int s = now.get(Calendar.SECOND);
		String str ="<Server>" +  h + ":" + m + ":" + s;
		return str;
	}
}

class ServerConnectionThread implements Runnable{
	ResultSet res;
	Connection conn;
	Socket clientSocket;
	DataInputStream clientIn;
	DataOutputStream clientOut;
	ServerConnectionThread(Socket s1) {
		clientSocket = s1;
	}
	public void run() {
		String s;
		//System.out.println("Will start reading client byes now");
		try {
			clientIn = new DataInputStream(clientSocket.getInputStream());
			clientOut = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			/*--Adding the JDBC driver. there is no need to change this statement.----*/
			/*---Make Sure the JDBC driver is included in the JRE system Library to access database---*/
			Class.forName("com.mysql.jdbc.Driver");

			/*---Here you enter the Database details....---*/
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/Database", "root", "mysql");
			/*---- 3306 is the port number---*/
			/*---"root" is the username you created for database---*/
			/*---"padpassword" is the password you created when setting up the database---*/

		}

		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		try 
		{
			//System.out.println("Server");
			

			@SuppressWarnings("deprecation")
			String command = clientIn.readUTF();
			System.out.println("Query:  " + command);

			/*-----Enter here to get info from database-----*/
			GetDetails(command);

		} 

		catch (Exception e) {
			e.printStackTrace();
		}


		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void GetDetails(String query)
	{
		try
		{
			
			/*----Split the single line into two commands----*/

			String newquery[] = query.split(" ");
			String query1 = newquery[0];

			String query2 = newquery[1];
			query2 = "'" + query2 + "'";

			System.out.println();

			/*---This command is used to get the desired result from database---*/

			//String NewCommand = " select * from database.patientData where PatientID = " +query1 +" and FirstName = " + query2;
			String NewCommand = " select * from database.patientData where PatientID = " +query1 ;
			System.out.println(timeStamp.getCurrentTime() + "::" + "Sql query : "+NewCommand);
			/*---Getting the output from database and priniting it ----*/
			int count = 0; 
			int whileCounter = 0;

			java.sql.Statement state = conn.createStatement();

			res = state.executeQuery(NewCommand);

			while(res.next())
			{
				count++;
			}
			String [] stringResult = new String[count];

			res.beforeFirst();




			while(res.next())
			{
				stringResult[whileCounter] = res.getString(1) +"  "+  res.getString(2) +"  " + res.getString(3)  +"  "+ res.getString(4) ;
				whileCounter++;
			}
			String n = " ";

			for(int i = 0; i < stringResult.length; i++)
			{
				/* The patient details stored in n*/
				n = n + stringResult[i];
			}
			System.out.println(timeStamp.getCurrentTime() + "::" + "Patient Details from DATABASE");
			System.out.println(n);
			System.out.println();
			System.out.println(" Sending Info to Client . .");
			clientOut.writeUTF(n + '\n');
			System.out.println(" Sent Info to Client . .");



		}
		catch(Exception ex)
		{

		}
	}
}
//Thread to listen for Load Balancer's status queries
class StatusThread implements Runnable{
	ServerSocket statusSocket;
	Socket statusAcceptSocket;
	int statusPort;
	DataInputStream lbIn;
	DataOutputStream lbOut;
	StatusThread(int portforStatus) {
		statusPort = portforStatus;
	}
	public void run() {
		String s;

		try {
			System.out.println(timeStamp.getCurrentTime() + "::" +"Starting Status thread");
			statusSocket = new ServerSocket(statusPort);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			try {
				statusAcceptSocket = statusSocket.accept();
				System.out.println(timeStamp.getCurrentTime() + "::" + "Incoming connection from Loadbalancer");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				lbIn = new DataInputStream(statusAcceptSocket.getInputStream());
				lbOut = new DataOutputStream(statusAcceptSocket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				lbOut.writeUTF("Server Status OK");
				s = lbIn.readUTF();
				System.out.println(timeStamp.getCurrentTime() + "::" +"Load balancer says " + s);
				lbOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

