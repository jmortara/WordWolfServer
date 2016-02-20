package game;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.mortaramultimedia.wordwolf.shared.messages.GameBoard;
import com.mortaramultimedia.wordwolf.shared.messages.TileData;

import core.WWSocketServer;

/**
 * GameBoardBuilder - builds a GameBoard data object for distribution to each player in a 2-player game.
 * @author jason mortara
 *
 */
public class GameBoardBuilder
{
	private static Logger log;
	private GameBoard gameBoard;
	
	public static final String CHARACTER_SET_A = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String CHARACTER_SET_S = "EEEEEEEEEEEEAAAAAAAAAIIIIIIIIIOOOOOOOONNNNNNRRRRRRTTTTTTLLLLSSSSUUUUDDDDGGGBBCCMMPPFFHHVVWWYYKJXQZ";
	public static final String CHARACTER_SET_B = "EEEEEEEEEEEEEEEEEEETTTTTTTTTTTTTAAAAAAAAAAAARRRRRRRRRRRRIIIIIIIIIIINNNNNNNNNNNOOOOOOOOOOOSSSSSSSSSDDDDDDCCCCCHHHHHLLLLLFFFFMMMMPPPPUUUUGGGYYYWWBJKQVXZ";
	
	
	public GameBoardBuilder()
	{
		// init log
		try
		{
			log = WWSocketServer.log;
			log.info("GameBoardBuilder constructor.");
		}
		catch(Exception e)
		{
			System.out.println("GameBoardBuilder: could not link to WWSocketServer's log.");
		}
	}
	
	public GameBoard getNewGameBoard(int gameID, int rows, int cols, String charSet)
	{
//		List<TileData> rowData = generateTileDataArray(rows);
//		List<TileData> colData = generateTileDataArray(cols);
		TileData[][] boardData = new TileData[rows][cols];		// = new List<TileData>[][]();
//		List<List<TileData>> list = new ArrayList<List<TileData>>();
		
		char[] charArray = charSet.toCharArray();
		
		
		int row;
		int col;
		int rand;
		char letter;
		TileData td = null;
		for(row=0; row<rows; row++)
		{
			for(col=0; col<cols; col++)
			{
				// get a random char from the designated charSet
				rand = (int) Math.floor(Math.random()*charArray.length);
				letter = charArray[rand];
				
				td = new TileData(row, col, letter, false);
				boardData[row][col] = td;
			}
		}
		
		return new GameBoard(gameID, rows, cols, boardData);
	}
	
	/*private List<TileData> generateTileDataArray(int size)
	{
		TileData td;
		List<TileData> list;
		for(int i=0; i<size; i++)
		{
			td = new TileData(row, col, letter, selected);
		}
	}*/

}
