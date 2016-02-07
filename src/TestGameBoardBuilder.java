

import com.mortaramultimedia.wordwolf.shared.messages.GameBoard;

public class TestGameBoardBuilder
{
	
	public TestGameBoardBuilder()
	{
		
	}

	public static void main(String[] args)
	{
		System.out.println("TestGameBoardBuilder: main");
		
		Integer size = 3;
		
		if (args.length > 0)
		{
			String sizeStr = (String) args[0]; 
			size = Integer.parseInt(sizeStr);
		}
		
		GameBoardBuilder gbb = new GameBoardBuilder();
		GameBoard gb = gbb.getNewGameBoard(-1, size, size, GameBoardBuilder.CHARACTER_SET_A);
//		System.out.println("TestGameBoardBuilder: GameBaord: " + gb);

	}

}
