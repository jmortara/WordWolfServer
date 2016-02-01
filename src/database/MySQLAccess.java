package database;
//see http://www.vogella.com/tutorials/MySQLJava/article.html

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.mortaramultimedia.wordwolf.shared.messages.LoginRequest;
import com.mortaramultimedia.wordwolf.shared.messages.LoginResponse;

import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;


public class MySQLAccess 
{
  private Connection sqlConnection = null;
  private Statement statement = null;
  private PreparedStatement preparedStatement = null;
  private ResultSet resultSet = null;

  public void connectToDataBase() throws Exception
  {
    try 
    {
      // this will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      // setup the connection with the DB.
      sqlConnection = DriverManager.getConnection("jdbc:mysql://mysql.wordwolfgame.com:3306/wordwolfdb?user=jmortara&password=wordwolf99");
      
		//get some of the properties we need from an external properties file in the resource bundle. use them for connecting. 
		 PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle.getBundle("database"); // i.e. the database.properties file
		 String JDBCDriver 		= bundle.getString("JDBCDriver");
		 String url 			= bundle.getString("url");
		 String port 			= bundle.getString("port");
		 String dbname 			= bundle.getString("dbname");
		 String user 			= bundle.getString("user");
		 String password 		= bundle.getString("password");
		 
		 Class.forName( JDBCDriver );	// this loads the defined JDBC class by name
		 String connectionStr	= url + ":" + port + "/" + dbname + "?" + "user=" + user + "&password=" + password; 
		 
		 sqlConnection = DriverManager.getConnection( connectionStr );

		 System.out.println("connectToDataBase: CONNECTION SUCCESSFUL");
    } 
    catch (CommunicationsException e) 
    {
    	System.out.println("connectToDataBase: CONNECTION FAILED. COMMUNICATIONS LINK FAILURE. POSSIBLE CONNECTION TIMEOUT OR WRONG PORT.");
    	throw e;
    } 
    catch (SQLException e) 
    {
    	 System.out.println("connectToDataBase: CONNECTION FAILED. ACCESS DENIED.");
    	 throw e;
    } 
    catch (Exception e) 
    {
      throw e;
    } 
    finally 
    {
      
    }
  }

  public void getAllUsers() throws Exception 
  {
	  System.out.println("getAllUsers: user table data:");
	  try
	  {
	      // statements allow to issue SQL queries to the database
	      statement = sqlConnection.createStatement();
	      
	      // resultSet gets the result of the SQL query
	      resultSet = statement.executeQuery("SELECT * from users");
	      
	      // resultSet is initialised before the first data set
	      while (resultSet.next()) 
	      {
	        // it is possible to get the columns via name
	        // also possible to get the columns via the column number
	        // which starts at 1
	        // e.g., resultSet.getSTring(2);
	        String username 	= resultSet.getString("username");
	        String email 		= resultSet.getString("email");
	        int current_score 	= resultSet.getInt("current_score");
	        int high_score 		= resultSet.getInt("high_score");
	        System.out.println("username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
//	        System.out.println("email: " + email);
//	        System.out.println("current_score: " + current_score);
//	        System.out.println("high_score: " + high_score);
//	        System.out.println("---");
//	        String summary = resultSet.getString("summary");
//	        Date date = resultSet.getDate("datum");
//	        String comment = resultSet.getString("comments");
//	        System.out.println("Date: " + date);
//	        System.out.println("Comment: " + comment);
	      }
	  }
	  catch(Exception e)
	  {
		  throw e;
	  }
	  finally
	  {
		  resultSet.close();
	  }
  }
  
  public Boolean createNewUser(String username, String password, String email) throws Exception
  {	
	  System.out.println("createNewUser: " + username);
	  try
	  {
	      // statements allow to issue SQL queries to the database
//	      statement = connection.createStatement();
//	      resultSet = statement.execute("INSERT INTO users (username, password, email, current_score, high_score) VALUES ('" + username + "', 'pass123', 'm@m.com', 0, 0)");

	      // preparedStatements can use variables and are more efficient
	      preparedStatement = sqlConnection.prepareStatement("INSERT INTO users (username, password, email, current_score, high_score) VALUES (?, ?, ?, ?, ?)");
	      // "myuser, webpage, datum, summary, COMMENTS from FEEDBACK.COMMENTS");
	      // parameters start with 1
	      preparedStatement.setString(1, username);
	      preparedStatement.setString(2, password);
	      preparedStatement.setString(3, email);
//	      preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
	      preparedStatement.setInt(4, 0);
	      preparedStatement.setInt(5, 0);
	      preparedStatement.executeUpdate();

//	      preparedStatement = connection.prepareStatement("SELECT user, password, email, current_score, high_score from users");
//	      resultSet = preparedStatement.executeQuery();
//	      writeResultSet(resultSet);

	      // remove again the insert comment
//	      preparedStatement = connection.prepareStatement("delete from FEEDBACK.COMMENTS where myuser= ? ; ");
//	      preparedStatement.setString(1, "Test");
//	      preparedStatement.executeUpdate();
	      
//	      resultSet = statement.executeQuery("select * from FEEDBACK.COMMENTS");
//	      writeMetaData(resultSet);
		  return true;
	  }
	  catch(MySQLIntegrityConstraintViolationException dupeUserException)
	  {
		  System.out.println("createNewUser: FAILED TO CREATE NEW USER. USERNAME ALREADY EXISTS: " + username);
	  }
	  catch (SQLException e)
	  {
		  e.printStackTrace();
	  }
	  finally
	  {
		  preparedStatement.close();
	  }
	  return false;
  }
  
  public void createRandomNewUser() throws Exception
  {
	  int rand = (int)(Math.random()*10000);
	  String username = "_deleteme" + rand;
	  System.out.println("createRandomNewUser: " + username);
	  createNewUser(username, "pass"+rand, "deleteme"+rand+"@gmail.com");
  }
  
  public void updateCurrentScore(String user) throws SQLException
  {
	System.out.println("updateHighScore: for: " + user);
	
	ResultSet userRecord = getUser( user, false );
	
	try
	{
		int existingCurrentScore = userRecord.getInt("current_score");
		int newCurrentScore = existingCurrentScore + 1;
	      preparedStatement = sqlConnection.prepareStatement("UPDATE users SET current_score=" + newCurrentScore + " WHERE username='" + user + "';");
//	      preparedStatement.setInt(5, 0);
	      preparedStatement.executeUpdate();
	      
	      // check result
	      getUser( user, true );
	}
	catch (SQLException e)
	{
		System.out.println("updateHighScore: FAILED.");
		throw e;
	}
	finally
	{
		if ( userRecord != null )
		{
			userRecord.close();
		}
	}
	
	/*
	try
	{
		statement = connection.createStatement();
	      
	      // resultSet gets the result of the SQL query
	      resultSet = statement.executeQuery("SELECT * from users WHERE username='" + user + "'");
	      
	      //TODO: there should only be one row. if there is more than one then multiple users have returned with the same username
	      boolean rowLocated = resultSet.first();
	      if ( rowLocated )
	      {
			System.out.println("updateHighScore: user record located: " + user);
			String username 	= resultSet.getString("username");
			String email 		= resultSet.getString("email");
			int current_score 	= resultSet.getInt("current_score");
			int high_score 		= resultSet.getInt("high_score");
			System.out.println("username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
	      }
	      else
	      {
	    	  Exception e = new Exception("FAILED TO GET USER BY USERNAME: " + user);
	    	  throw e;
	      }
	} 
	catch (Exception e)
	{
		System.out.println(e);
	}
	finally
	{
		resultSet.close();
	}
	*/
  }
  
	public LoginResponse login(LoginRequest request, Boolean close) throws SQLException
	{
		System.out.println("MySQLAccess: login: " + request);
		
		LoginResponse response = null;
		String errMsg = null;
		try
		{
			statement = sqlConnection.createStatement();

			// resultSet gets the result of the SQL query
			resultSet = statement.executeQuery( "SELECT * from users WHERE username='" + request.getUserName() + "' AND password='" + request.getPassword() + "'");

			// TODO: there should only be one row. if there is more than one
			// then multiple users have returned with the same username
			boolean resultIsValid = resultSet.first();		// move the cursor to the first row; return true if that command is successful
			if (resultIsValid)
			{
				
				System.out.println("MySQLAccess: login: user record located: " + request.getUserName());
				//TODO: this section may also reveal issues if the ResultSet has more than one row
				String username 	= resultSet.getString("username");
				String email 		= resultSet.getString("email");
				int current_score 	= resultSet.getInt("current_score");
				int high_score 		= resultSet.getInt("high_score");
				
				System.out.println("MySQLAccess: login: username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
				response = new LoginResponse(true, 1, username, null, false, 0);
			} 
			// result set did not find at least one matching user? return response w/ error
			else
			{
				response = new LoginResponse(false, 1, request.getUserName(), "ERROR: EMPTY USER/PASSWORD QUERY RESULT", false, 0);
				Exception e = new Exception( "MySQLAccess: login: FAILED. USERNAME NOT FOUND: " + request.getUserName());
				throw e;
			}
		} 
		// attempt to create result set threw error? return response w/ error
		catch (Exception e)
		{
			response = new LoginResponse(false, 1, request.getUserName(), "ERROR: EXCEPTION DURING ATTEMPT TO RETRIEVE USER/PASSWORD", false, 0);
			System.out.println(e);
		} 
		finally
		{
			if (null != resultSet && close)
			{
				resultSet.close();
			}
		}

		return response;
	}
  
  public ResultSet getUser(String user, Boolean close) throws SQLException
  {
	try
	{
		statement = sqlConnection.createStatement();
	      
	      // resultSet gets the result of the SQL query
	      resultSet = statement.executeQuery("SELECT * from users WHERE username='" + user + "'");
	      
	      //TODO: there should only be one row. if there is more than one then multiple users have returned with the same username
	      boolean rowLocated = resultSet.first();
	      if ( rowLocated )
	      {
			System.out.println("getUser: user record located: " + user);
			String username 	= resultSet.getString("username");
			String email 		= resultSet.getString("email");
			int current_score 	= resultSet.getInt("current_score");
			int high_score 		= resultSet.getInt("high_score");
			System.out.println("username:" + username + ", email:" + email + ", current_score:" + current_score + ", high_score:" + high_score);
	      }
	      else
	      {
	    	  Exception e = new Exception("getUser: FAILED. USERNAME NOT FOUND: " + user);
	    	  throw e;
	      }
	} 
	catch (Exception e)
	{
		System.out.println(e);
	}
	finally
	{
		if ( close )
		{
			resultSet.close();
		}
	}
	
	return resultSet;
  }
  
  private void writeMetaData(ResultSet resultSet) throws SQLException {
    // now get some metadata from the database
    System.out.println("The columns in the table are: ");
    System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
    for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
      System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
    }
  }


  // you need to close all three to make sure
  private void close() {
//    resultSet.close();
//    close(statement);
//    close(connect);
  }
  
  /*
  private void close(Closeable c) {
    try {
      if (c != null) {
        c.close();
      }
    } catch (Exception e) {
    // don't throw now as it might leave following closables in undefined state
    }
  }*/
} 