import java.net.Socket;
import java.util.logging.Logger;

import com.mortaramultimedia.wordwolf.shared.constants.Constants;


/**
 * The server-side Player object.
 * @author jason
 *
 */
public class Player 
{
	private static Logger log;
	private Socket conn;
	private int port;
	private Player opponent;
	private String username;
	private String state;
	

	/**
	 * Constructor
	 * @param conn
	 */
	public Player(Socket conn) 
	{
		this.conn = conn;
		this.port = this.conn.getPort();
		
		log = Logger.getLogger( "Player " + this.port );
		log.info( "Player constructor on port: " + this.port );
		
		setState(Constants.PLAYER_STATE_NEW);
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


	@Override
	public String toString()
	{
		return "Player [port=" + port + ", username=" + username + ", state=" + state + "]";
	}
	
}
