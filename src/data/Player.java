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
	private int port;
	private Player opponent;
	private String username;
	private String state;
	private GameBoard gameBoard;
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
	
	
	/////////////////////////
	// GETTERS/SETTERS
	
	public String getState()
	{
		return state;
	}
	
	public void setState(String state)
	{
		this.state = state;
		log.info( "Player state updated: " + this.port + ", " + this.username + ", " + this.state);
	}
	
	public GameBoard getGameBoard()
	{
		return gameBoard;
	}

	public void setGameBoard(GameBoard gameBoard)
	{
		this.gameBoard = gameBoard;
	}

	public Socket getConn()
	{
		return conn;
	}

	public void setConn(Socket conn)
	{
		this.conn = conn;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public Player getOpponent()
	{
		return opponent;
	}

	public void setOpponent(Player opponent)
	{
		this.opponent = opponent;
	 	log.info( "setOpponent: opponent Player selected on port: " + this.opponent.getPort() );
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public int getScore()
	{
		return score;
	}

	public void setScore(int score)
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
		log.info("handleSelectOpponentRequest: current state, request: " + state + ", " + request);
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
		log.info("handleSelectOpponentResponse: " + response);
		
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

	//////////////////////////
	// Object overrides

	@Override
	public String toString()
	{
		return "Player [port=" + port + ", username=" + username + ", state=" + state + "]";
	}


	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((opponent == null) ? 0 : opponent.hashCode());
		result = prime * result + port;
		result = prime * result + score;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (opponent == null)
		{
			if (other.opponent != null)
				return false;
		} else if (!opponent.equals(other.opponent))
			return false;
		if (port != other.port)
			return false;
		if (score != other.score)
			return false;
		if (state == null)
		{
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (username == null)
		{
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	
}
