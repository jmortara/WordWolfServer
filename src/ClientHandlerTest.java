import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.mortaramultimedia.wordwolf.shared.constants.*;
import com.mortaramultimedia.wordwolf.shared.messages.*;
import database.*;


/**
 * Client Handler Thread
 * @author jason mortara
 */
class ClientHandlerTest extends Thread
{
	private Logger log;			// reference to WWSocketServer's Log
    private Socket connection;	// passed from the main WWSocketServer class in this class' constructor
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    /**
     * ClientHandlerTest constructor
     * @param connection 
     */
    ClientHandlerTest(Socket connection)
    {
    	log = WWSocketServer.log;
    	log.info( "wwss ClientHandlerTest constructor." );
        this.connection = connection;
        
        try
		{
        	in 	= new ObjectInputStream(connection.getInputStream());
	    	log.info( "wwss Created ObjectInputStream." );
	    	
			out	= new ObjectOutputStream(connection.getOutputStream());
	    	log.info( "wwss Created ObjectOutputStream." );
		} 
        catch (IOException e)
		{
			e.printStackTrace();
		}
        
        
//    	log.info("wwss testing DB...");
//        MySQLAccessTester.testConnection();
    }
 
    
	public void run()
    {
//		String line;
		//String input = "";
         
        try
        {
            //get socket's object reading and writing streams
        	
          
            //Now start reading input from client
            while( true )
            {
                Object obj = in.readObject();
            	logMsg( "RECEIVED: " + obj );
            	
            	/**
            	 * If receiving a SimpleMessage, log the message. If echo was requested, send it back to the client as well.
            	 */
            	if(obj instanceof SimpleMessage)
            	{
            		handleSimpleMessage(((SimpleMessage) obj), out);
            	}
            	else if(obj instanceof ConnectToDatabaseRequest)
            	{
            		handleConnectToDatabaseRequest(((ConnectToDatabaseRequest) obj), out);
            	}
            	else if(obj instanceof LoginRequest)
            	{
            		handleLoginRequest(((LoginRequest) obj), out);
            	}
            	
            	out.flush();
            }
            
        }
       
        catch (IOException e)
        {
        	log.info("wwss IOException on socket : " + e);
        	e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            log.info("wwss IOException on socket : " + e);
            e.printStackTrace();
        }
    }
	
	/**
	 * Handler for incoming SimpleMessage objects.
	 * @param msgObj
	 * @param out
	 */
	private void handleSimpleMessage(SimpleMessage msgObj, ObjectOutputStream out)
	{
    	log.info("wwss handleSimpleMessage: " + msgObj);
		
		String appendedMsg = "";
		if((msgObj.getEcho() == true))
		{
			// if hello received, say hello back
			if(msgObj.getMsg().contains(Constants.HELLO_SERVER))
			{
				appendedMsg = Constants.HELLO_CLIENT;
			}
			// otherwise reply by echoing the received message
			else appendedMsg = msgObj.getMsg();
		}
		try
		{
			out.writeObject(new SimpleMessage(("SENDING OBJECT TO CLIENT " + appendedMsg), false));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Handler for DatabaseConnectionRequest objects.
	 * @param request
	 * @param out
	 */
	private void handleConnectToDatabaseRequest(ConnectToDatabaseRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleConnectToDatabaseRequest");
    	
    	String responseMsg;
    	SimpleMessage responseObj;
    	
    	// attempt the login
		Boolean dbConnectionSucceeded = MySQLAccessTester.testConnection();

		// send the connection test response
		if(dbConnectionSucceeded)
		{
			responseMsg = "Database Connection Succeeded.";
		}
		else
		{
			responseMsg = "Database Connection FAILED.";
		}
		responseObj = new SimpleMessage(responseMsg, false);
		try
		{
			out.writeObject(responseObj);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Handler for incoming LoginRequest objects.
	 * @param request
	 * @param out
	 */
	private void handleLoginRequest(LoginRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleLoginRequest: " + request);
    	
    	String responseMsg;
    	SimpleMessage responseObj;
    	
    	// attempt the login
		Boolean loginSucceeded = MySQLAccessTester.attemptLogin( (LoginRequest) request );
		
		// send the login response
		if(loginSucceeded)
		{
			//TODO: swap the message for the real LoginResponse obj
			responseMsg = "Login Succeeded with request: " + request;
		}
		else
		{
			responseMsg = "Login FAILED with request: " + request;
		}
		responseObj = new SimpleMessage(responseMsg, false);
		try
		{
			out.writeObject(responseObj);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void logMsg(String msg)
	{
		log.info("wwss ClientHandlerTest " + connection.getPort() + " " + msg);
	}
}// end class ClientHandlerTest
