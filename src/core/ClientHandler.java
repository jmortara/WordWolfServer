package core;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.mortaramultimedia.wordwolf.shared.constants.*;
import com.mortaramultimedia.wordwolf.shared.game.Validator;
import com.mortaramultimedia.wordwolf.shared.messages.*;
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation;

import constants.Errors;
import data.Model;
import data.Player;
import database.*;
import game.GameBoardBuilder;
import test.TestMySQLAccess;


/**
 * Client Handler Thread
 * @author jason mortara
 */
class ClientHandler extends Thread
{
	private Logger log;							// reference to WWSocketServer's Log
    private Socket connection;					// passed from the main WWSocketServer class in this class' constructor
    private ObjectInputStream in;				// the Object Input Stream  associated with this thread's socket connection
    private ObjectOutputStream out;				// the Object Output Stream associated with this thread's socket connection
    private MySQLAccess dataAccessObj;			// the JDBC access object (DAO) instance for this thread
    private Player player;						// the dedicated Player object on this Thread. Receives commands via run().
    private GameBoardBuilder gameBoardBuilder;	// the GameBoardBuilder for this thread
    //private GameBoard gameBoard;				// the data for the game's board-- its rows, cols, and letters (moved to Player)
    
    
    /**
     * ClientHandler constructor
     * @param connection 
     */
    ClientHandler(Socket connection)
    {
    	log = WWSocketServer.log;
    	log.info( "wwss ClientHandler constructor." );
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
    	TestMySQLAccess.testConnection();
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
            	 * If receiving a SelectOpponentRequest, which is a request from one player to another to become opponents.
            	 */
            	else if(obj instanceof SelectOpponentRequest)
            	{
            		handleSelectOpponentRequest(((SelectOpponentRequest) obj), out);
            	}
    			/**
    			 * If receiving a SelectOpponentResponse.
    			 */
    			else if(obj instanceof SelectOpponentResponse)
    			{
    				handleSelectOpponentResponse(((SelectOpponentResponse) obj), out);
    			}
    			/**
    			 * If receiving an OpponentBoundMessage, which is a message from a client to that client's opponent.
    			 */
    			else if(obj instanceof OpponentBoundMessage)
    			{
    				handleOpponentBoundMessage(((OpponentBoundMessage) obj), out);
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
            	/**
            	 * If receiving a GameMoveRequest, check its GameMove for validity, process the move, and respond with points awarded.
            	 */
            	else if(obj instanceof GameMoveRequest)
            	{
            		handleGameMoveRequest(((GameMoveRequest) obj), out);
            	}
            	/**
            	 * If receiving a EndGameRequest, complete any game-over wrapup on the server side, and confirm with the client.
            	 */
            	else if(obj instanceof EndGameRequest)
            	{
            		handleEndGameRequest(((EndGameRequest) obj), out);
            	}
            	
            	out.flush();
            }
            
        }
       
        /**
         * One cause of IOException is disconnects caused by closing the app.
         */
        catch(EOFException e)
        {
        	//TODO: also close client socket in addition to in/out streams
        	log.info("wwss EOFException on socket : " + e);
        	log.info("wwss Client may have disconnected. Closing input and output object streams on this thread...");
        	try
        	{
        		out.close();
        		in.close();
        	}
        	catch(IOException e1)
        	{
            	log.warning("wwss IOException while closing object streams: " + e1);
        		e1.printStackTrace();
        	}
        }
        catch (IOException e)
        {
        	log.warning("wwss IOException on socket : " + e);
        	e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            log.warning("wwss ClassNotFoundException on socket : " + e);
            e.printStackTrace();
        } 
        catch (SQLException e)
		{
            log.warning("wwss SQLException on socket : " + e);
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
    	
//    	String responseMsg;
    	ConnectToDatabaseResponse response;
    	
    	// attempt to connect to the database
//    	Boolean dbConnectionSucceeded = MySQLAccessTester.testConnection();
    	Boolean dbConnectionSucceeded = false;
		try
		{
			dbConnectionSucceeded = dataAccessObj.connectToDataBase();
		} 
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

		// send the connection test response
		/*if(dbConnectionSucceeded)
		{
			responseMsg = "Database Connection Succeeded.";
		}
		else
		{
			responseMsg = "Database Connection FAILED.";
		}*/
		response = new ConnectToDatabaseResponse(dbConnectionSucceeded);
		try
		{
			out.writeObject(response);
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
	
	private void handleSelectOpponentRequest(SelectOpponentRequest request, ObjectOutputStream out)
	{
		log.info("wwss handleSelectOpponentRequest: " + request);
		
		// source user initiates the request to the destination user
		String sourceUsername = request.getSourceUsername();
		Player sourcePlayer = getPlayerByUsername(sourceUsername);
		sourcePlayer.setState(PlayerState.REQUESTED_OPPONENT);
		
		try
		{
			String destinationUsername = request.getDestinationUserName();
			Player destinationPlayer = getPlayerByUsername(destinationUsername);
			if(destinationPlayer != null)
			{
				destinationPlayer.setState(PlayerState.RECEIVED_OPPONENT_REQUEST);
				destinationPlayer.handleSelectOpponentRequest(request);
			}
			else log.info("wwss handleSelectOpponentRequest: could not locate destination user: " + destinationUsername);
		}
		catch(Exception e)
		{
			if(e instanceof SocketException)
			{
				log.warning("wwss handleSelectOpponentRequest: SocketException. Destination player for opponent request may have disconnected. " + request);
				SelectOpponentResponse response = new SelectOpponentResponse(false, request.getSourceUsername(), request.getDestinationUserName());
				try
				{
					out.writeObject(response);
				} 
				catch (IOException e1)
				{
					log.warning("wwss handleSelectOpponentRequest: error writing response object: " + response);
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}
	
	private void handleSelectOpponentResponse(SelectOpponentResponse response, ObjectOutputStream out)
	{
		log.info("wwss handleSelectOpponentResponse: " + response);
		
		// source player accepts or rejects the request in this response
		String sourceUsername = response.getSourceUserName();
		Player sourcePlayer = getPlayerByUsername(sourceUsername);
		
		try
		{
			String destinationUsername = response.getDestinationUsername();
			Player destinationPlayer = getPlayerByUsername(destinationUsername);
			if(destinationPlayer != null)
			{
				destinationPlayer.handleSelectOpponentResponse(response);
				if(response.getRequestAccepted())
				{
					sourcePlayer.setState(PlayerState.ACCEPTED_OPPONENT);
					matchPlayers(sourcePlayer, destinationPlayer);
				}
				else
				{
					sourcePlayer.setState(PlayerState.IDLE);
					destinationPlayer.setState(PlayerState.IDLE);
				}
			}
			else log.info("wwss handleSelectOpponentRequest: could not locate destination user: " + destinationUsername);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void handleOpponentBoundMessage(OpponentBoundMessage request, ObjectOutputStream out)
	{
    	log.info("wwss handleOpponentBoundMessage: " + request);
    	
    	Player opponent = player.getOpponent();
    	opponent.handleMessageFromOpponent(request);
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
		log.info("wwss handleCreateGameRequest: " + request);
		setupGame(request.getBoardRows(), request.getBoardCols());
	}
	
	private void handleGameMoveRequest(GameMoveRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleGameMoveRequest: " + request);
    	int pointsAwarded = 0;
    	GameMoveResponse response = null;
    	if(player.getGameBoard() != null)
    	{
    		Boolean moveIsValid = Validator.validateMove(player.getGameBoard(), request.getGameMove());
    		if(moveIsValid)
    		{
    			pointsAwarded = calculateScoreFromMove(request.getGameMove());
        		if(player != null)
        		{
        			player.setScore(player.getPort() + pointsAwarded);
        			response = new GameMoveResponse(player.getUsername(), -1, true, true, pointsAwarded, null);
        		}
    		}
    		else
    		{
    			log.warning("wwss handleGameMoveRequest: move has not passed validation. Not calculating a score for it.");
    			response = new GameMoveResponse(player.getUsername(), -1, true, false, 0, Errors.GAME_MOVE_INVALID);
    		}
    	}
    	else
    	{
    		log.warning("wwss handleGameMoveRequest: no gameBoard exists, so cannot process move. Ignoring.");
			response = new GameMoveResponse(player.getUsername(), -1, false, false, 0, Errors.GAME_BOARD_IS_NULL);
    	}
    	
    	try
		{
			out.writeObject(response);
		} 
    	catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void handleEndGameRequest(EndGameRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleEndGameRequest: " + request);
    	
    	//TODO: wrap up on the server side before sending the confirmation response
    	
    	log.info("wwss handleEndGameRequest: sending EndGameResponse to player: " + player.getUsername());
    	player.setState(PlayerState.GAME_ENDED);
    	EndGameResponse playerResponse = new EndGameResponse(player.getUsername(), -1, true, player.getScore(), null);
    	try
		{
			out.writeObject(playerResponse);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    	
    	//TODO: note that the opponent response here is not generated from a request from that opponent
    	log.info("wwss handleEndGameRequest: sending EndGameResponse to opponent: " + player.getOpponent().getUsername());
    	player.getOpponent().setState(PlayerState.GAME_ENDED);
    	EndGameResponse opponentResponse = new EndGameResponse(player.getOpponent().getUsername(), -1, true, player.getOpponent().getScore(), null);
    	try
		{
			out.writeObject(opponentResponse);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
			Player player = new Player(this.connection, this.in, this.out);
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
				player.setState(PlayerState.IDLE);
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
		logMsg( "getPlayerByUsername? " + username );

		if ( Model.players == null ) 
		{
			logMsg( "getPlayerByUsername: players list is null." );
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

	private Boolean matchPlayers(Player player1, Player player2)
	{
		try
		{
			log.info("wwss matchPlayers: matching these two players as opponents: " + player1.getUsername() + ", " + player2.getUsername());
			player1.setOpponent(player2);
			player1.setState(PlayerState.READY_FOR_GAME_START);
			player2.setOpponent(player1);
			player2.setState(PlayerState.READY_FOR_GAME_START);
			log.info("wwss matchPlayers: match successful.");
			SimpleMessage player1ConfirmationMsg = new SimpleMessage("You are confirmed to have an opponent: " + player2.getUsername());
			SimpleMessage player2ConfirmationMsg = new SimpleMessage("You are confirmed to have an opponent: " + player1.getUsername());
			player1.handleSimpleMessage(player1ConfirmationMsg);
			player2.handleSimpleMessage(player2ConfirmationMsg);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Set up a new GameBoard and assign it to player/opponent on the server side, and forward it to the each player's client in that game.
	 * @param requestedRows
	 * @param requestedCols
	 */
	private void setupGame(int requestedRows, int requestedCols)
	{
		log.info("wwss ClientHandler: setupGame");
		if(player == null || player.getOpponent() == null)
		{
			log.warning("wwss ClientHandler: setupGame: player or opponent is null... aborting setupGame");
			return;
		}
		
		log.info("wwss ClientHandler: setupGame between " + player.getUsername() + " and " + player.getOpponent().getUsername());
		gameBoardBuilder = new GameBoardBuilder();
		GameBoard gameBoard = gameBoardBuilder.getNewGameBoard(-1, requestedRows, requestedCols, GameBoardBuilder.CHARACTER_SET_A);	//TODO: make charset dynamic?
		player.setGameBoard(gameBoard);
		player.getOpponent().setGameBoard(gameBoard);
		
		log.info("wwss ClientHandler: setupGame: gameBoard created:");
		player.getGameBoard().printBoardData();

		CreateGameResponse response = new CreateGameResponse
			(
				1, 
				player.getUsername(), 
				"defaultGameType", 
				player.getGameBoard().getRows(), 
				player.getGameBoard().getCols(), 
				1, 
				1,
				player.getOpponent().getUsername(), 
				0, 
				0, 
				null, null, 
				player.getGameBoard(), 
				30000, 
				null
			);
		
		player.setState(PlayerState.PLAYING_GAME);
		player.handleCreateGameResponse(response);

		player.getOpponent().setState(PlayerState.PLAYING_GAME);
		player.getOpponent().handleCreateGameResponse(response);
	}
	
	/**
	 * Calculate a score for the move. For now, keep it simple: score = number of letters (tiles).
	 * @param gameMove
	 * @return
	 */
	private int calculateScoreFromMove(GameMove gameMove)
	{
		int moveScore = 0;
		int numTiles = 0;
		if(gameMove.getMove().size() > 0)
		{
			numTiles = gameMove.getMove().size();
		}
		moveScore = numTiles;
		return moveScore;
	}
	
	public void logMsg(String msg)
	{
		log.info("wwss ClientHandler " + connection.getPort() + " " + msg);
	}
}// end class ClientHandler
