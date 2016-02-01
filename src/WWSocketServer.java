import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import com.mortaramultimedia.wordwolf.shared.constants.*;
import com.mortaramultimedia.wordwolf.shared.messages.*;

/**
 * Word Wolf Server - meant for remote deployment.
 * @author Jason Mortara
 */
//TODO make Singleton?
public class WWSocketServer
{
    private static ServerSocket s;
    private static Socket conn;
    
    private static FileHandler logFileHandler;
    public static Logger log;
    private static SimpleFormatter logFormatter;
    
    //message prefixes
//    public static final String ECHO						= "/echo:";
//    public static final String SET_USERNAME				= "/setUsername:";
//    public static final String GET_USERNAME				= "/getUsername";
//    public static final String GET_OPPONENT_USERNAMES	= "/getOpponentUsernames";			// needs no colon or params
    //public static final String GET_OPPONENT_PORTS			= "/getOpponentPorts";				//TODO // needs no colon or params
    //public static final String MESSAGE_PLAYER_PORT		= "/messagePlayer_port_";			//TODO // must append colon
    //public static final String SELECT_OPPONENT_PORT		= "/selectOpponent_port_";			//TODO // must append colon
//    public static final String SELECT_OPPONENT_USERNAME	= "/selectOpponentUsername:";		// must append colon
//    public static final String MESSAGE_OPPONENT 		= "/messageOpponent:";
//	public static final String SEND_NEW_CURRENT_SCORE	= "/sendNewCurrentScore:";
    
    

    public static void main(String args[])
    {
    	// set up logging
        try
        {
        	System.out.println("wwss main: setting up logging");
        	log = Logger.getLogger("ServerLog");
            logFileHandler = new FileHandler("serverlog.log");
            log.addHandler( logFileHandler );
            logFormatter = new SimpleFormatter();
            logFileHandler.setFormatter( logFormatter );
            log.info("wwss Log Start ---------------------------------- ");
        }
        catch (SecurityException se)
        {
        	se.printStackTrace();
        }
        catch (IOException ioe)
        {
        	ioe.printStackTrace();
        }
        log.info("wwss First log message");
        log.info("wwss Server version: " + Model.VERSION);

        Model.init();
        
        // socket setup
        s = null;
        conn = null;
        
        try
        {
        	log.info("wwss main: attempting to create a ServerSocket on port: " + Model.PORT);
        	
            //1. creating a server socket - 1st parameter is port number and 2nd is the backlog
            s = new ServerSocket(Model.PORT, Model.MAX_BACKLOG_CONNECTIONS);
             
            //2. Wait for an incoming connection
            echo("main: Server socket created. Waiting for connection...");
             
            while(true)
            {
                //get the connection socket
                conn = s.accept();
                 
                //print the hostname and port number of the connection
                echo("main: Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());
                 
                //create new thread to handle client
                new ClientHandlerTest(conn).start();
            }
        }
         
        catch(IOException e)
        {
            System.err.println("wwss IOException");
        }
         
        //5. close the connections and stream
        try
        {
            s.close();
        }
         
        catch(IOException ioException)
        {
            System.err.println("wwss Unable to close. IOexception");
        }
    }
     
    public static void echo(String msg)
    {
        System.out.println("wwss main: " + msg);
    }
    
    public static void logAndEcho(String msg)
    {
    	
    }
}
 
// moved ClientHandler to external .java class
