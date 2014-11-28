//java server example - see http://www.binarytides.com/java-socket-programming-tutorial/
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
 

/**
 * Server meant for remote deployment.
 * 
 * File -> Export as Runnable JAR
 * Upload to wordwolfgame.com/server 
 * Putty into wordwolfgame.com       // WARNING - will connect to any site hosted on jasonmortara.com
 * run Putty command: java -jar servertest3.jar
 * Should see 'waiting for connection...'
 * Open local cmd prompt: telnet wordwolfgame.com 4000
 * Works with multiple clients. Also logs.
 * 
 * To kill remote process open another remote putty session and use: pkill -9 -f <jarname> 
 * @author jason
 *
 */
public class WWSocketServer
{
    private static ServerSocket s;
    private static Socket conn;
    
    private static FileHandler logFileHandler;
    public static Logger log;
    private static SimpleFormatter logFormatter;
    
    //message prefixes
    public static final String ECHO = "echo:";
    public static final String MESSAGE_PLAYER_PORT = "messagePlayer_port_";
    
    

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
                new ClientHandler(conn).start();
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
 
class ClientHandler extends Thread
{
	private Logger log;
    private Socket conn;
     
    ClientHandler(Socket conn)
    {
    	log = WWSocketServer.log;
    	log.info( "wwss ClientHandler constructor." );
        this.conn = conn;
    }
 
	public void run()
    {
		String line;
		//String input = "";
         
        try
        {
            //get socket writing and reading streams
            DataInputStream in = new DataInputStream(conn.getInputStream());
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
            PrintStream out = new PrintStream(conn.getOutputStream());
 
            //Send welcome message to client
            out.println("Welcome to the WW Server!\n");
            out.println("Server Version " + Model.VERSION);
            out.println("You are on port: " + this.conn.getPort() );
            
            createPlayer( this.conn );
 
            //Now start reading input from client
            while( (line = br.readLine()) != null && !line.equals(".") && !line.equals("bye") )
            {
            	logMsg( "received: " + line );
            	
           		int firstColonIndex = line.indexOf( ":" );
           		
           		if ( firstColonIndex > -1 )	//TODO: improve this method of identifying prefixes
           		{
           			logMsg( "Colon character found. Assume this indicates a message prefix. Found at char: " + firstColonIndex );
           			Boolean prefixMatched = false;
                	String prefix = line.substring( 0, firstColonIndex + 1 );
                	String strippedLine = line.substring( firstColonIndex + 1, line.length() );
                	int lastUnderscoreInPrefixIndex = prefix.lastIndexOf( "_" );	//this underscore is not used in all prefix cases
                	logMsg("Message prefix is: " + prefix);
                	
                	
                	// parse format example: 'echo:hello'
                	if( prefix.equals( WWSocketServer.ECHO ) )
                	{
                		prefixMatched = true;
    	                 //reply with the same message, adding some text
    	            	logMsg("echo: Server thread sending back : " + " " + strippedLine);
    	            	out.println( "wwss ClientHandler echoing: " + " " + line );
                	}
                	
                	// parse format example: 'messagePlayer_port_1234:hello other player'
                	if ( prefix.startsWith( WWSocketServer.MESSAGE_PLAYER_PORT ) )
                	{
                		prefixMatched = true;
                   		String destPortStr = line.substring( lastUnderscoreInPrefixIndex + 1, firstColonIndex );
                   		int destPort = new Integer( destPortStr );
                		logMsg( "parsed destPort is: " + destPort ); //TODO: this may not correctly parse all values
                		
                		////////////// START HERE. THE PORT SEEMS TO BE DETECTED. WE JUST NEED TO TRIAGE THE MESSAGE TO DO A COMMAND INSTEAD OF AN ECHO.
                		// make new player!
                		
                	}
                	
                	// unknown prefix or no prefix
                	if ( !prefixMatched )
                	{
                		logMsg( "Unknown message prefix." );
                	}
           			
           		}
            	
            }
             
            //client disconnected, so close socket
            logMsg("Client disconnected. Closing socket.");
            conn.close();
        }
       
        catch (IOException e)
        {
            log.info("wwss IOException on socket : " + e);
            e.printStackTrace();
        }
    }
	
	private void createPlayer( Socket conn )
	{
		Player player = new Player( conn );
		
		if ( Model.players == null )
		{
			Model.players = new ArrayList<Player>();
		}
		
		Model.players.add( player );
		
		
	}
	
	public void logMsg(String msg)
	{
		log.info("wwss ClientHandler " + conn.getPort() + " " + msg);
	}
}