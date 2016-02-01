package test;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.mortaramultimedia.wordwolf.shared.messages.LoginRequest;
import com.mortaramultimedia.wordwolf.shared.messages.LoginResponse;

import database.MySQLAccess;


public class MySQLAccessTester
{
	private static final String TAG = "MySQLAccessTester";

	private static MySQLAccess dao;
	
	
	
	public static void main(String[] args) throws SQLException
	{
		System.out.println("MySQLAccessTester: main");
		testConnection();
		
		LoginRequest loginRequest = new LoginRequest(1, "test1", "test1pass", "test1@wordwolfgame.com");
		LoginResponse loginResponse = dao.login(loginRequest, true);
	}
	

	public static Boolean testConnection()
	{
		System.out.println("MySQLAccessTester: testConnection");

		dao = new MySQLAccess();

		try
		{
			dao.connectToDataBase();
			dao.getAllUsers();
			dao.createRandomNewUser();
			ResultSet testUser = dao.getUser("tyler", true);
			dao.updateCurrentScore("jason");
			return true;
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Attempt login with the LoginRequest obj stored in the Model, say from the LoginActivity
	 * @return
	 */
	
	/*public static Boolean attemptLogin(LoginRequest loginRequest)
	{
		MySQLAccess dao = new MySQLAccess();

		System.out.println("MySQLAccessTester: attemptLogin via passed LoginRequest with these credentials: " + loginRequest);

		if (dao != null)
		{
			try
			{
				dao.connectToDataBase();
				
				ResultSet retrievedUsers = dao.getUser(loginRequest.getUserName(), false);

				// we need to know if there were actual rows of data retrieved by the db query in order to know if the login succeeded or not
				int numRows = 0;
				if (retrievedUsers != null)
				{
					retrievedUsers.beforeFirst();
					retrievedUsers.last();
					numRows = retrievedUsers.getRow();
				}

				if (numRows == 1)
				{
					System.out.println("MySQLAccessTester: attemptLogin: success! found a single matching user.");
					return true;
				}
				else
				{
					System.out.println("MySQLAccessTester: attemptLogin: failed! found this many matching users: " + numRows);
				}
			}
			catch (SQLException e)
			{
				System.out.println("MySQLAccessTester: attemptLogin: ERROR");
				e.printStackTrace();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}*/
	
}