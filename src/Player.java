import java.net.Socket;
import java.util.logging.Logger;


public class Player 
{

	private static Logger log;
	private Socket conn;
	private int port;
	
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

}
