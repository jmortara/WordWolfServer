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


/**
 * Client Handler Thread
 * @author jason mortara
 */
class ClientHandler extends Thread
{
	private Logger log;			// reference to WWSocketServer's Log
    private Socket connection;	// passed from the main WWSocketServer class in this class' constructor
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    /**
     * ClientHandler constructor
     * @param connection 
     */
    ClientHandler(Socket connection)
    {
    	log = WWSocketServer.log;
    	log.info( "wwss ClientHandler constructor." );
        this.connection = connection;
        
        try
		{
			in 	= new ObjectInputStream(connection.getInputStream());
		} 
        catch (IOException e)
		{
			e.printStackTrace();
		}
    }
 
    
	public void run()
    {
//		String line;
		//String input = "";
         
        try
        {
            //get socket's object reading and writing streams
        	
//          DataInputStream in = new DataInputStream(connection.getInputStream());
//        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
//          PrintStream out = new PrintStream(connection.getOutputStream());
 
            //Send welcome message to client
//            out.writeObject(new SimpleMessage("Welcome to the WW Server!\n"));
//            out.writeObject(new SimpleMessage("Server Version " + Model.VERSION));
//            out.writeObject(new SimpleMessage("You are on port: " + this.connection.getPort() ));
            
            // create new Player
            createPlayer( this.connection );
 
            //Now start reading input from client
//            while( (line = br.readLine()) != null && !line.equals(".") && !line.equals("bye") )
            Object obj = in.readObject();
            while( obj != null /*&& !line.equals(".") && !line.equals("bye")*/ )
            {
            	logMsg( "received: " + obj );
            	
//       			Boolean prefixMatched = false;
//       			String slash = "/";
//           		int firstColonIndex = obj.toString().indexOf( ":" );
//           		int lastUnderscoreInPrefixIndex;
//           		String prefix;
//           		String strippedLine = "";
           		Player originatingPlayer = this.getPlayerOnPort( this.connection.getPort() );
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
           		
           		if ( obj != null )	//TODO: redundant null check
           		{
           			if(obj instanceof SimpleMessage)
           			{
           				SimpleMessage o = (SimpleMessage) obj;
                		logMsg("echo: ClientHandler received SimpleMessage: " + o.getMsg());
                		if(o.getEcho() == true)
                		{
                			out.writeObject( "wwss ClientHandler echoing back to the client: " + o.getMsg());
                		}
           			}
           			else if (obj instanceof LoginRequest)
           			{
                		username = ((LoginRequest) obj).getUserName();
                		logMsg( "parsed username is: " + username );
                		
                		if ( originatingPlayer.getUsername() == null )
            			{
                			logMsg( "ok, your username has not been set yet..." );
                    		if ( getPlayerByUsername( username ) == null )	//TODO: some validation step. assume valid username input for now
                    		{
                    			logMsg( "ok, that username is available..." );
                        		
                    			originatingPlayer.setUsername( username );
                    			//playerOut = new PrintStream( originatingPlayer.getConn().getOutputStream() ); 
                    			out.writeObject( "wwss ClientHandler: confirmed: username is assigned to you: " + originatingPlayer.getUsername() );
                    		}
            			}
                		else
                		{
                			logMsg( "WARNING - your username has already been set: " + originatingPlayer.getUsername() );
                		}
           			}
           			else if (obj instanceof CreateNewAccountRequest)
           			{
           				//TODO: fill in
           			}
           			else if (obj instanceof CreateGameRequest)
           			{
           				//TODO: fill in
           			}
           			else if (obj instanceof GameMove)
           			{
           				//TODO: fill in
           			}
           			
           			// placeholders
           			else if (obj.toString() == "GetUserNameRequest")
           			{
           				logMsg("echo: Server thread sending back : " + originatingPlayer.getUsername() );
           				out.writeObject( new SimpleMessage("wwss ClientHandler: your username is: " + originatingPlayer.getUsername()));
           			}
           			
           			else if (obj.toString() == "GetOpponentPortsRequest")
           			{
                		out.writeObject( new SimpleMessage("wwss ClientHandler: requested opponents ports"));
                		String portsList = "";
                		for ( Player player : Model.players )
                		{
                			if ( !player.equals( originatingPlayer  ) )
                			{
                				portsList += player.getPort();
                				portsList += " ";
                			}
                		}
                		out.writeObject( new SimpleMessage("wwss ClientHandler: available opponent ports: " + portsList ));
           			}
           			
           			else if (obj.toString() == "GetOpponentUsernamesRequest")
           			{
           				out.writeObject( new SimpleMessage("wwss ClientHandler: requested opponent usernames"));
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
                			out.writeObject( new SimpleMessage("wwss ClientHandler: available opponent usernames: " + usernames ));
                		}
           			}
           			
           			else if (obj.toString() == "MessagePlayerPortRequest")
           			{
           				out.writeObject( new SimpleMessage("wwss ClientHandler: request to message player port"));
                		/*destPortStr = line.substring( lastUnderscoreInPrefixIndex + 1, firstColonIndex );
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
                				out.writeObject( "wwss ClientHandler: unknown player: " + destPort );
                			}
                		}
                		else 
                		{
                			out.writeObject( "wwss ClientHandler: unknown player on requested port " + destPort );
                		}*/
           			}
           			
           			else if (obj.toString() == "SelectOpponentOnPortRequest")
           			{
           				out.writeObject( new SimpleMessage("wwss ClientHandler: request to select an opponent on port"));
                		/*destPortStr = line.substring( lastUnderscoreInPrefixIndex + 1, firstColonIndex );
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
                					out.writeObject( "wwss ClientHandler: confirmed: your opponent is now player on port: " + originatingPlayer.getOpponent().getPort() );
                					opponentOut.println( "wwss ClientHandler: confirmed: your opponent is now player on port: " + opponent.getOpponent().getPort() );	// opponent's opponent is originating player
                				}	
                			}
                			else 
                			{
                				out.writeObject( "wwss ClientHandler: unknown player: " + destPort );
                			}
                		}
                		else 
                		{
                			out.writeObject( "wwss ClientHandler: unknown player on requested port " + destPort );
                		}*/
           			}
           			
           			else if (obj.toString() == "SelectOpponentByUsernameRequest")
           			{
           				out.writeObject( new SimpleMessage("wwss ClientHandler: request to select an opponent by username"));
                		/*destUsername = strippedLine;	//line.substring( lastUnderscoreInPrefixIndex + 1, firstColonIndex );
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
                					out.writeObject( "wwss ClientHandler: confirmed: your opponent is now: " + originatingPlayer.getOpponent().getUsername() );
                					opponentOut.println( "wwss ClientHandler: confirmed: your opponent is now: " + opponent.getOpponent().getUsername() );	// opponent's opponent is originating player
                				}	
                			}
                			else 
                			{
                				out.writeObject( "wwss ClientHandler: unknown player username: " + destUsername );
                			}
                		}
                		else 
                		{
                			out.writeObject( "wwss ClientHandler: unknown player username: " + destUsername );
                		}*/
           			}
           			
           			else if (obj.toString() == "MessageOpponentRequest")
           			{
           				out.writeObject( new SimpleMessage("wwss ClientHandler: request to message opponent"));
           				/*
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
                			out.writeObject( "wwss ClientHandler: you have no opponent selected." );
                		}
           				 */
           			}
           			
           			else if (obj.toString() == "SendNewCurrentScoreRequest")
           			{
           				out.writeObject( new SimpleMessage("wwss ClientHandler: request to select an opponent by username"));
                		/*opponent = originatingPlayer.getOpponent();
                		
                		if ( opponent != null )
                		{
            				opponentConn = opponent.getConn();
            				opponentOut = new PrintStream( opponentConn.getOutputStream() );	//TODO: when to close?
            				opponentOut.println( "wwss ClientHandler: message from your opponent:" );
            				opponentOut.println( "My new score is: " + strippedLine );
                		}
            			else 
            			{
            				out.writeObject( "wwss ClientHandler: you have no opponent selected." );
            			}*/
           			}
           			
           			else logMsg("ERROR - UNKNOWN OBJECT TYPE: " + obj.getClass().getSimpleName());
           		}
            	
           		out.flush();
            }
             
            //client disconnected, so close socket
            logMsg("Client disconnected. Closing socket.");
            connection.close();
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
    } // end run()
	
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
		log.info("wwss ClientHandler " + connection.getPort() + " " + msg);
	}
}// end class ClientHandler
