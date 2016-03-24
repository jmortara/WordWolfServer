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
import java.util.List;
import java.util.logging.Logger;
import com.mortaramultimedia.wordwolf.shared.constants.*;
import com.mortaramultimedia.wordwolf.shared.game.Validator;
import com.mortaramultimedia.wordwolf.shared.messages.*;
import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation;

import constants.Consts;
import constants.Errors;
import data.Model;
import data.Player;
import data.PublicPlayerData;
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
    
    private Boolean threadActive = true;		// is this thread active? if false, thread should close connections and shut down
    
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
     * Send an object to the client by writing it to the output stream.
     * @param obj
     */
    private void sendObject(Object obj)
    {
    	log.info( "wwss sendObject: " + obj );
		try
		{
			out.writeObject(obj);
		} 
		catch (IOException e)
		{
			log.warning("wwss handleSelectOpponentRequest: ERROR writing object: " + obj);
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
    
    @Override
	public void run()
    {
        try
        {
            // start reading input from client
            while( this.threadActive )
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
            	/**
            	 * If receiving a PostEndGameActionRequest, if a rematch is requested, start one; if not, let client decide next action.
            	 */
            	else if(obj instanceof PostEndGameActionRequest)
            	{
            		handlePostEndGameActionRequest(((PostEndGameActionRequest) obj), out);
            	}
            	
            	out.flush();
            }
            
        }
       
        /**
         * One cause of IOException is disconnects caused by closing the app.
         */
        catch(EOFException e)
        {
        	//log.info("wwss EOFException on socket : " + e);
        	log.info("wwss Client probably disconnected. Closing input and output object streams on this thread...");

        	handleDisconnect();
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
    
    @Override
    protected void finalize()
    {
        log.warning("wwss finalize ************ thread ");

    }
	
	
	private void handleDisconnect()
	{
        log.warning("wwss handleDisconnect of player");

		if(this.player != null)
		{
			Player opponent = this.player.getOpponent();
	        // reset this player's opponent status to free the opponent up for a new match
			if(opponent != null)
			{
		        log.warning("wwss handleDisconnect: removing player's opponent: " + this.player.getOpponent().getUsername());
				opponent.removeOpponent();
				opponent.setState(PlayerState.IDLE);
				//TODO: *** new message type: notify opponent that this player has disconnected... the opponent should either finish game or start over
			}

			log.warning("wwss handleDisconnect of player: " + this.player.getUsername());
			this.player.removeOpponent();
			// set this player's state also
			setPlayerStateToDisconnected(this.player);
			
			//TODO: leave player state as disconnected for some duration? have cleanup thread remove players? set a timer in this thread?
			Model.removePlayerFromList(this.player);	//TODO - test
			nullifyPlayer(this.player);					// removing from list and nullifying should allow same player to login again without issues
			logPlayersList();
		}
		
		try 
		{
			out.close();
			in.close();
		} 
		catch (IOException e) 
		{
        	log.warning("wwss IOException while closing object streams: " + e);
			e.printStackTrace();
		}
		
    	//TODO: check that this is working correctly
		try 
		{
			connection.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		this.threadActive = false;
	}
	
	/**
	 * Shortcut to set the state of any player to disconnected. 
	 * @param p
	 */
	private void setPlayerStateToDisconnected(Player p)
	{
		if(p != null)
		{
	        log.warning("wwss setPlayerStateToDisconnected ***: " + p.getUsername());
			p.setState(PlayerState.DISCONNECTED);
		}
	}
	
	/**
	 * Set the value of a player to null, for garbage collection.
	 * @param p
	 */
	private void nullifyPlayer(Player p)
	{
		if(p != null)
		{
	        log.warning("wwss nullifyPlayer ***: " + p.getUsername());
			p = null;
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
		
		sendObject(new SimpleMessage(appendedMsg, false));
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
		
		// note that there is most likely no player created yet, but in case there is, for example on reconnect to db
		if(dbConnectionSucceeded && this.player != null)
		{
			this.player.setState(PlayerState.CONNECTED_2);
		}
		
		response = new ConnectToDatabaseResponse(dbConnectionSucceeded);
		sendObject(response);
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
		sendObject(loginResponse);
		if(loginResponse.getLoginAccepted() && this.player != null)
		{
			this.player.setState(PlayerState.CONNECTED_3);
		}

		
		// create the server-side Player obj and add it to the global list of players
		Boolean playerCreated = createPlayer(loginResponse);
		log.info("wwss handleLoginRequest: create player succeeded? " + playerCreated);
		
		if(!playerCreated)
		{
			log.info("wwss handleLoginRequest: attempting to reattach previously existing player... " + loginResponse.getUserName());
			for(Player existingPlayer : Model.getPlayers())
			{
				if(existingPlayer.getUsername() == loginResponse.getUserName())
				{
					attachPlayer(existingPlayer);
					break;
				}
			}
			if(this.player == null)
			{
				log.warning("wwss handleLoginRequest: reattaching player FAILED. This thread's player is still null.");
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
    	log.info("wwss handleGetPlayerListRequest: size of Model.players: " + Model.getPlayers().size());

    	String requestType = request.getRequestType();
    	String requestedUsername = null;
    	ArrayList<String> usernamesList = new ArrayList<String>();		// list of username Strings only
    	ArrayList<PublicPlayerData> publicPlayerDataList = new ArrayList<PublicPlayerData>();		// subset of usernamesList, in formatted type
    	GetPlayerListResponse response = null;
    	
    	//TODO: create a robot player in case no human players are available - would require some AI for gamplay
    	if(this.player != null)
    	{
    		player.setState(PlayerState.GETTING_PLAYER_LIST);
	    	switch(requestType)
			{
				case PlayerListType.THIS_PLAYER:
					requestedUsername = this.player.getUsername();
					if(requestedUsername != null)
					{
						usernamesList.add(requestedUsername);
					}
					break;
				
				case PlayerListType.OPPONENT:
					requestedUsername = this.player.getOpponent().getUsername();
					if(requestedUsername != null)
					{
						usernamesList.add(requestedUsername);
					}
					break;
					
				case PlayerListType.SPECIFIC_PLAYER_BY_USERNAME:
					if(Model.getPlayers() != null && request.getRequestedUsername() != null)
					{
						Player requestedPlayer = getPlayerByUsername(request.getRequestedUsername());
						requestedUsername = requestedPlayer.getUsername();
						if(requestedUsername != null)
						{
							usernamesList.add(requestedUsername);
						}
					}
					break;
	
				case PlayerListType.ALL_PLAYERS:
					if(Model.getPlayers() != null)
					{
						for(Player playerInList : Model.getPlayers())
						{
							if(playerInList != this.player)
							{
								usernamesList.add(playerInList.getUsername());
							}
						}
					}
					break;
	
				case PlayerListType.ALL_ACTIVE_PLAYERS:
					if(Model.getPlayers() != null)
					{
						for(Player playerInList : Model.getPlayers())
						{
							usernamesList.add(playerInList.getUsername());
						}
					}
					break;
					
				
				case PlayerListType.ALL_UNMATCHED_PLAYERS:			// THIS IS CURRENTLY THE TYPE REQUESTED BY THE CLIENT
					log.info("wwss handleGetPlayerListRequest: getting list of all unmatched players from DB...");
					if(Model.getPlayers() != null)
					{
						for(Player playerInList : Model.getPlayers())
						{
							log.info("wwss handleGetPlayerListRequest: checking player in Model.players: " + playerInList );
							//if(!playerInList.equals(this.player))	// using object comparison
							if(playerInList != this.player)			// using memory address equality
							{
								if(playerInList.getOpponent() == null)	//TODO - add a condition for PLAYER_STATE_IDLE etc
								{
									usernamesList.add(playerInList.getUsername());
								}
							}
						}
					}
					break;
					
				default:
			    	log.warning("wwss handleGetPlayerListResponse: WARNING: UNHANDLED LIST TYPE REQUESTED: " + requestType);
					// do nothing; empty list 
					break;
			}

	    	log.info("wwss handleGetPlayerListResponse: got player usernames list: " + usernamesList.toString());
	    	
	    	// convert the list of usernames to PublicPlayerDatas
	    	PublicPlayerData playerData = null;		// a single player's data
	    	for(String username : usernamesList)
	    	{
	    		try 
	    		{
					playerData = dataAccessObj.getPublicUserData(username, true);
		    		if(playerData != null)
		    		{
		    			publicPlayerDataList.add(playerData);
		    		}
				} 
	    		catch (SQLException e) 
	    		{
					e.printStackTrace();
				}
	    	}
	    	
			response = new GetPlayerListResponse(requestType, publicPlayerDataList);
	    	log.info("wwss handleGetPlayerListResponse: post-serialized player list: " + response.getPlayersCopy());
			sendObject(response);
			player.setState(PlayerState.IDLE);
    	}
    	else
    	{
        	log.warning("wwss handleGetPlayerListResponse: WARNING: PLAYER IS NULL. Not sending player list.");
    	}
	}
	
	private void handleSelectOpponentRequest(SelectOpponentRequest request, ObjectOutputStream out)
	{
		log.info("wwss ***handleSelectOpponentRequest*** : " + request);
		
		// source user initiates the request to the destination user
		String sourceUsername = request.getSourceUsername();
		Player sourcePlayer = getPlayerByUsername(sourceUsername);
		
		// ignore redundant requests
		if( sourcePlayer.getState().equals(PlayerState.REQUESTED_OPPONENT) || sourcePlayer.getState().equals(PlayerState.REQUESTED_REMATCH) )
		{
			log.info("wwss handleSelectOpponentRequest: ignoring request, this player has already initiated own request: " + sourcePlayer.getUsername());
			return;
		}
		else if(sourcePlayer.getState().equals(PlayerState.RECEIVED_OPPONENT_REQUEST))
		{
			log.info("wwss handleSelectOpponentRequest: ignoring request, this player has already received an opponent request: " + sourcePlayer.getUsername());
			return;
		}
		else if(sourcePlayer.getState().equals(PlayerState.ACCEPTED_OPPONENT))
		{
			log.info("wwss handleSelectOpponentRequest: ignoring request, this player has already accepted an opponent request: " + sourcePlayer.getUsername());
			return;
		}
		else if(sourcePlayer.getState().equals(PlayerState.READY_FOR_GAME_START))
		{
			log.info("wwss handleSelectOpponentRequest: ignoring request, this player is already ready to start an established game: " + sourcePlayer.getUsername());
			return;
		}
		
		// in the case of a rematch request, make sure both players don't request the same rematch at the same time
		if(request.getIsRematch())
		{
			sourcePlayer.setState(PlayerState.REQUESTED_REMATCH);
		}
		else 
		{
			sourcePlayer.setState(PlayerState.REQUESTED_OPPONENT);
		}
		
		try
		{
			String destinationUsername = request.getDestinationUserName();
			Player destinationPlayer = getPlayerByUsername(destinationUsername);
			
			if(destinationPlayer != null && Model.getPlayers().contains(destinationPlayer))
			{
				// if the destination player already sent out a request for a rematch, ignore the rematch request since the source player will respond to the one sent
				if(!destinationPlayer.getState().equals(PlayerState.REQUESTED_REMATCH))
				{
					destinationPlayer.setState(PlayerState.RECEIVED_OPPONENT_REQUEST);
					destinationPlayer.handleSelectOpponentRequest(request);
				}
				else
				{
					log.warning("wwss handleSelectOpponentRequest: ignoring rematch request since destination user already requested one: " + destinationUsername);
				}
			}
			else 
			{
				log.info("wwss handleSelectOpponentRequest: could not locate destination user: " + destinationUsername);
				SelectOpponentResponse playerResponse   = new SelectOpponentResponse(false, request.getSourceUsername(), request.getDestinationUserName(), request.getIsRematch(), false);
				sendObject(playerResponse);
			}
		}
		catch(Exception e)
		{
			if(e instanceof SocketException)
			{
				log.warning("wwss handleSelectOpponentRequest: SocketException. Destination player for opponent request may have disconnected. " + request);
				SelectOpponentResponse playerResponse   = new SelectOpponentResponse(false, request.getSourceUsername(), request.getDestinationUserName(), request.getIsRematch(), false);
				sendObject(playerResponse);

//				SelectOpponentResponse opponentResponse = new SelectOpponentResponse(false, request.getDestinationUserName(), request.getSourceUsername());
//				sendObject(opponentResponse);
			}
			e.printStackTrace();
		}
	}
	
	/**
	 * Handle a player's accept or decline of SelectOpponentRequest.
	 * Send out the related response to both players so that the game can begin for both players, or both should become idle.
	 * @param response
	 * @param out
	 */
	private void handleSelectOpponentResponse(SelectOpponentResponse response, ObjectOutputStream out)
	{
		log.warning("wwss handleSelectOpponentResponse: " + response);
		
		// source player is the player who accepts or rejects the request they received via this response
		//String sourceUsername = response.getSourceUserName();
		//Player sourcePlayer = getPlayerByUsername(sourceUsername);

		//String sourceUsername = null;
		Player sourcePlayer = getPlayerByUsername(response.getSourceUsername());
		//String destinationUsername = null;
		Player destinationPlayer = getPlayerByUsername(response.getDestinationUsername());
		
		// if this thread's player is the player that sent the initial request, set this player's state.
		if(this.player == sourcePlayer)
		{
			log.warning("wwss handleSelectOpponentResponse: sent by source player (this player): " + response.getSourceUsername());
			if(response.getRequestAccepted())
			{
				this.player.setState(PlayerState.ACCEPTED_OPPONENT);
				destinationPlayer.setState(PlayerState.ACCEPTED_OPPONENT);
			}
			else
			{
				this.player.setState(PlayerState.IDLE);
				this.player.removeOpponent();
				destinationPlayer.setState(PlayerState.IDLE);
				destinationPlayer.removeOpponent();
			}
			
			SelectOpponentResponse sourcePlayerResponse;
			SelectOpponentResponse destinationPlayerResponse;
			
			//mark3
			if(!response.getIsRematch())
			{
				// the source player is the one who received the original request and sent back the response passed to this method
				sourcePlayerResponse = new SelectOpponentResponse(response.getRequestAccepted(), response.getDestinationUsername(), response.getSourceUsername(), response.getIsRematch(), false);
				this.player.handleSelectOpponentResponse(sourcePlayerResponse);	// send out the response to this player's client

				// the destination player is the player who initiated the original request. they should receive the unaltered response.
				destinationPlayerResponse = response;//new SelectOpponentResponse(response.getRequestAccepted(), response.getSourceUsername(), response.getDestinationUsername(), response.getIsRematch());
				destinationPlayer.handleSelectOpponentResponse(destinationPlayerResponse);	// send out the response to the destination player's client
			}
			else
			{
				// the source player is the one who received the original request and sent back the response passed to this method
				sourcePlayerResponse = new SelectOpponentResponse(response.getRequestAccepted(), response.getDestinationUsername(), response.getSourceUsername(), response.getIsRematch(), false);
				this.player.handleSelectOpponentResponse(sourcePlayerResponse);	// send out the response to this player's client

				// the destination player is the player who initiated the original request. they should receive the unaltered response.
				destinationPlayerResponse = new SelectOpponentResponse(response.getRequestAccepted(), response.getSourceUsername(), response.getDestinationUsername(), response.getIsRematch(), true);
				destinationPlayer.handleSelectOpponentResponse(destinationPlayerResponse);	// send out the response to the destination player's client
			}
		}
		else if(this.player == destinationPlayer)
		{
			log.warning("wwss handleSelectOpponentResponse: IGNORED by destination player (this player): " + response.getDestinationUsername());
			if(response.getRequestAccepted())
			{
				//this.player.setState(PlayerState.ACCEPTED_OPPONENT);
			}
			else
			{
//				this.player.setState(PlayerState.IDLE);
//				sourcePlayer.setState(PlayerState.IDLE);
			}
//			SelectOpponentResponse destinationPlayerResponse = new SelectOpponentResponse(response.getRequestAccepted(), response.getDestinationUsername(), response.getSourceUserName());;
//			this.player.handleSelectOpponentResponse(destinationPlayerResponse);	// send out the response to the destination player's client
		}
		else
		{
			log.warning("wwss handleSelectOpponentResponse: UNHANDLED CASE, CAN'T LOCATE A PLAYER IN THE RESPONSE: " + response);
		}
		
		// compare the two player states. if both are in accepted_opponent state, match them up
		try
		{
			if(sourcePlayer.getState().equals(PlayerState.ACCEPTED_OPPONENT) && destinationPlayer.getState().equals(PlayerState.ACCEPTED_OPPONENT))
			{
				log.info("wwss handleSelectOpponentResponse: both players are ready to be matched: " + sourcePlayer.getUsername() + ", " + destinationPlayer.getUsername());
				matchPlayers(sourcePlayer, destinationPlayer);
			}
		}
		catch(NullPointerException e)
		{
			log.warning("wwss handleSelectOpponentResponse: WARNING: possible null player: " + response);
			e.printStackTrace();
		}
		
		/*
		SelectOpponentResponse sourcePlayerResponse = response;
		
		
		try
		{
			// destination player is the player who originally made the invitation to a potential opponent in the form of a SelectOpponentRequest
			destinationUsername = sourcePlayerResponse.getDestinationUsername();
			destinationPlayer = getPlayerByUsername(destinationUsername);

			
			SelectOpponentResponse destinationPlayerResponse = new SelectOpponentResponse(response.getRequestAccepted(), response.getDestinationUsername(), response.getSourceUserName());;
			if(destinationPlayer != null)
			{	//mark1
				destinationPlayer.handleSelectOpponentResponse(sourcePlayerResponse);
				sourcePlayer.handleSelectOpponentResponse(destinationPlayerResponse);
				if(response.getRequestAccepted())
				{
					sourcePlayer.setState(PlayerState.ACCEPTED_OPPONENT);
					destinationPlayer.setState(PlayerState.ACCEPTED_OPPONENT);
					matchPlayers(sourcePlayer, destinationPlayer);
				}
				else
				{
					//TODO - send out a rejection response in order to reset the client state
					sourcePlayer.setState(PlayerState.IDLE);
					destinationPlayer.setState(PlayerState.IDLE);
				}
			}
			else log.info("wwss handleSelectOpponentRequest: could not locate destination user: " + destinationUsername);
			
		}
		catch(NullPointerException e)
		{
			if(sourcePlayer == null)
			{
				log.warning("wwss handleSelectOpponentRequest: WARNING: NULL SOURCE PLAYER: " + sourceUsername);
			}
			else if(destinationPlayer == null)
			{
				log.warning("wwss handleSelectOpponentRequest: WARNING: NULL DESTINATION PLAYER: " + destinationUsername);
			}
			else
			{
				//mark2
				log.warning("wwss handleSelectOpponentRequest: WARNING: UNKNOWN CAUSE OF NULL POINTER EXCEPTION: sourcePlyaer: " + sourcePlayer);
				log.warning("wwss handleSelectOpponentRequest: WARNING: UNKNOWN CAUSE OF NULL POINTER EXCEPTION: destinationPlayer: " + destinationPlayer);
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}*/
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
    	log.info("wwss handleCreateNewAccountRequest: " + request);
    	
    	String responseMsg = null;
    	//SimpleMessage responseObj;
    	CreateNewAccountResponse createNewAccountResponse = null;
    	
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
		
		if(createNewAccountSucceeded)
		{
	    	log.info("wwss handleCreateNewAccountRequest: new account creation succeeded.");
			createNewAccountResponse = new CreateNewAccountResponse(true,  request.getUserName(), request.getPassword(), request.getEmail(), null);
		}
		else
		{
	    	log.info("wwss handleCreateNewAccountRequest: new account creation FAILED.");
			createNewAccountResponse = new CreateNewAccountResponse(false,  request.getUserName(), request.getPassword(), request.getEmail(), ("ERROR: NEW ACCOUNT CREATION FAILED: " + createNewAccountResponse));
		}
		
		try 
		{
			out.writeObject(createNewAccountResponse);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		// send the login response
		/*if(createNewAccountSucceeded)
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
		
		// ignore redundant requests
		if(player.getState().equals(PlayerState.PLAYING_GAME))
		{
			log.info("wwss handleCreateGameRequest: ignoring request, this player is already playing a game: " + player.getUsername());
			return;
		}
		/*else if(sourcePlayer.getState().equals(PlayerState.RECEIVED_OPPONENT_REQUEST))
		{
			log.info("wwss handleSelectOpponentRequest: ignoring request, this player has already received an opponent request: " + sourcePlayer.getUsername());
			return;
		}
		else if(sourcePlayer.getState().equals(PlayerState.ACCEPTED_OPPONENT))
		{
			log.info("wwss handleSelectOpponentRequest: ignoring request, this player has already accepted an opponent request: " + sourcePlayer.getUsername());
			return;
		}*/
		else if(player.getGameBoard() != null)
		{
			log.info("wwss handleCreateGameRequest: ignoring request, this player has already has a GameBoard: " + player.getUsername());
			player.getOpponent().setGameBoard(player.getGameBoard());	// make sure both GameBoards are the same
			return;
		}
		else if(player.getOpponent() != null && player.getOpponent().getGameBoard() != null)
		{
			log.info("wwss handleCreateGameRequest: ignoring request, this player's opponent has already has a GameBoard: " + player.getUsername());
			player.getOpponent().setGameBoard(player.getGameBoard());	// make sure both GameBoards are the same
			return;
		}
		
		setupGame(request.getBoardRows(), request.getBoardCols());
	}
	
	private void handleGameMoveRequest(GameMoveRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleGameMoveRequest: " + request);
		String wordSubmitted = Validator.getWordFromGameMove(request.getGameMove());
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
        			player.addToScore(pointsAwarded);
        			response = new GameMoveResponse(player.getUsername(), -1, true, true, wordSubmitted, pointsAwarded, player.getScore(), null);
        		}
    		}
    		else
    		{
    			log.warning("wwss handleGameMoveRequest: move has not passed validation. Not scoring it.");
    			response = new GameMoveResponse(player.getUsername(), -1, true, false, wordSubmitted, 0, player.getScore(), Errors.GAME_MOVE_INVALID);
    		}
    	}
    	else
    	{
    		log.warning("wwss handleGameMoveRequest: no gameBoard exists, so cannot process move. Ignoring.");
			response = new GameMoveResponse(player.getUsername(), -1, false, false, wordSubmitted, 0, player.getScore(), Errors.GAME_BOARD_IS_NULL);
    	}
    	
    	sendObject(response);
	}
	
	private void handleEndGameRequest(EndGameRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handleEndGameRequest: " + request);
    	
    	//TODO: wrap up on the server side before sending the confirmation response. make sure both requests have come in before sending a unified response.
    	
    	Player opponent = player.getOpponent();
    	
    	log.info("wwss handleEndGameRequest: sending EndGameResponse to player: " + player.getUsername());
    	if(!player.getState().equals(PlayerState.GAME_ENDED))
    	{
	    	player.setState(PlayerState.GAME_ENDED);
	    	player.setGameBoard(null);
	    	
	    	// get the opponent's score... if they are still a valid player/connection
	    	int opponentScore = 0;
	    	if(opponent != null)
	    	{
	    		opponentScore = opponent.getScore();
	    	}
	    	else
	    	{
	        	log.warning("wwss handleEndGameRequest: could not get score from opponent, possible opponent disconnected: " + opponent);
	    	}
	    	
	    	// send the EndGameResponse to this thread's client
	    	EndGameResponse playerResponse = new EndGameResponse(player.getUsername(), -1, true, player.getScore(), opponentScore, null);
			player.handleEndGameResponse(playerResponse);
			
			// update the scores in the db
			try 
			{
				dataAccessObj.updateHighScore( player.getUsername(), player.getScore());
				dataAccessObj.updateTotalScore(player.getUsername(), player.getScore());
			} 
			catch (SQLException e) 
			{
	        	log.warning("wwss handleEndGameRequest: SQL EXCEPTION: could not get score from opponent, possible opponent disconnected: " + opponent);
				e.printStackTrace();
			}
    	}
    	else
		{
    		log.warning("wwss handleEndGameRequest: player is already in game ended state, ignoring: " + player.getUsername());
		}

    	
    	//TODO: note that the opponent response here is not generated from a request from that opponent
    	//log.info("wwss handleEndGameRequest: sending EndGameResponse to opponent: " + player.getOpponent().getUsername());
    	/*if(opponent!= null)
    	{
    		opponent.setState(PlayerState.GAME_ENDED);
    		opponent.setGameBoard(null);
        	EndGameResponse opponentResponse = new EndGameResponse(player.getOpponent().getUsername(), -1, true, player.getOpponent().getScore(), player.getScore(), null);
        	player.getOpponent().handleEndGameResponse(opponentResponse);
    	}*/
	}
	
	private void handlePostEndGameActionRequest(PostEndGameActionRequest request, ObjectOutputStream out)
	{
    	log.info("wwss handlePostEndGameActionRequest: " + request);
    	
    	Player opponent = player.getOpponent();
		
    	player.resetScore();
    	if(opponent != null)
    	{
        	opponent.resetScore();
    	}
    	
    	// if this thread's player requested a rematch, reformat the request into a Select Opponent Request and send it to their opponent as usual
	    if(request.getRematch())
	    {
	    	SelectOpponentRequest selectOpponentRequest = new SelectOpponentRequest(request.getUserName(), request.getOpponentUserName(), true);
	    	handleSelectOpponentRequest(selectOpponentRequest, out);
			//setupGame(request.getBoardRows(), request.getBoardCols());
	    	//CreateGameResponse createGameResponse = new CreateGameResponse(request.getUserID(),  request.getUserName(),  "game_type_rematch",  request.getBoardRows(),  request.getBoardCols(),  -1,  request.getOpponentUserID(),  request.getOpponentUserName(),  0,  0,  null,  null,  gameBoard,  Consts.DEFAULT_GAME_DURATION_MS,  null);
	    }
	    
	    // if no rematch was requested, notify this player's opponent so they can look for a new opponent
	    else
	    {
	    	// set both players free of opponents
	    	if(opponent != null)
	    	{
	    		opponent.setState(PlayerState.IDLE);
	    		opponent.removeOpponent();

	    		// and notify the player's opponent of the news
		    	PostEndGameActionResponse rematchDeclinedResponse = new PostEndGameActionResponse(false, request.getUserName(), request.getOpponentUserName());
		    	opponent.handlePostEndGameActionResponse(rematchDeclinedResponse);
	    	}
	    	player.removeOpponent();
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
			
			if(playerExists(login.getUserName()))
			{
				// note - this results in handleLoginRequest reattaching the existing player
				logMsg(" WARNING: Duplicate Player - ignoring request to add player with same username.");
				return false;
			}
			else
			{
				Model.addPlayerToList(player);
				attachPlayer(player);
				
				logMsg(" New Player Created! Num players now: " + Model.getPlayers().size());
				logPlayersList();
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
			if(null == this.player)
			{
				setPlayer(p);
				player.setState(PlayerState.CONNECTED_1);
				logMsg("wwss attachPlayer: attached Player to this thread (existing player was null): " + player.getUsername());
			}
			else
			{
				logMsg("wwss attachPlayer: attached Player to this thread (existing player was "+this.player.getUsername()+"): " + player.getUsername());
				setPlayer(p);
				player.setState(PlayerState.CONNECTED_1);
			}
		}
		catch (Error e)
		{
			logMsg("wwss attachPlayer: ERROR: ignoring an attempt to attach more than one player on this thread.");
		}
	}
	
	private synchronized void setPlayer(Player p)
	{
		this.player = p;
	}
	
	private Boolean playerExists( String username ) 
	{
		logMsg( "playerExists? " + username );

		if ( Model.getPlayers() == null ) 
		{
			logMsg( "playerExists: players list is null." );
			return false;
		}
		
		Boolean playerFound = false;
		for ( Player player : Model.getPlayers() )
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

		if ( Model.getPlayers() == null ) 
		{
			logMsg( "getPlayerByUsername: players list is null." );
			return null;
		}
		
		Player existingPlayer = null;
		
		for ( Player player : Model.getPlayers() )
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
		log.info("wwss ***matchPlayers*** : " + player1.getUsername() + ", " + player2.getUsername());
		
		// ignore redundant requests
		if(player1.getOpponent() == player2 && player2.getOpponent() == player1 && player1.getState().equals(PlayerState.READY_FOR_GAME_START) && player2.getState().equals(PlayerState.READY_FOR_GAME_START))
		{
			log.warning("wwss matchPlayers: ignoring, these players have already been matched: " + player1.getUsername() + ", " + player2.getUsername());
			return true;
		}
		
		// make the players opponents of each other and update their states (THIS IS THE ONLY PLACE WHERE setOpponent() IS CALLED)
		try
		{
			log.info("wwss matchPlayers: matching these two players as opponents: " + player1.getUsername() + ", " + player2.getUsername());
			player1.setOpponent(player2);
			player1.setState(PlayerState.READY_FOR_GAME_START);
			player2.setOpponent(player1);
			player2.setState(PlayerState.READY_FOR_GAME_START);
			log.info("wwss matchPlayers: match successful.");
			//SimpleMessage player1ConfirmationMsg = new SimpleMessage("You are confirmed to have an opponent: " + player2.getUsername());
			//SimpleMessage player2ConfirmationMsg = new SimpleMessage("You are confirmed to have an opponent: " + player1.getUsername());
			//player1.handleSimpleMessage(player1ConfirmationMsg);
			//player2.handleSimpleMessage(player2ConfirmationMsg);
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
		GameBoard gameBoard = gameBoardBuilder.getNewGameBoard(-1, requestedRows, requestedCols, Consts.DEFAULT_CHARACTER_SET);	//TODO: make charset dynamic?
		player.setGameBoard(gameBoard);
		player.getOpponent().setGameBoard(gameBoard);
		
		log.info("wwss ClientHandler: setupGame: gameBoard created:");
		player.getGameBoard().printBoardData();

		// TODO = this var is now unused, remove
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
				Consts.DEFAULT_GAME_DURATION_MS, 
				null
			);
		// set some references for clarity when passing as args to each of the two responses (player and opponent)
		Player opponent = player.getOpponent();
		int playerUserID   = 1;
		int opponentUserID = 2;
		String playerUsername   = player.getUsername();
		String opponentUsername = opponent.getUsername();
		String gameType = "defaultGameType";
		int boardRows = player.getGameBoard().getRows();
		int boardCols = player.getGameBoard().getCols();
		int gameID = 1;
		int startingPlayerScore   = 0;
		int startingOpponentScore = 0;
		List<TileData> existingPlayerMoves   = null;
		List<TileData> existingOpponentMoves = null;
		final long GAME_DURATION_MS = Consts.DEFAULT_GAME_DURATION_MS;
		String errMsg = null;
		
		// create and send the Create Game Response for the player on this thread
		CreateGameResponse responseForPlayer = new CreateGameResponse(playerUserID, playerUsername, gameType, boardRows, boardCols, gameID, opponentUserID, opponentUsername, startingPlayerScore, startingOpponentScore, existingPlayerMoves, existingOpponentMoves, gameBoard, GAME_DURATION_MS, errMsg);
		player.setState(PlayerState.PLAYING_GAME);
		player.handleCreateGameResponse(responseForPlayer);

		// create and send the Create Game Response for the opponent of the player on this thread
		CreateGameResponse responseForOpponent = new CreateGameResponse(opponentUserID, opponentUsername, gameType, boardRows, boardCols, gameID, playerUserID, playerUsername, startingOpponentScore, startingPlayerScore, existingOpponentMoves, existingPlayerMoves, gameBoard, GAME_DURATION_MS, errMsg);
		player.getOpponent().setState(PlayerState.PLAYING_GAME);
		player.getOpponent().handleCreateGameResponse(responseForOpponent);
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
	
	/**
	 * Print a list of current players in the global players list.
	 */
	public void logPlayersList()
	{
		String playerNamesStr = ""; 
		if(Model.getPlayers() != null)
		{
			for(Player playerInList : Model.getPlayers())
			{
				playerNamesStr += (playerInList.getUsername() + " ");
			}
		}
		log.info("wwss ClientHandler: logPlayersList: current players list: " + playerNamesStr);
	}
	
	public void logMsg(String msg)
	{
		log.info("wwss ClientHandler " + connection.getPort() + " " + msg);
	}
}// end class ClientHandler
