package core;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.*;

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
    private static Boolean acceptConnections = true;
    private static ArrayList<ClientHandler> clientList;
    
    private static FileHandler logFileHandler;
    public static Logger log;
    private static SimpleFormatter logFormatter;	// see JDK_HOME/jre/lib/logging.properties
    

    public static void main(String args[])
    {
    	// set up logging
        try
        {
        	System.out.println();
        	System.out.print(Consts.STARTUP_MESSAGE);
        	System.out.println("wwss main: setting up logging");
        	log = Logger.getLogger("ServerLog");
            logFileHandler = new FileHandler("serverlog.log");
            log.addHandler( logFileHandler );
            /*for(Handler iHandler:log.getParent().getHandlers())
            {
            	log.getParent().removeHandler(iHandler);		// removes the timestamp lines from the log, reducing output, but also removes some needed logging
            }*/
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
        log.info("wwss Server version: " + Consts.SERVER_VERSION);

        Model.init();
        
        initDictionary();	//TODO - refactor - delegate Dictionary methods to a separate class
        
        // socket setup
        s = null;
        conn = null;
        
        clientList = new ArrayList<ClientHandler>();
        ClientHandler newClientHandler = null;
        
        try
        {
        	log.info("wwss main: attempting to create a ServerSocket on port: " + Consts.PORT);
        	
            // Create a server socket - 1st parameter is port number and 2nd is the backlog
            s = new ServerSocket(Consts.PORT, Consts.MAX_BACKLOG_CONNECTIONS);
             
            // Wait for an incoming connection
            echo("main: Server socket created. Waiting for connection...");
             
            while(acceptConnections)
            {
                // Get the connection socket
                conn = s.accept();
                 
                // Print the hostname and port number of the connection
                echo("main: Connection received from " + conn.getInetAddress().getHostName() + " on port: " + conn.getPort());
                 
                // Create new thread to handle client
                newClientHandler = new ClientHandler(conn);
                newClientHandler.setName("ClientHandler_" + conn.getPort());
                clientList.add(newClientHandler);
                newClientHandler.start();

                echo("main: client list size is now: " + clientList.size());
            }
        }
        catch(IOException e)
        {
            System.err.println("wwss main: IOException");
        }
        finally
        {
            // Close the connections and stream
            try
            {
            	System.out.println("wwss main: WARNING: closing ServerSocket.");
                s.close();
            }
            
            catch(IOException e)
            {
            	System.err.println("wwss main: Unable to close. IOexception");
            }
             
            catch(NullPointerException e)
            {
                System.err.println("wwss main: Unable to close. NullPointerException. Is server already running on this IP?");
            }
        }
    }
    
    /**
     * Reset the global dictionary and load a fresh copy from external file.
     */
    private static void initDictionary()
    {
		System.out.println("wwss main: initDictionary");
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
