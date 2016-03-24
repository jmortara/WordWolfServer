package data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import com.mortaramultimedia.wordwolf.shared.constants.*;
import com.mortaramultimedia.wordwolf.shared.messages.*;

import core.WWSocketServer;


/**
 * The server-side Player object.
 * @author jason
 *
 */
public class Player 
{
	private static Logger log;
	private Socket conn;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private int port = -1;
	private Player opponent = null;
	private String username = null;
	private String state = null;
	private GameBoard gameBoard = null;
	private int score = 0;
	

	/**
	 * Constructor
	 * @param conn
	 */
	public Player(Socket conn, ObjectInputStream in, ObjectOutputStream out) 
	{
		this.conn = conn;
		this.in   = in;
		this.out  = out;
		this.port = this.conn.getPort();
		
		//log = Logger.getLogger( "Player " + this.port );
		log = WWSocketServer.log;
		log.info( "Player constructor on port: " + this.port );
		
		setState(PlayerState.INIT);
	}

	
	/////////////////////////
	// SCORING
	
	public void addToScore(int points)
	{
		setScore( getScore() + points );
	}
	
	public void resetScore()
	{
		setScore(0);
	}
	
	public void removeOpponent()
	{
		log.info( "removeOpponent");
		setOpponent(null);
	}
	
	/////////////////////////
	// GETTERS/SETTERS
	
	public synchronized String getState()
	{
		return state;
	}
	
	public synchronized void setState(String state)
	{
		this.state = state;
		log.info( "Player state updated: " + this.port + ", " + this.username + ", " + this.state);
	}
	
	public synchronized GameBoard getGameBoard()
	{
		return gameBoard;
	}

	public synchronized void setGameBoard(GameBoard gameBoard)
	{
		if(this.gameBoard != null && gameBoard != null)
		{
			log.warning( "WARNING: Player already has a GameBoard set: " + this.username + ", " + this.state);
		}
		this.gameBoard = gameBoard;
	}

	public synchronized Socket getConn()
	{
		return conn;
	}

	public synchronized void setConn(Socket conn)
	{
		this.conn = conn;
	}

	public synchronized int getPort()
	{
		return port;
	}

	public synchronized void setPort(int port)
	{
		this.port = port;
	}

	public synchronized Player getOpponent()
	{
		return opponent;
	}

	public synchronized void setOpponent(Player opponent)
	{
		this.opponent = opponent;
		//log.info( "setOpponent: opponent Player selected on port: " + this.opponent.getPort() );
	 	log.info( "setOpponent: " + getUsername() + "'s opponent Player set to: " + getOpponentUsername() );
	}

	private synchronized String getOpponentUsername()
	{
		if(this.getOpponent() == null)
		{
			return "null_opponent";
		}
		else return this.getOpponent().getUsername();
	}
	
	public synchronized String getUsername()
	{
		return username;
	}

	public synchronized void setUsername(String username)
	{
		this.username = username;
	}
	
	public synchronized int getScore()
	{
		return score;
	}

	public synchronized void setScore(int score)
	{
		this.score = score;
	}


	/////////////////////////////////////////
	// I/O to/from streams
	public void handleSimpleMessage(SimpleMessage msg)
	{
		log.info("handleSimpleMessage: " + msg);
		try
		{
			out.writeObject(msg);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void handleMessageFromOpponent(OpponentBoundMessage msgObj)
	{
		log.info("handleMessageFromOpponent: " + msgObj);
		try
		{
			out.writeObject(msgObj);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void handleSelectOpponentRequest(SelectOpponentRequest request)
	{
		log.warning(this.username + ": handleSelectOpponentRequest: current state, request: " + state + ", " + request);
		try
		{
			out.writeObject(request);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void handleSelectOpponentResponse(SelectOpponentResponse response)
	{
		log.warning(this.username + ": handleSelectOpponentResponse: " + response);
		
		if(response.getRequestAccepted() == true)
		{
			//TODO: MAKE PLAYERS OPPONENTS OF EACH OTHER *******************************************
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
	
	public void handleCreateGameResponse(CreateGameResponse response)
	{
		log.info("handleCreateGameResponse: " + response);
		
		try
		{
			out.writeObject(response);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void handleEndGameResponse(EndGameResponse response)
	{
		log.info("handleEndGameResponse: " + response);
		
		try
		{
			out.writeObject(response);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void handlePostEndGameActionResponse(PostEndGameActionResponse response)
	{
		log.info("handlePostEndGameActionResponse: " + response);
		
		try
		{
			out.writeObject(response);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	//////////////////////////
	// Object overrides

	@Override
	public String toString()
	{
		return "Player [port=" + port + ", username=" + username + ", state=" + state + ", opponent=" + getOpponentUsername() + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conn == null) ? 0 : conn.hashCode());
		result = prime * result + ((gameBoard == null) ? 0 : gameBoard.hashCode());
		result = prime * result + ((in == null) ? 0 : in.hashCode());
		result = prime * result + ((opponent == null) ? 0 : opponent.hashCode());
		result = prime * result + ((out == null) ? 0 : out.hashCode());
		result = prime * result + port;
		result = prime * result + score;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (conn == null) {
			if (other.conn != null)
				return false;
		} else if (!conn.equals(other.conn))
			return false;
		if (gameBoard == null) {
			if (other.gameBoard != null)
				return false;
		} else if (!gameBoard.equals(other.gameBoard))
			return false;
		if (in == null) {
			if (other.in != null)
				return false;
		} else if (!in.equals(other.in))
			return false;
		if (opponent == null) {
			if (other.opponent != null)
				return false;
		} else if (!opponent.equals(other.opponent))
			return false;
		if (out == null) {
			if (other.out != null)
				return false;
		} else if (!out.equals(other.out))
			return false;
		if (port != other.port)
			return false;
		if (score != other.score)
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	
}
