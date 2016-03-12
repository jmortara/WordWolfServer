package constants;

import game.GameBoardBuilder;

/**
 * Constants
 * @author jason mortara
 */
public class Consts
{
	// Server
	public static final String 	SERVER_VERSION = "0.0.14";
	public static final int 	PORT = 4001;
	public static final int 	MAX_BACKLOG_CONNECTIONS = 10;
	public static final String 	STARTUP_MESSAGE = 
			
		"#     #                   #     #                      ####                              \n" +
		"#  #  #  ###  ####  ####  #  #  #  ###  #    ####     #    # #### ####  #   # #### ####  \n" +
		"#  #  # #   # #   # #   # #  #  # #   # #    #        #      #    #   # #   # #    #   # \n" + 
		"#  #  # #   # ####  #   # #  #  # #   # #    ###       ####  ###  ####  #   # ###  ####  \n" + 
		"#  #  # #   # #  #  #   # #  #  # #   # #    #        #    # #    #  #   # #  #    #  #  \n" + 
		" ## ##   ###  #   # ####   ## ##   ###  #### #         ####  #### #   #   #   #### #   # "   + "v" + SERVER_VERSION + "\n\n"; 
	
	
	// Game 
	public static final String 	DEFAULT_CHARACTER_SET = GameBoardBuilder.CHARACTER_SET_W;
	public static final long 	DEFAULT_GAME_DURATION_MS = 60 * 1000;
	
}
