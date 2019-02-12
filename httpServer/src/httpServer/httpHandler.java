package httpServer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class httpHandler extends Thread{
	// Data stream and output streams for data transfer
	private 	BufferedReader 		clientInputStream;
    private 	DataOutputStream 	clientOutputStream;
    
    //Client socket for maintaing connection with the client
    private 	Socket 				clientSocket;
    private 	int 				conectionID;
    /***************************************************************************
     * Constructor
     * @param clientSocket: client socket created when the client connects to
     * the server
     */
    public httpHandler (Socket clientSocket, int clientId)
    {
    	try{
    		//Assign to my local socket, the socket I receive.
    		this.clientSocket = clientSocket;
    		this.conectionID = clientId;
    		//Declaring reader
    	    clientInputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    	     
    	    //Declaring writer
    	    clientOutputStream = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}    
    }
    
    public void run()
    {
    	try {
			dataTransfer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /*****************************************************************************
     * Data transfer method
     * Tasks: Manage connection with the client.
     * 		Receive commands from the client
     * 		Invoke the appropriate method depending on the received command
     * @throws Exception 
     * 
     *****************************************************************************/
	public void dataTransfer() throws Exception {
		try{
			//Wait for command
        	System.out.println("Waiting for a command");
        	
        	
        	clientSocket.setKeepAlive(true);
        	
        	do {
        		
        		this.fileWResponse();
        		clientSocket.setSoTimeout(30000);
        		
        	} while(clientSocket.getKeepAlive());
	        
	        //Close connection and socket
        	
	        
		} catch(SocketTimeoutException e)
		{
			System.out.println("Time OUT!");
			clientInputStream.close();
        	clientOutputStream.close();
	        clientSocket.close();
	        System.out.println("Connection Finished");
			//e.printStackTrace();
		}
	}
	
	private String splitRequest() throws IOException {
		
		String clientMessage = clientInputStream.readLine();
        System.out.println("Request: " + clientMessage + "Connection Id: " + conectionID);
        
        //Print the rest of the header
        String rest = "";
        while (!(rest = clientInputStream.readLine()).isEmpty()) {
        	System.out.println("Request: " + rest);
        }
		
		String parts[] = clientMessage.split("\\s+"); //Split the request
        String command = parts[0]; //Store the command
        String file = parts[1]; //Store the file requested
        System.out.println("Command: " + command + " File request: " + file);
        
        String fileRequest = ("." + file);
        
        if (fileRequest.equals("./")) {
        	fileRequest = "./index.html";
        }
        	
    	System.out.println("File Complete: " + fileRequest);    

		return fileRequest;
	}
	
	public void fileWResponse() throws Exception{
		
		String fileName = splitRequest();
		String contentType = getType(fileName);
		String response="";
		
		//File reader object
        FileInputStream location = null;
        
        //Search of the files
        File file = new File(fileName);
             
        try {
            location = new FileInputStream(file); //get the input of the file
            
            response= "HTTP/1.1 200 OK\r\n"; //VERSION 
            response+= "Content-type: "+contentType+" \r\n";//response is in html format
            response+= "Content-length: " +String.valueOf(file.length())+"\r\n";//length of response file
            response+= "\r\n";//after blank line we have to append file data

        }
        catch (FileNotFoundException e) {
        	
        	//If the file its not found, send the page of error.
        	System.out.println("File Not Found! Error 404");
        	File errorFile = new File("." + "/404.html");
            location = new FileInputStream(errorFile);
        	
            response= "HTTP/1.1 404 Not found\r\n"; 
            response+= "Content-Type: text/html \r\n";
            response+= "Content-Length: " +String.valueOf(errorFile.length())+"\r\n";
            response+= "\r\n";//after blank line we have to append file data
        }

        System.out.println("RESPONSE: " + response);
        clientOutputStream.writeBytes(response); //pass the response to the Output in bytes;
        sendFile(location);
        //clientOutputStream.flush();
        location.close();
    }    
	
	public void sendFile(FileInputStream filetoSend)  throws Exception {
		
		try {
			byte[] buffer = new byte[1024];
	        int size = 0;
	        // Copy requested file into the socket�s output stream.
	        while((size = filetoSend.read(buffer)) > 0 ) {
	        	
	            	clientOutputStream.write(buffer, 0, size);
	            	clientOutputStream.flush();
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getType(String fileName) {
		
		String contentType = "";
		
		//Check what file has being ask
	    if (fileName.endsWith(".html")){
	    	contentType = "text/html";
	    	System.out.println("CONTENT TYPE 1: " + contentType);
	    }
	    else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
	    	contentType = "image/jpeg";
	    	System.out.println("CONTENT TYPE: " + contentType);
	    }
	    else {
	    	contentType = "text/html";
	    	System.out.println("CONTENT TYPE 2: " + contentType);
	    }
	    
	    return contentType;
    }
}	