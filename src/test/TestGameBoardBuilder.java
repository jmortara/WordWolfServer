package test;


import java.util.ArrayList;
import java.util.List;

import com.mortaramultimedia.wordwolf.shared.game.Validator;
import com.mortaramultimedia.wordwolf.shared.messages.*;

import game.GameBoardBuilder;

public class TestGameBoardBuilder
{
	
	public TestGameBoardBuilder()
	{
		
	}

	public static void main(String[] args)
	{
		System.out.println("TestGameBoardBuilder: main");
		
		Integer size = 5;
		
		if (args.length > 0)
		{
			String sizeStr = (String) args[0]; 
			size = Integer.parseInt(sizeStr);
		}
		
		GameBoardBuilder gbb = new GameBoardBuilder();
		GameBoard gb = gbb.getNewGameBoard(-1, size, size, GameBoardBuilder.CHARACTER_SET_A);
		System.out.println("TestGameBoardBuilder: example letter at row 0, col 2: " + gb.getBoardData()[0][2]);
		System.out.println("TestGameBoardBuilder: GameBoard:");
		gb.printBoardData();
		
		
		//
		TileData td;
		for(int row=0; row<size; row++)
		{
			for(int col=0; col<size; col++)
			{
				td = gb.getTileDataAtPos(row, col);
				td.setLetter(new String("Z").charAt(0));
			}
		}
		
		// change to all A's
		System.out.println("");
		gb.printBoardData();
		
		// change to custom board
		char a = new String("A").charAt(0);
		char d = new String("D").charAt(0);
		char e = new String("E").charAt(0);
		char i = new String("I").charAt(0);
		char t = new String("T").charAt(0);
		char o = new String("I").charAt(0);
		char n = new String("N").charAt(0);
		
		td = gb.getTileDataAtPos(0, 0);
		td.setLetter(a);
		td = gb.getTileDataAtPos(0, 1);
		td.setLetter(d);
		td = gb.getTileDataAtPos(1, 1);
		td.setLetter(d);
		td = gb.getTileDataAtPos(2, 1);
		td.setLetter(e);
		td = gb.getTileDataAtPos(2, 2);
		td.setLetter(d);
		
		System.out.println("");
		
		gb.printBoardData();

		// build move - switch some values out here...
		TileData td0 = new TileData(0, 0, a, false);
		TileData td1 = new TileData(0, 1, d, false);
		TileData td2 = new TileData(1, 1, d, false);
		TileData td3 = new TileData(2, 1, e, false);
		TileData td4 = new TileData(2, 2, d, false);
		
		// ... or here to try various invalid moves
		ArrayList<TileData> move = new ArrayList<TileData>();
		move.add(td0);
		move.add(td1);
		move.add(td2);
		move.add(td3);
		move.add(td4);
		
		GameMove gameMove = new GameMove(move);
		Validator.validateMove(gb, gameMove);
	}

}
