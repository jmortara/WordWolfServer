import java.util.ArrayList;


public class Model 
{
	// constants
	public static final String VERSION = "0.0.6";
	public static final int PORT = 4001;
	public static final int MAX_BACKLOG_CONNECTIONS = 10;
	
	public static ArrayList<Player> players;
	
	public static void init()
	{
		if ( players == null )
		{
			players = new ArrayList<Player>();
		}
	}
	
}
