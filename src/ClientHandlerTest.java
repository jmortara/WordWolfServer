import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.mortaramultimedia.wordwolf.shared.constants.*;
import com.mortaramultimedia.wordwolf.shared.messages.*;
import database.*;
import test.MySQLAccessTester;


/**
 * Client Handler Thread
 * @author jason mortara
 */
class ClientHandlerTest extends Thread
{
	private Logger log;					// reference to WWSocketServer's Log
    private Socket connection;			// passed from the main WWSocketServer class in this class' constructor
    private ObjectInputStream in;		// the Object Input Stream  associated with this thread's socket connection
    private ObjectOutputStream out;		// the Object Output Stream associated with this thread's socket connection
    private MySQLAccess dataAccessObj;	// the JDBC access object (DAO) instance for this thread
    private Player player;				// the dedicated Player object on this Thread. Receives commands via run().
    
    
    /**
     * ClientHandlerTest constructor
     * @param connection 
     */
    ClientHandlerTest(Socket connection)
    {
    	log = WWSocketServer.log;
    	log.info( "wwss ClientHandlerTest constructor." );
        this.connection = connection;
        
        initObjectStreams();
        initDatabaseAccess();
    }
    
    /**
     * Init the object streams used to read and write data to and from the socket in this Thread.
     */
    private void initObjectStreams()
    {
    	log.info( "wwss initObjectStreams" );
    	
        try
		{
        	if(null == in)
        	{
        		in 	= new ObjectInputStream(connection.getInputStream());
	    		log.info( "wwss initObjectStreams: Created ObjectInputStream." );
        	}	
        	if(null == out)
        	{
        		out	= new ObjectOutputStream(connection.getOutputStream());				//TODO output stream is stored as class var, don't need to pass it in run()
        		log.info( "wwss initObjectStreams: Created ObjectOutputStream." );
        	}
		}
        catch (IOException e)
		{
			e.printStackTrace();
		}
    }
    
    /**
     * Init the DAO for accessing the database.
     */
    private void initDatabaseAccess()
    {
    	log.info( "wwss initDatabaseAccess" );
    	if (null == dataAccessObj)
    	{
        	dataAccessObj = new MySQLAccess();
        	log.info( "wwss initDatabaseAccess: DAO created." );
    	}
    }
 
    /**
     * Test the MySQL database connection.
     */
    private void testDBConnection()
    {
    	log.info( "wwss testDBConnection: testing DB... [DEPRECATED]" );
    	MySQLAccessTester.testConnection();
    }
    
	public void run()
    {
        try
        {
            // start reading input from client
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
            	/**
            	 * If receiving a ConnectToDatabaseRequest...
            	 */
            	else if(obj instanceof ConnectToDatabaseRequest)
            	{
            		handleConnectToDatabaseRequest(((ConnectToDatabaseRequest) obj), out);
            	}
            	/**
            	 * If receiving a LoginRequest...
            	 */
            	else if(obj instanceof LoginRequest)
            	{
            		handleLoginRequest(((LoginRequest) obj), out);
            		
            		//TODO: TEMPORARY TEST TO VALIDATE PLAYER CREATION. REMOVE.
            		getPlayerByUsername(((LoginRequest) obj).getUserName());
            	}
            	/**
            	 * If receiving a GetPlayerListRequest
            	 */
            	else if(obj instanceof GetPlayerListRequest)
            	{
            		handleGetPlayerListRequest(((GetPlayerListRequest) obj), out);
            	}
            	/**
            	 * If receiving a CreateNewAccountRequest...
            	 */
            	else if(obj instanceof CreateNewAccountRequest)
            	{
            		handleCreateNewAccountRequest(((CreateNewAccountRequest) obj), out);
            	}
            	/**
            	 * If receiving a CreateGameRequest...
            	 */
            	else if(obj instanceof CreateGameRequest)
            	{
            		handleCreateGameRequest(((CreateGameRequest) obj), out);
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
            log.info("wwss ClassNotFoundException on socket : " + e);
            e.printStackTrace();
        } 
        catch (SQLException e)
		{
            log.info("wwss SQLException on socket : " + e);
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
	 * If the login succeeds, create a Player obj and attach it to this thread.
	 * @param request
	 * @param out
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private void handleLoginRequest(LoginRequest request, ObjectOutputStream out) throws SQLException, IOException
	{
		log.info("wwss handleLoginRequest: " + request);
		
		// attempt the login and close any ResultSet
		LoginResponse loginResponse = dataAccessObj.login( (LoginRequest) request, false );
		
		// if a Player obj was returned in the response, set the player's connection to the one in this thread
		/*if(loginResponse.getPlayer() != null)
		{
			loginResponse.getPlayer().setConn(connection);
		}*/
		
		// send the login response
		/*if(loginResponse.getLoginAccepted())
		{
			responseMsg = "Login Succeeded with request: " + request;
		}
		else
		{
			responseMsg = "Login FAILED with request: " + request;
		}
		responseObj = new SimpleMessage(responseMsg, false);*/
		log.info("wwss handleLoginRequest: response from login attempt: " + loginResponse);
		out.writeObject(loginResponse);
		
		// create the server-side Player obj
		Boolean playerCreated = createPlayer(loginResponse);
		log.info("wwss handleLoginRequest: create player succeeded? " + playerCreated);
		
		if(!playerCreated)
		{
			log.info("wwss handleLoginRequest: attempting to reattach previously existing player... " + loginResponse.getUserName());
			for(Player existingPlayer : Model.players)
			{
				if(existingPlayer.getUsername() == loginResponse.getUserName())
				{
					attachPlayer(existingPlayer);
				}
				
			}
		}
	}
	
	/**
	 * Handle a request to get a list of players as String usernames. 
	 * Currently supports several types of requests. For example: this player; this player's opponent; and all players.
	 * @param request
	 * @param out
	 */
	private void handleGetPlayerListRequest(GetPlayerListRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleGetPlayerListRequest: " + request.getRequestType());

    	String foundUsername = null;
    	ArrayList<String> list = new ArrayList<String>();
    	GetPlayerListResponse response = null;
    	
    	switch(request.getRequestType())
		{
			case GetPlayerListRequest.REQUEST_TYPE_THIS_PLAYER:
				if(this.player != null)
				{
					foundUsername = this.player.getUsername();
					if(foundUsername != null)
					{
						list.add(foundUsername);
					}
				}
				break;
			
			case GetPlayerListRequest.REQUEST_TYPE_OPPONENT:
				if(this.player != null)
				{
					foundUsername= this.player.getOpponent().getUsername();
					if(foundUsername != null)
					{
						list.add(foundUsername);
					}
				}
				break;

			case GetPlayerListRequest.REQUEST_TYPE_ALL_PLAYERS:
				if(Model.players != null)
				{
					for(Player player : Model.players)
					{
						list.add(player.getUsername());
					}
				}
				break;

			case GetPlayerListRequest.RESPONSE_TYPE_ALL_ACTIVE_PLAYERS:
				if(Model.players != null)
				{
					for(Player player : Model.players)
					{
						//TODO: BEHAVIOR TBD
					}
				}
				break;
				
			default:
				// do nothing; empty list
				break;
		}
    	
    	log.info("wwss handleGetPlayerListResponse: got player list: " + list.toString());
    	
		response = new GetPlayerListResponse(list);
    	log.info("wwss handleGetPlayerListResponse: post-serialized player list: " + response.getPlayersCopy());
		try
		{
			out.writeObject(response);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//TODO:FILL IN
	private void handleCreateNewAccountRequest(CreateNewAccountRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleLoginRequest: FILL IN BEHAVIOR: " + request);
    	
/*    	String responseMsg;
    	//SimpleMessage responseObj;
    	LoginResponse loginResponse;
    	
    	// attempt the login
		Boolean createNewAccountSucceeded = false;
		
		try
		{
			createNewAccountSucceeded = dataAccessObj.createNewUser(request.getUserName(), request.getPassword(), request.getEmail());
		} 
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		// send the login response
		if(createNewAccountSucceeded)
		{
			//TODO: get a user/player obj back from the createNewUser() operation
			loginResponse = new LoginResponse(1, request.getUserName(), createNewAccountSucceeded, null, false, -1);
			//responseMsg = "Login Succeeded. LoginResponse: " + responseObj;
		}
		else
		{
			responseMsg = "Login FAILED with request: " + request;
//			loginResponse = new LoginResponse(1, null, createNewAccountSucceeded, responseMsg, false, -1);
			responseObj = new SimpleMessage(responseMsg, false);
		}
		try
		{
			out.writeObject(loginResponse);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}*/
	}
	
	private void handleCreateGameRequest(CreateGameRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleLoginRequest: [PLACEHOLDER FUNCTIONALITY]" + request);
	}
	
	/**
	 * Create a server-side Player object and add it to the list in the Model.
	 * This will receive commands from the client, channeled through this thread's run() method.
	 * @param login
	 * @return
	 */
	private Boolean createPlayer(LoginResponse login)
	{
    	log.info("wwss createPlayer w/login response fields");
		try
		{
			Player player = new Player(this.connection);
			player.setUsername(login.getUserName());
			
			if(playerExists(player.getUsername()))
			{
				logMsg(" WARNING: Duplicate Player - ignoring request to add player with same username.");
				return false;
			}
			else
			{
				Model.players.add(player);
				attachPlayer(player);
				
				logMsg(" New Player Created! Num players now: " + Model.players.size());
				return true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Attach a Player obj to this thread. No more than one Player obj should be attached.
	 * @param p
	 */
	private void attachPlayer(Player p)
	{
		try
		{
			if(null == player)
			{
				player = p;
				logMsg("wwss attachPlayer: attached Player to this thread: " + player.getUsername());
			}
		}
		catch (Error e)
		{
			logMsg("wwss attachPlayer: ERROR: ignoring an attempt to attach more than one player on this thread.");
		}
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

	public void logMsg(String msg)
	{
		log.info("wwss ClientHandlerTest " + connection.getPort() + " " + msg);
	}
}// end class ClientHandlerTest
