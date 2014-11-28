import java.net.Socket;
import java.util.logging.Logger;


public class Player 
{

	private static Logger log;
	private Socket conn;
	private int port;
	private Player opponent;
	
	public Player getOpponent() {
		return opponent;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Socket getConn() {
		return conn;
	}

	
	public Player(Socket conn) 
	{
		this.conn = conn;
		this.port = this.conn.getPort();
		
		log = Logger.getLogger( "Player " + this.port );
		log.info( "Player constructor on port: " + this.port );
	}
	
	public void setOpponent( Player opponent )
	{
		this.opponent = opponent;
		log.info( "setOpponent: opponent Player selected on port: " + this.opponent.getPort() );
	}

}
