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
    public static final String ECHO						= "/echo:";
    public static final String SET_USERNAME				= "/setUsername:";
    public static final String GET_USERNAME				= "/getUsername";
    public static final String GET_OPPONENT_USERNAMES	= "/getOpponentUsernames";			// needs no colon or params
    public static final String GET_OPPONENT_PORTS		= "/getOpponentPorts";				//TODO // needs no colon or params
    public static final String MESSAGE_PLAYER_PORT		= "/messagePlayer_port_";			//TODO // must append colon
    public static final String SELECT_OPPONENT_PORT		= "/selectOpponent_port_";			//TODO // must append colon
    public static final String SELECT_OPPONENT_USERNAME	= "/selectOpponentUsername:";		// must append colon
    public static final String MESSAGE_OPPONENT 		= "/messageOpponent:";
	public static final String SEND_NEW_CURRENT_SCORE	= "/sendNewCurrentScore:";
    
    

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
            
            // create new Player
            createPlayer( this.conn );
 
            //Now start reading input from client
            while( (line = br.readLine()) != null && !line.equals(".") && !line.equals("bye") )
            {
            	logMsg( "received: " + line );
            	
       			Boolean prefixMatched = false;
       			String slash = "/";
           		int firstColonIndex = line.indexOf( ":" );
           		int lastUnderscoreInPrefixIndex;
           		String prefix;
           		String strippedLine = "";
           		Player originatingPlayer = this.getPlayerOnPort( this.conn.getPort() );
           		//Player player;
           		String username;
           		Socket playerConn;
           		PrintStream playerOut;           		
           		String destUsername;
           		String destPortStr;
           		Integer destPort;
           		Player destPlayer;
           		Socket opponentConn;
           		PrintStream opponentOut;
           		Player opponent;
           		
           		if ( line.charAt( 0 ) == slash.charAt( 0 ) )	// messages starting with a slash are assumed to be intended for parsing
           		{
           			logMsg( "Slash found as first char. Assume this indicates a message prefix.");
           			
           			// determine prefix 
           			
           			// for colon-formatted prefixes
           			if ( firstColonIndex > -1 )
           			{
           				prefix = line.substring( 0, firstColonIndex + 1 );
           				if ( prefix != null && prefix.length() > -1 )
           				{
           					strippedLine = line.substring( firstColonIndex + 1, line.length() );
           				}
           			}
           			// for prefixes not including a colon, no params (strippedLine) are looked for
           			else
           			{
           				prefix = line.substring( 0, line.length() );
           			}
           			logMsg("Received message: " + line);

           			lastUnderscoreInPrefixIndex = prefix.lastIndexOf( "_" );	//this underscore is not used in all prefix cases
                	
                	
                	// parse format example: '/echo:hello'
                	if( prefix.equals( WWSocketServer.ECHO ) )
                	{
                		prefixMatched = true;
                		//reply with the same message
                		logMsg("echo: Server thread sending back : " + " " + strippedLine);
                		out.println( "wwss ClientHandler echoing: " + " " + strippedLine );
                	}

                	
                	// parse format example: '/setUsername:jimmyjones' 
                	if ( prefix.startsWith( WWSocketServer.SET_USERNAME ) )
                	{
                		prefixMatched = true;
                		username = line.substring( firstColonIndex + 1, line.length() );
                		logMsg( "parsed username is: " + username );
                		
                		if ( originatingPlayer.getUsername() == null )
            			{
                			logMsg( "ok, your username has not been set yet..." );
                    		if ( getPlayerByUsername( username ) == null )	//TODO: some validation step. assume valid username input for now
                    		{
                    			logMsg( "ok, that username is available..." );
                        		
                    			originatingPlayer.setUsername( username );
                    			//playerOut = new PrintStream( originatingPlayer.getConn().getOutputStream() ); 
                    			out.println( "wwss ClientHandler: confirmed: username is assigned to you: " + originatingPlayer.getUsername() );
                    		}
            			}
                		else
                		{
                			logMsg( "WARNING - your username has already been set: " + originatingPlayer.getUsername() );
                		}
                		
                	}
                	
                	
                	// parse format example: '/getUsername'
                	if( prefix.startsWith( WWSocketServer.GET_USERNAME ) )
                	{
                		prefixMatched = true;
                		logMsg("echo: Server thread sending back : " + originatingPlayer.getUsername() );
                		out.println( "wwss ClientHandler: your username is: " + originatingPlayer.getUsername() );
                	}
                	

                	// parse format example: 'getOpponentPorts' 
                	if ( prefix.equals( WWSocketServer.GET_OPPONENT_PORTS ) )
                	{
                		prefixMatched = true;
                		String portsList = "";
                		for ( Player player : Model.players )
                		{
                			if ( !player.equals( originatingPlayer  ) )
                			{
                				portsList += player.getPort();
                				portsList += " ";
                			}
                		}
                		out.println( "wwss ClientHandler: available opponent ports: " + portsList );
                	}
                	
                	
                	// parse format example: 'getOpponentUsernames' 
                	if ( prefix.equals( WWSocketServer.GET_OPPONENT_USERNAMES ) )
                	{
                		prefixMatched = true;
                		String usernames = "";
                		if ( Model.players != null )
                		{
                			for ( Player player : Model.players )
                			{
                				if ( !player.equals( originatingPlayer  ) )
                				{
                					usernames += player.getUsername();
                					usernames += " ";
                				}
                			}
                			out.println( "wwss ClientHandler: available opponent usernames: " + usernames );
                		}
                	}
                	
                	
                	
                	// parse format example: 'messagePlayer_port_1234:hello other player'
                	if ( prefix.startsWith( WWSocketServer.MESSAGE_PLAYER_PORT ) )
                	{
                		prefixMatched = true;
                		destPortStr = line.substring( lastUnderscoreInPrefixIndex + 1, firstColonIndex );
                		destPort = Integer.parseInt( destPortStr );
                		logMsg( "parsed destPort is: " + destPort ); //TODO: this may not correctly parse all values
                		
                		if ( playerExists( destPort ) )
                		{
                			destPlayer = getPlayerOnPort( destPort );
                			if ( destPlayer != null )
                			{
                				Socket destConn = destPlayer.getConn();
                				PrintStream destPlayerOut = new PrintStream( destConn.getOutputStream() );
                				destPlayerOut.println( "wwss ClientHandler: message from player on port " + this.conn.getPort() + " to player on port " + destPort + ":" );
                				destPlayerOut.println( "wwss ClientHandler:" + strippedLine );
                			}
                			else 
                			{
                				out.println( "wwss ClientHandler: unknown player: " + destPort );
                			}
                		}
                		else 
                		{
                			out.println( "wwss ClientHandler: unknown player on requested port " + destPort );
                		}
                	}
                	
                	
                	// parse format example: 'selectOpponent_port_12345:' (colon required)
                	if ( prefix.startsWith( WWSocketServer.SELECT_OPPONENT_PORT ) )
                	{
                		prefixMatched = true;
                		destPortStr = line.substring( lastUnderscoreInPrefixIndex + 1, firstColonIndex );
                		destPort = Integer.parseInt( destPortStr );
                		logMsg( "parsed destPort is: " + destPort ); //TODO: this may not correctly parse all values
                		
                		if ( playerExists( destPort ) )
                		{
                			opponent = getPlayerOnPort( destPort );
                			if ( opponent != null )
                			{
                				opponentConn = opponent.getConn();
                				opponentOut = new PrintStream( opponentConn.getOutputStream() );		//TODO: when to close?
                				opponentOut.println( "wwss ClientHandler: player on port " + originatingPlayer.getPort() + " has selected you as their opponent. Your port: " + destPort );
                				originatingPlayer.setOpponent( opponent );
                				opponent.setOpponent( originatingPlayer );
                				
                				if ( originatingPlayer.getOpponent().equals( opponent ) && opponent.getOpponent().equals( originatingPlayer ) )
                				{
                					out.println( "wwss ClientHandler: confirmed: your opponent is now player on port: " + originatingPlayer.getOpponent().getPort() );
                					opponentOut.println( "wwss ClientHandler: confirmed: your opponent is now player on port: " + opponent.getOpponent().getPort() );	// opponent's opponent is originating player
                				}	
                			}
                			else 
                			{
                				out.println( "wwss ClientHandler: unknown player: " + destPort );
                			}
                		}
                		else 
                		{
                			out.println( "wwss ClientHandler: unknown player on requested port " + destPort );
                		}
                	}
                	
                	
                	// parse format example: '/selectOpponentUsername:jimmyjones' (colon required)
                	if ( prefix.startsWith( WWSocketServer.SELECT_OPPONENT_USERNAME ) )
                	{
                		prefixMatched = true;
                		destUsername = strippedLine;	//line.substring( lastUnderscoreInPrefixIndex + 1, firstColonIndex );
                		logMsg( "parsed destUsername is: " + destUsername ); 
                		
                		if ( playerExists( destUsername ) )
                		{
                			opponent = getPlayerByUsername( destUsername );
                			if ( opponent != null )
                			{
                				opponentConn = opponent.getConn();
                				opponentOut = new PrintStream( opponentConn.getOutputStream() );		//TODO: when to close?
                				opponentOut.println( "wwss ClientHandler: player " + originatingPlayer.getUsername() + " has selected you as their opponent. " );
                				originatingPlayer.setOpponent( opponent );
                				opponent.setOpponent( originatingPlayer );
                				
                				if ( originatingPlayer.getOpponent().equals( opponent ) && opponent.getOpponent().equals( originatingPlayer ) )
                				{
                					out.println( "wwss ClientHandler: confirmed: your opponent is now: " + originatingPlayer.getOpponent().getUsername() );
                					opponentOut.println( "wwss ClientHandler: confirmed: your opponent is now: " + opponent.getOpponent().getUsername() );	// opponent's opponent is originating player
                				}	
                			}
                			else 
                			{
                				out.println( "wwss ClientHandler: unknown player username: " + destUsername );
                			}
                		}
                		else 
                		{
                			out.println( "wwss ClientHandler: unknown player username: " + destUsername );
                		}
                	}
                	
                	
                	// parse format example: 'messageOpponent:hello' 
                	if ( prefix.equals( WWSocketServer.MESSAGE_OPPONENT ) )
                	{
                		prefixMatched = true;
                		opponent = originatingPlayer.getOpponent();
                		
                		if ( opponent != null )
                		{
                			opponentConn = opponent.getConn();
                			opponentOut = new PrintStream( opponentConn.getOutputStream() );	//TODO: when to close?
                			opponentOut.println( "wwss ClientHandler: message from your opponent:" );
                			opponentOut.println( " " + strippedLine );
                		}
                		else 
                		{
                			out.println( "wwss ClientHandler: you have no opponent selected." );
                		}
                	}
                	
                	
                	// parse format example: '/sendNewCurrentScore:12' 
                	if ( prefix.equals( WWSocketServer.SEND_NEW_CURRENT_SCORE ) )
                	{
                		prefixMatched = true;
                		opponent = originatingPlayer.getOpponent();
                		
                		if ( opponent != null )
                		{
            				opponentConn = opponent.getConn();
            				opponentOut = new PrintStream( opponentConn.getOutputStream() );	//TODO: when to close?
            				opponentOut.println( "wwss ClientHandler: message from your opponent:" );
            				opponentOut.println( "My new score is: " + strippedLine );
                		}
            			else 
            			{
            				out.println( "wwss ClientHandler: you have no opponent selected." );
            			}
                	}
                	
                	
                	// unknown prefix or no prefix
                	if ( !prefixMatched )
                	{
                		logMsg( "Unknown message prefix. Don't forget trailing colon where necessary." );
                	}
           			
           		}
           		else
           		{
           			logMsg( "Entry ignored.");
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
		
		logMsg( " New Player Created! Num players now: " + Model.players.size() );
	}
	
	private Boolean playerExists( int port )
	{
		logMsg( "playerExists on port? " + port );

		if ( Model.players == null ) 
		{
			logMsg( "playerExists: players list is null." );
			return false;
		}
		
		Boolean playerFound = false;
		for ( Player player : Model.players )
		{
			if ( player.getPort() == port )
			{
				playerFound = true;
				break;
			}
		}
		
		logMsg( "playerFound: " + playerFound );
		return playerFound;
	}
	
	private Boolean playerExists( String username ) 
	{
		logMsg( "playerExists? " + username );

		if ( Model.players == null ) 
		{
			logMsg( "playerExists: players list is null." );
			return false;
		}
		
		Boolean playerFound = false;
		for ( Player player : Model.players )
		{
			if ( player.getUsername().equals( username ) )
			{
				playerFound = true;
				break;
			}
		}
		
		logMsg( "playerFound: " + playerFound );
		return playerFound;
	}

	private Player getPlayerOnPort( int port )
	{
		logMsg( "getPlayerOnPort on port " + port );
		
		if ( Model.players == null ) 
		{
			logMsg( "getPlayerOnPort: players list is null." );
			return null;
		}
		
		Player existingPlayer = null;
		for ( Player player : Model.players )
		{
			if ( player.getPort() == port )
			{
				existingPlayer = player;
			}
		}
		
		return existingPlayer;
	}
	
	private Player getPlayerByUsername( String username ) 
	{
		logMsg( "playerExists? " + username );

		if ( Model.players == null ) 
		{
			logMsg( "playerExists: players list is null." );
			return null;
		}
		
		Player existingPlayer = null;
		
		for ( Player player : Model.players )
		{
			if ( player.getUsername() == null )
			{
				logMsg( "getPlayerByUsername: no player found with username: " + username );
				return null;
			}
			if ( player.getUsername().equals( username ) )
			{
				existingPlayer = player;
				logMsg( "getPlayerByUsername: player found: " + existingPlayer.getUsername() );
				break;
			}
		}
		
		return existingPlayer;
	}

	/*
	private void selectOpponentOnPort ( int port )
	{
		
	}
	*/
	public void logMsg(String msg)
	{
		log.info("wwss ClientHandler " + conn.getPort() + " " + msg);
	}
}

