package httpServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class httpServer {
	
	public static void main(String[] args) {
		
		//Useless flag used to correct "End never reached Error"
		boolean loop = true;
		int port = 50008;
		int i = 0;
		
		try{
	        //Create the socket
			ServerSocket serverSocket = new ServerSocket(port); //Instanciando el server socket
	        System.out.println("Socket created on port: " + port);
	        System.out.println("Waiting for connections");
			
			while(loop){
	        	//Wait for connections
	        	Socket clientSocket = serverSocket.accept(); //Keep listening until you receive a client.
	        	System.out.println("New Connection" + ++i);
	        
	        	//Create the handler for each client connection
				httpHandler handler = new httpHandler(clientSocket, i); 
				//el handler es el thread. le pasas el socket, el Input stream y el output stream.
	        
	        	//Start handler has a thread
	       		handler.start();
			}

	        //Close the server socket
	        serverSocket.close();
	        
			} catch (IOException e) {
		}	
	}
}

