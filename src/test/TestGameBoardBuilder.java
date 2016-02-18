package test;


import com.mortaramultimedia.wordwolf.shared.messages.GameBoard;

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
	}

}
