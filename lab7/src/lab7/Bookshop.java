package lab7;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.swing.*;

import java.util.Properties;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.sql.Date;



public class Bookshop {

	Scanner in = null;
	Connection conn = null;
	// Database Host
	final String databaseHost = "orasrv1.comp.hkbu.edu.hk";
	// Database Port
	final int databasePort = 1521;
	// Database name
	final String database = "pdborcl.orasrv1.comp.hkbu.edu.hk";
	final String proxyHost = "faith.comp.hkbu.edu.hk";
	final int proxyPort = 22;
	final String forwardHost = "localhost";
	int forwardPort;
	Session proxySession = null;
	boolean noException = true;
    int boidcount=0;
	// JDBC connecting host
	String jdbcHost;
	// JDBC connecting port
	int jdbcPort;

	String[] options = { // if you want to add an option, append to the end of
							// this array
			"add a order", "print order information (by sid)", "delete a order (by oid)",
			"update a order", "GM Mode",
			"exit" };

	/**
	 * Get YES or NO. Do not change this function.
	 * 
	 * @return boolean
	 */
	boolean getYESorNO(String message) {
		JPanel panel = new JPanel();
		panel.add(new JLabel(message));
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		JDialog dialog = pane.createDialog(null, "Question");
		dialog.setVisible(true);
		boolean result = JOptionPane.YES_OPTION == (int) pane.getValue();
		dialog.dispose();
		return result;
	}

	/**
	 * Get username & password. Do not change this function.
	 * 
	 * @return username & password
	 */
	String[] getUsernamePassword(String title) {
		JPanel panel = new JPanel();
		final TextField usernameField = new TextField();
		final JPasswordField passwordField = new JPasswordField();
		panel.setLayout(new GridLayout(2, 2));
		panel.add(new JLabel("Username"));
		panel.add(usernameField);
		panel.add(new JLabel("Password"));
		panel.add(passwordField);
		JOptionPane pane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
			private static final long serialVersionUID = 1L;

			@Override
			public void selectInitialValue() {
				usernameField.requestFocusInWindow();
			}
		};
		JDialog dialog = pane.createDialog(null, title);
		dialog.setVisible(true);
		dialog.dispose();
		return new String[] { usernameField.getText(), new String(passwordField.getPassword()) };
	}

	/**
	 * Login the proxy. Do not change this function.
	 * 
	 * @return boolean
	 */
	public boolean loginProxy() {
		if (getYESorNO("Using ssh tunnel or not?")) { // if using ssh tunnel
			String[] namePwd = getUsernamePassword("Login cs lab computer");
			String sshUser = namePwd[0];
			String sshPwd = namePwd[1];
			try {
				proxySession = new JSch().getSession(sshUser, proxyHost, proxyPort);
				proxySession.setPassword(sshPwd);
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				proxySession.setConfig(config);
				proxySession.connect();
				proxySession.setPortForwardingL(forwardHost, 0, databaseHost, databasePort);
				forwardPort = Integer.parseInt(proxySession.getPortForwardingL()[0].split(":")[0]);
			} catch (JSchException e) {
				e.printStackTrace();
				return false;
			}
			jdbcHost = forwardHost;
			jdbcPort = forwardPort;
		} else {
			jdbcHost = databaseHost;
			jdbcPort = databasePort;
		}
		return true;
	}

	/**
	 * Login the oracle system. Change this function under instruction.
	 * 
	 * @return boolean
	 */
	public boolean loginDB() {
		String username = "f1225133";//Replace e1234567 to your username
		String password = "f1225133";//Replace e1234567 to your password
		
		/* Do not change the code below */
		if(username.equalsIgnoreCase("e1234567") || password.equalsIgnoreCase("e1234567")) {
			String[] namePwd = getUsernamePassword("Login sqlplus");
			username = namePwd[0];
			password = namePwd[1];
		}
		String URL = "jdbc:oracle:thin:@" + jdbcHost + ":" + jdbcPort + "/" + database;

		try {
			System.out.println("Logging " + URL + " ...");
			conn = DriverManager.getConnection(URL, username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Show the options. If you want to add one more option, put into the
	 * options array above.
	 */
	public void showOptions() {
		System.out.println("Please choose following option:");
		for (int i = 0; i < options.length; ++i) {
			System.out.println("(" + (i + 1) + ") " + options[i]);
		}
	}

	/**
	 * Run the manager
	 */
	public void run() {
		while (noException) {
			showOptions();
			String line = in.nextLine();
			if (line.equalsIgnoreCase("exit"))
				return;
			int choice = -1;
			try {
				choice = Integer.parseInt(line);
			} catch (Exception e) {
				System.out.println("This option is not available");
				continue;
			}
			if (!(choice >= 1 && choice <= options.length)) {
				System.out.println("This option is not available");
				continue;
			}
			if (options[choice - 1].equals("add a order")) {
				addFlight();
			} else if (options[choice - 1].equals("delete a order (by oid)")) {
				deleteFlight();
			} else if (options[choice - 1].equals("print order information (by sid)")) {
				printFlightByNo();
			} else if (options[choice - 1].equals("update a order")) {
				updatebookorder();
			} else if (options[choice - 1].equals("GM Mode")) {
				printstudentinfo();
			} else if (options[choice - 1].equals("exit")) {
				break;
			}
		}
	}

	/**
	 * Print out the infomation of a flight given a flight_no
	 * 
	 */
	private void printstudentinfo() {
		System.out.println("Print the student information");
		String yy=in.nextLine();
		yy=yy.trim();
		if (yy.equalsIgnoreCase("exit"))
			return;
		try {
		Statement stm = conn.createStatement();
		Statement stm2 = conn.createStatement();
        String sql2="select sum(order_price) from order_placed where sid="+yy;
		
		String sql = "SELECT * FROM students WHERE sid = " + yy ;
		
		ResultSet rs = stm.executeQuery(sql);
		ResultSet rs2=stm2.executeQuery(sql2);
		if (!rs.next()|| !rs2.next())
			return;
		String[] heads = { "SID", "GENDER", "NAME", "MAJOR", "Discount_lv"  };
		for (int i = 0; i < 5; ++i) { // flight table 6 attributes
				System.out.println(heads[i] + " : " + rs.getString(i + 1)); // attribute
																			// id
																			// starts
																			// with
																			// 1
			 }
		System.out.println("Total order amount:"+rs2.getString(1));
		}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	 

		
		
	
	private void updatebookorder() {
		System.out.print("Input a sid to update the order information");
		String yy=in.nextLine();
		yy=yy.trim();
		if (yy.equalsIgnoreCase("exit"))
			return;
		
		try {
			Statement stm = conn.createStatement();
			Statement stm2 = conn.createStatement();
			String sql123="select boid from bookorder_hv where sid="+yy;
			ResultSet rs = stm2.executeQuery(sql123);
            while(rs.next()) {
			String sql ="DECLARE\r\n" + 
					"  n DATE;\r\n" + 
					"  v NUMBER;\r\n" + 
					"BEGIN\r\n" + 
					"  SELECT deliverydate INTO n FROM bookorder_hv WHERE boid="+rs.getString(1)+" ;\r\n" + 
					"\r\n" + 
					"  SELECT SYSDATE - n INTO v FROM dual;\r\n" + 
					"\r\n" + 
					"  UPDATE bookorder_hv SET deliverydate = NULL WHERE  boid="+rs.getString(1)+"  AND v > 0;\r\n" + 
					"END;"
					;
			stm.execute(sql); // please pay attention that we use
			

            }} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
		
	}
	private void printFlightInfo(String flight_no) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT * FROM order_placed WHERE sid = " + flight_no ;
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return;
			String[] heads = { "sid", "order_price", "paymethod", "oid", "odate", "Cardno" };
			while(rs.next()) {
			for (int i = 0; i < 6; ++i) { // flight table 6 attributes
				try {
					System.out.println(heads[i] + " : " + rs.getString(i + 1)); // attribute
																				// id
																				// starts
																				// with
																				// 1
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} }catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
	}
	private void listallbook(int bid) {
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT * FROM books where bid="+bid ;
			ResultSet rs = stm.executeQuery(sql);
			if (!rs.next())
				return;
			String[] heads = { "bid", "title", "quantity", "author", "price" };
			for(int i=0;i<5;++i){ // flight table 6 attributes
				try {
					System.out.println(heads[i] + " : " + rs.getString(i+1)); // attribute
														
																				// starts
																				// with
																				// 1
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			noException = false;
		}
	}


	/**
	 * List all flights in the database.
	 */
	private void listAllFlights() {
		System.out.println("All orders in the database now:");
		try {
			Statement stm = conn.createStatement();
			String sql = "SELECT oid FROM order_placed";
			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0;
			while (rs.next()) {
				System.out.println(rs.getString(1));
				++resultCount;
			}
			System.out.println("Total " + resultCount + " order(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
	}

	/**
	 * Select out a flight according to the flight_no.
	 */
	private void printFlightByNo() {
		listAllFlights();
		System.out.println("Please input the sid to order info:");
		String line = in.nextLine();
		line = line.trim();
		if (line.equalsIgnoreCase("exit"))
			return;

		printFlightInfo(line);
	}

	/**
	 * Given source and dest, select all the flights can arrive the dest
	 * directly. For example, given HK, Tokyo, you may find HK -> Tokyo Your job
	 * to fill in this function.
	 */
	private void selectFlightsInZeroStop() {
		System.out.println("Please input source, dest:");

		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;

		String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		try {
			/**
			 * Create the statement and sql
			 */
			Statement stm = conn.createStatement();

			String sql = String.format("SELECT FLIGHT_NO FROM FLIGHTS WHERE SOURCE = '%s' AND DEST = '%s'", values[0], values[1]);
			/**
			 * Formulate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */
			System.out.println(sql);

			ResultSet rs = stm.executeQuery(sql);

			int resultCount = 0; // a counter to count the number of result
									// records
			while (rs.next()) { // this is the result record iterator, see the
								// tutorial for details

				/*
				 * Write your own to print flight information; you may use the
				 * printFlightInfo() function
				 */
				printFlightInfo(rs.getString(1));
				++resultCount;
				System.out.println("=================================================");

			}
			System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		}
	}

	/**
	 * Given source and dest, select all the flights can arrive the dest in one
	 * stop. For example, given HK, Tokyo, you may find HK -> Beijing, Beijing
	 * -> Tokyo Your job to fill in this function.
	 */
	private void selectFlightsInOneStop() {
		System.out.println("Please input source, dest:");

		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;

		String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i)
			values[i] = values[i].trim();

		/**
		 * try {
		 * 
		 * // Similar to the 'selectFlightsInZeroStop' function; write your own
		 * code here
		 * 
		 * 
		 * } catch (SQLException e) { e.printStackTrace(); noException = false;
		 * }
		 */
		try {
			//select * from flights where depart_time > to_date('2000/01/01/10:00:00', 'yyyy/mm/dd/hh24/mi/ss');
			// HK, Beijing
			Statement stm = conn.createStatement();
			String sql = String.format("SELECT F1.FLIGHT_NO, F2.FLIGHT_NO FROM FLIGHTS F1, FLIGHTS F2 WHERE F1.SOURCE = '%s' AND F1.DEST = F2.SOURCE AND F2.DEST = '%s' AND F1.ARRIVE_TIME <= F2.DEPART_TIME", values[0], values[1]);
			System.out.println(sql);
			ResultSet rs = stm.executeQuery(sql);
			
			int resultCount = 0;
			while (rs.next()) {
				printFlightInfo(rs.getString(1));
				System.out.println("-------------------------------------------------");
				printFlightInfo(rs.getString(2));
				++resultCount;
				System.out.println("=================================================");
			}
			System.out.println("Total " + resultCount + " choice(s).");
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			noException = false;
		} 
	}

	/**
	 * Insert data into database
	 * 
	 * @return
	 */
	private void addbookorder() {
		for (int i=1;i<4;i++) {
		listallbook(i);}
		System.out.println("Do you want to add more book?If yes please input the deliverydate,quantity");
		System.out.println(",oid,sid,bid,boid");
		String line="1";
		while(!line.equalsIgnoreCase("exit")) {
			line=in.nextLine();
			if (line.equalsIgnoreCase("exit"))
				return;
			String[]values=line.split(",");
			for(int i=0;i<values.length;++i) {
				values[i]=values[i].trim();
				if (values.length < 5) {
					System.out.println("The value number is expected to be 6");
					return;
				}
			}
			try {
				Statement stm = conn.createStatement();
				String sql = "INSERT INTO bookorder_hv VALUES(TO_DATE('"+values[0]+"','YYYY-MM-DD'),"  +  
																						// is
																						// flight
																						// no
				values[5] +","+ // this
																
				
		// is
																			// depart_time
			 values[1]  +","+ // this
																			// is
																			// arrive_time
				values[2]+","// this is fare
						 + values[3] + ", " + // this is source
						 values[4]  + // this is dest
						")";
				String sql2="INSERT INTO HV VALUES("+values[4]+values[5]+")";
				

				stm.executeUpdate(sql);
				
				stm.close();
				System.out.println("succeed to add a bookorder ");
				boidcount++;
			} catch (SQLException e) {
				
				e.printStackTrace();
				System.out.println("fail to add a bookorder " + line);
				noException = false;
			}
			
			
		}
		
	}
	private void addFlight() {
		for (int i=1;i<4;i++) {
			listallbook(i);}
		System.out.println("Please input the sid,order_price , pay method, oid, odate, card no,deliverydate,quantity,Bid,boid:");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;
		String[] values = line.split(",");
		if (values.length < 9) {
			System.out.println("The value number is expected to be 9");
			return;
		}
		for (int i = 0; i < values.length; ++i) {
			values[i] = values[i].trim();
            System.out.print(values[i]+" ");}
		try {
			Statement stm = conn.createStatement();
			String sql = "INSERT INTO order_placed VALUES("+values[0]+ ", " + // this
																					// is
																					// sid
			values[1]+"," + // this
																		// is
																		// order_price
					"'"+  values[2]+"'," + // this
																		// is
																		// pay method
			values[3] + ", " + // this is oid
					"" +"TO_DATE('"+values[4]+"'"+",'YYYY-MM-DD')" + ", " + // this is odate
					  values[5]+ // this is cardno
					")";
			String sql2="INSERT INTO BOOKORDER_HV VALUES(TO_DATE('"+values[6]+"','YYYY-MM-DD'),"+values[9]+","+
					values[7]+","+values[3]+","+values[0]+","+values[8]+")";
			String sql3="INSERT INTO HV VALUES("+values[8]+","+values[9]+")";
			System.out.println(sql);
			System.out.println(sql2);
			String sql0="Select quantity from books where bid="+values[8];
			
			ResultSet rs = stm.executeQuery(sql0);
			while(rs.next()) {
			int i=rs.getInt(1);
			int input=Integer.parseInt(values[7]);
			if(i==0 || input>i) {
				System.out.println("Cannot add order because your book is out of stock");
				return;
			}}

			stm.executeUpdate(sql);
			stm.executeUpdate(sql2);
			stm.executeUpdate(sql3);

			stm.close();
			boidcount++;

			System.out.println("succeed to add order ");
	        addbookorder();

			printFlightInfo(values[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to add a order " + line);
			noException = false;
		}
	}

	/**
	 * Please fill in this function to delete a flight.
	 */
	public void deleteFlight() {
		listAllFlights();
		System.out.println("Please input the oid to delete:");
		String line = in.nextLine();

		if (line.equalsIgnoreCase("exit"))
			return;
		line = line.trim();

		try {
			Statement stm = conn.createStatement();
			String sql22=" SELECT (TRUNC(SYSDATE) - TRUNC(odate))from order_placed , dual where oid= "+line;
			ResultSet rs2=stm.executeQuery(sql22);
			while(rs2.next()) {
				if(rs2.getInt(1)>7) {
					System.out.println("Your order placed 7 days ago");
					return;
				}
			}
			String sql0="select deliverydate from bookorder_hv where oid="+line;
			ResultSet rs =stm.executeQuery(sql0);
		while(rs.next()) {
				if(rs.getString(1)==null) {
					System.out.println("You order have some book delivered");
					return;
				}}
				// please pay attention that we use
			
				String sql1="DELETE FROM BOOKORDER_HV WHERE OID="+line;


			String sql = "DELETE FROM order_placed WHERE oid="+line;
			/*
			 * Formuate your own SQL query:
			 *
			 * sql = "...";
			 *
			 */

			stm.executeUpdate(sql1); // please pay attention that we use
			stm.executeUpdate(sql);						// executeUpdate to update the database

			stm.close();

			/*
			 * You may uncomment the statement below after formulating the SQL
			 * query above
			 *
			 */ System.out.println("succeed to delete order " + line);
		
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("fail to delete order " + line);
			noException = false;
		}
	}

	/**
	 * Close the manager. Do not change this function.
	 */
	public void close() {
		System.out.println("Thanks for using this manager! Bye...");
		try {
			if (conn != null)
				conn.close();
			if (proxySession != null) {
				proxySession.disconnect();
			}
			in.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor of flight manager Do not change this function.
	 */
	public Bookshop() {
		System.out.println("Welcome to use this manager!");
		in = new Scanner(System.in);
	}

	/**
	 * Main function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Bookshop manager = new Bookshop();
		if (!manager.loginProxy()) {
			System.out.println("Login proxy failed, please re-examine your username and password!");
			return;
		}
		if (!manager.loginDB()) {
			System.out.println("Login database failed, please re-examine your username and password!");
			return;
		}
		System.out.println("Login succeed!");
		try {
			manager.run();
		} finally {
			manager.close();
		}
	}
}