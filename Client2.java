

import java.net.*; 
import java.util.Calendar;
import java.io.*;

public class Client2 {
	Socket clientSocket;
	DataInputStream serverIn;
	DataOutputStream serverOut;
	Client2() throws IOException {
		String s;
		System.out.println(getCurrentTime() + "::" +"Starting Client ..");
		clientSocket = new Socket("localhost", 38000);
		serverIn=new DataInputStream(clientSocket.getInputStream());
		serverOut=new DataOutputStream(clientSocket.getOutputStream());
		/*--------Reading file from The location specified--------*/
	      
		File file = new File("C:/Users/Devi/Documents/scu/COEN233/project/Project/Demo/March9/AmbikaPreethi/src/ClientServer/test2.txt");

		
		/*---- Creating an input stream to get data from file--------*/
		DataInputStream bin = null;
		
		try
		{
			
           FileInputStream fin = new FileInputStream(file);
      
           bin = new DataInputStream(fin);
           
           
           /*---- An input stream to receive Patient Data from server----*/
           BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
           
           /*-----An output stream to send out the text file to server------*/
           DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

            
            /*---------read file using BufferedInputStream------*/
           
           while( bin.available() > 0 ){
        	   
        	   @SuppressWarnings("deprecation")
        	   String toserver = bin.readLine();
        	   System.out.println(toserver);
        	   
        		 /*------- Send it out to the Server--------*/
        	   serverOut.writeUTF(toserver + '\n');
           }
           
           
           /*--- Reading data sent by server And displaying the output--------*/
           
           String PatientInfo = serverIn.readUTF(); 
           String UpdatedPatientInfo[] = PatientInfo.split(" ");
           String PatientId = UpdatedPatientInfo[1];
           String PatientFirstName = UpdatedPatientInfo[3];
           String PatientLastName = UpdatedPatientInfo[5];
           String PatientAge = UpdatedPatientInfo[7];
           System.out.println(Client2.getCurrentTime() + "::" +"Patient ID: " +PatientId);
           System.out.println("Pateint First Name: "+PatientFirstName);
           System.out.println("Patient Last Name "+PatientLastName);
           System.out.println("Patient Age: "+PatientAge);
           
   
   
		}
		
		
		catch(FileNotFoundException e)
		{
			System.out.println("File not found" + e);
		}
		
		
		catch(IOException ioe)
		{
			System.out.println("Exception while reading the file " + ioe); 
		}
	}
	static String getCurrentTime()
	{
		Calendar now = Calendar.getInstance();
		int h = now.get(Calendar.HOUR_OF_DAY);
		int m = now.get(Calendar.MINUTE);
		int s = now.get(Calendar.SECOND);
		String str = "<Client2> " + h + ":" + m + ":" + s;
		return str;
	}
	
	public static void main(String[] args){
		try{
			new Client2();			
		}catch (IOException e){}
	}
	
}
