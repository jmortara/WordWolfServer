package data;

import java.util.ArrayList;
import java.util.HashMap;


public class Model 
{
	public static ArrayList<Player> players;
	private static HashMap<String, String> globalDictionary;
	
	public static void init()
	{
		if ( players == null )
		{
			players = new ArrayList<Player>();
		}
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
