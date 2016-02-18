package data;

import java.util.ArrayList;


public class Model 
{
	// constants
	
	public static ArrayList<Player> players;
	
	public static void init()
	{
		if ( players == null )
		{
			players = new ArrayList<Player>();
		}
	}
	
}
