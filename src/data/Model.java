package data;

import java.util.ArrayList;
import java.util.HashMap;


public class Model 
{
	private static ArrayList<Player> players;
	private static HashMap<String, String> globalDictionary;
	
	
	public static void init()
	{
		if ( players == null )
		{
			players = new ArrayList<Player>();
		}
	}

	public static synchronized void addPlayerToList(Player player)
	{
		if(player != null)
		{
			players.add(player);
			System.out.println("Player added to global list: " + player.getUsername());
		}
	}
	
	public static synchronized void removePlayerFromList(Player player)
	{
		if(player != null)
		{
			if(players.contains(player))
			{
				players.remove(player);
				System.out.println("Player removed from global list: " + player.getUsername());
			}
		}
	}

	
	//////////////////////
	// GETTERS / SETTERS

	public static synchronized ArrayList<Player> getPlayers() 
	{
		return players;
	}

	public static HashMap<String, String> getGlobalDictionary()
	{
		return globalDictionary;
	}

	public static void setGlobalDictionary(HashMap<String, String> globalDictionary)
	{
		Model.globalDictionary = globalDictionary;
	}
	
}
