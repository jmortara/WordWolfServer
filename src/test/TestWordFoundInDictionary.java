package test;

import java.util.ArrayList;
import java.util.List;

import com.mortaramultimedia.wordwolf.shared.game.*;
import com.mortaramultimedia.wordwolf.shared.messages.*;

public class TestWordFoundInDictionary
{

	public static void main(String[] args)
	{
		System.out.println("TestWordFoundInDictionary: main");
		
		char a = new String("a").charAt(0);
		char d = new String("d").charAt(0);
		char e = new String("e").charAt(0);
		char i = new String("i").charAt(0);
		char t = new String("t").charAt(0);
		char o = new String("o").charAt(0);
		char n = new String("n").charAt(0);
		
		TileData td0 = new TileData(0, 0, a, false);
		TileData td1 = new TileData(0, 1, d, false);
		TileData td2 = new TileData(0, 2, d, false);
		TileData td3 = new TileData(0, 3, e, false);
		TileData td4 = new TileData(0, 4, d, false);
		
		ArrayList<TileData> move = new ArrayList<TileData>();
		move.add(td0);
		move.add(td1);
		move.add(td2);
		
		GameMove gameMove = new GameMove(move);
		
		Validator.validateFoundInDictionary(gameMove);
		
		move.add(td3);
		Validator.validateFoundInDictionary(gameMove);
		
		move.add(td4);
		Validator.validateFoundInDictionary(gameMove);
	}
	

}
