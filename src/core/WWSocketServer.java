package core;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import com.mortaramultimedia.wordwolf.shared.constants.*;
import com.mortaramultimedia.wordwolf.shared.messages.*;

import constants.Consts;
import data.Model;

/**
 * Word Wolf Server - meant for remote deployment.
 * @author Jason Mortara
 */
//TODO make Singleton?
public class WWSocketServer
{
    private static ServerSocket s;
    private static Socket conn;
    
    private static FileHandler logFileHandler;
    public static Logger log;
    private static SimpleFormatter logFormatter;
    
    

    public static void main(String args[])
    {
    	// set up logging
        try
        {
        	System.out.print(Consts.STARTUP_MESSAGE);
        	System.out.println("wwss main: setting up logging");
        	log = Logger.getLogger("ServerLog");
            logFileHandler = new FileHandler("serverlog.log");
            log.addHandler( logFileHandler );
            logFormatter = new SimpleFormatter();
            logFileHandler.setFormatter( logFormatter );
            log.info("wwss Log Start ---------------------------------- ");
        }
        catch (SecurityException se)
        {
        	se.printStackTrace();
        }
        catch (IOException ioe)
        {
        	ioe.printStackTrace();
        }
        log.info("wwss First log message");
        log.info("wwss Server version: " + Consts.VERSION);

        Model.init();
        
        initDictionary();
        
        // socket setup
        s = null;
        conn = null;
        
        try
        {
        	log.info("wwss main: attempting to create a ServerSocket on port: " + Consts.PORT);
        	
            //1. creating a server socket - 1st parameter is port number and 2nd is the backlog
            s = new ServerSocket(Consts.PORT, Consts.MAX_BACKLOG_CONNECTIONS);
             
            //2. Wait for an incoming connection
            echo("main: Server socket created. Waiting for connection...");
             
            while(true)
            {
                //get the connection socket
                conn = s.accept();
                 
                //print the hostname and port number of the connection
                echo("main: Connection received from " + conn.getInetAddress().getHostName() + " : " + conn.getPort());
                 
                //create new thread to handle client
                new ClientHandler(conn).start();
            }
        }
         
        catch(IOException e)
        {
            System.err.println("wwss IOException");
        }
         
        //5. close the connections and stream
        try
        {
            s.close();
        }
         
        catch(IOException ioException)
        {
            System.err.println("wwss Unable to close. IOexception");
        }
    }
    
    private static void initDictionary()
    {
		System.out.println("wwss main: loadDictionary");
    	if(Model.getGlobalDictionary() == null)
    	{
    		populateDictionary();
    	}
    }
    
    /**
     * Get the result of the loaded external dictionary and put it into the Model for global access.
     */
	private static void populateDictionary()
	{
		System.out.println("wwss main: populateDictionary");

		HashMap<String, String> dict = new HashMap<String, String>();
		dict = loadDictionary();
		Model.setGlobalDictionary(dict);
		System.out.println("populateDictionary: globalDictionary length after load:  " + Model.getGlobalDictionary().size() );
	}

	/**
	 * Load the dictionary of allowable words for the game from an external file and return it as a HashMap.
	 * @return
	 */
	private static HashMap<String, String> loadDictionary()
	{
		System.out.println("wwss main: loadDictionary");

		HashMap<String, String> myDict = new HashMap<String, String>();
//		AssetManager assetManager = context.getAssets();
		String line;
		int currentLine = 0;
		int lastLineToPrint = 10;
		try {
			File file = new File("assets/dictionary_GIANT.txt");
			FileInputStream ims = new FileInputStream(file);
			BufferedReader r = new BufferedReader(new InputStreamReader(ims));
			try {
				while ((line=r.readLine()) != null) 
				{
					myDict.put(line, line);
					if ( currentLine <= lastLineToPrint)
					{
						System.out.println("read dictionary line: " + line);
					}
					currentLine++;
				}
				System.out.println("...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return myDict;
	}
    
    
    public static void echo(String msg)
    {
        System.out.println("wwss main: " + msg);
    }
    
    public static void logAndEcho(String msg)
    {
    	
    }
}
 
// moved ClientHandler to external .java class
