package net.sourceforge.actool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sourceforge.actool.defaults;
import net.sourceforge.actool.model.da.Component;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;


public class DBManager implements Runnable {
		private static DBManager instance = new DBManager();
//		private static Map<String, Connection> connections = new HashMap<String, Connection>();
		private  Vector<Connection> connectionPool;
		private  Vector<Connection> busyPool;
		private  boolean connectionPending;
		private  final long timeout;
		private  boolean waitIfBusy;
		private static  final int poolsize = defaults.MAX_THREADS*2;
		
	//--------------------------------------------------db helper functions-----------------------------------------------------------------
		protected DBManager(){
//			connections = new HashMap<String, Connection>();
			connectionPool = new Vector<Connection>(poolsize);
			busyPool = new Vector<Connection>();
			for(int i=0; i<poolsize; i++) {
			      connectionPool.addElement(connect());
			    }
			connectionPending = false;
			timeout=60000;
			waitIfBusy =true;
		}
		
		public synchronized Connection getConnection()
			      throws SQLException {
			    if (!connectionPool.isEmpty()) {
			      Connection existingConnection =
			        (Connection)connectionPool.lastElement();
			      int lastIndex = connectionPool.size() - 1;
			      connectionPool.removeElementAt(lastIndex);
			      // If connection on available list is closed (e.g.,
			      // it timed out), then remove it from available list
			      // and repeat the process of obtaining a connection.
			      // Also wake up threads that were waiting for a
			      // connection because maxConnection limit was reached.
			      if (existingConnection.isClosed()) {
			        notifyAll(); // Freed up a spot for anybody waiting
			        return(getConnection());
			      } else {
			    	  busyPool.addElement(existingConnection);
			        return(existingConnection);
			      }
			    } else {
			      
			      // Three possible cases:
			      // 1) You haven't reached maxConnections limit. So
			      //    establish one in the background if there isn't
			      //    already one pending, then wait for
			      //    the next available connection (whether or not
			      //    it was the newly established one).
			      // 2) You reached maxConnections limit and waitIfBusy
			      //    flag is false. Throw SQLException in such a case.
			      // 3) You reached maxConnections limit and waitIfBusy
			      //    flag is true. Then do the same thing as in second
			      //    part of step 1: wait for next available connection.
			      
//			      if ((totalConnections() < defaults.MAX_THREADS) &&
//			          !connectionPending) {
//			        makeBackgroundConnection();
//			      } else if (!waitIfBusy) {
//			        throw new SQLException("Connection limit reached");
//			      }
			      // Wait for either a new connection to be established
			      // (if you called makeBackgroundConnection) or for
			      // an existing connection to be freed up.
//			      try {
//			        wait();
//			      } catch(InterruptedException ie) {}
			      // Someone freed up a connection, so try again.
			    	run();
			      return(getConnection());
			    }
			  }
		
		
		private void makeBackgroundConnection() {
		    connectionPending = true;
		    try {
		      Thread connectThread = new Thread(this);
		      connectThread.start();
		    } catch(OutOfMemoryError oome) {
		      // Give up on new connection
		    }
		  }
		
		

		  public void run() {
		    try {
		      Connection connection = connect();
		      synchronized(this) {
		    	  connectionPool.addElement(connection);
		        connectionPending = false;
		        notifyAll();
		      }
		    } catch(Exception e) { // SQLException or OutOfMemory
		      // Give up on new connection and wait for existing one
		      // to free up.
		    }
		  }
		
		  public synchronized void free(Connection connection) {
			  busyPool.removeElement(connection);
			  if(connectionPool.size()<poolsize)
			    connectionPool.addElement(connection);
			    // Wake up threads that are waiting for a connection
			    notifyAll(); 
			  }
			    
			  public synchronized int totalConnections() {
			    return(connectionPool.size() +
			    		busyPool.size());
			  }

			  /** Close all the connections. Use with caution:
			   *  be sure no connections are in use before
			   *  calling. Note that you are not <I>required</I> to
			   *  call this when done with a ConnectionPool, since
			   *  connections are guaranteed to be closed when
			   *  garbage collected. But this method gives more control
			   *  regarding when the connections are closed.
			   */

			  public synchronized void closeAllConnections() {
			    closeConnections(connectionPool);
			    connectionPool = new Vector();
			    closeConnections(busyPool);
			    busyPool = new Vector();
			  }

			  private void closeConnections(Vector connections) {
			    try {
			      for(int i=0; i<connections.size(); i++) {
			        Connection connection =
			          (Connection)connections.elementAt(i);
			        if (!connection.isClosed()) {
			          connection.close();
			        }
			      }
			    } catch(SQLException sqle) {
			      // Ignore errors; garbage collect anyhow
			    }
			  }
			  
		
		protected static Connection connect() {
			
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
		    IWorkspaceRoot workspaceRoot = workspace.getRoot();
		    IPath workspacedir = new Path(Platform.getLocation().toString()+"/"+workspaceRoot.getName()+"/JavaModleDb_file");
		    return connect(workspacedir);
		}

		protected static Connection connect(IPath workspacedir) {
			return connect("jdbc:hsqldb:"+workspacedir.toString()+"; shutdown=true", "sa", "");
		}
		
		
	
		protected static Connection connect(String connectionString, String user, String password) {
			Connection conn = null;
			try{
				String key = connectionString+user+password;
				Class.forName("org.hsqldb.jdbcDriver");
//				if(connections.containsKey(key)){
//					if(!(conn=connections.get(key)).isClosed())
//					return connections.get(key);
//					else connections.remove(key);
//				}	
			    conn = DriverManager.getConnection(connectionString, user, password);
//			    connections.put(key, conn);
				
				
			}
			catch(Exception e) {
			    System.out.println(e.toString());
			}
			return conn;
		}
	
//		public static Connection checkout(){
//				Connection result = null;
//				if(!free.isEmpty()){
//					try {
//					while(!free.isEmpty()&&((result=free.poll()).isClosed()));
//					while(locked.size()<defaults.MAX_THREADS&&(result==null||result.isClosed()))Thread.sleep(20); 
//						result = connect();
//					locked.add(result);
//					} catch (SQLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					return result;
//					
//				}
//		}
		
		public static interface IResultSetDelegate{
	        public int invoke(ResultSet rs,Object... args)throws SQLException;
	    }
		
		//use for SQL command SELECT
	   
	    public static int preparedQuery(String expression, Object[] values/*,Connection conn1*/,IResultSetDelegate delegate,Object... args ) throws SQLException {
	    	Connection conn = instance.getConnection();
	    	PreparedStatement st=conn.prepareStatement(expression);
	        for(int i=0; i<values.length;i++)
	        st.setObject(1+i, values[i]);
	        int result = delegate.invoke(st.executeQuery(),args);
	        st.close();     // also closes ResultSet rs
	        instance.free(conn);
	        return result;
	    }
	    public static int preparedQuery(String expression/*,Connection conn*/,IResultSetDelegate delegate,Object... args ) throws SQLException {
//	        PreparedStatement st=conn.prepareStatement(expression);
//	        int result = delegate.invoke(st.executeQuery(),args);
//	        st.close();     // also closes ResultSet rs
	        return preparedQuery(expression, new Object[0]/*,conn*/,delegate,args);
	    }

	//use for SQL commands CREATE, DROP, INSERT and UPDATE

	    public static void preparedUpdate(String expression/*, Connection conn*/) throws SQLException {
	    	preparedUpdate(expression,new Object[0]/*, conn*/);
	        
	    }
	    public static  void preparedUpdate(final String expression,final Object[] values/*, Connection conn1*/) throws SQLException {
	    	
	    	Connection conn = instance.getConnection();
	        PreparedStatement st = conn.prepareStatement(expression);
	        for(int i=0; i<values.length;i++)
		        st.setObject(1+i, values[i]);
	        int i = st.executeUpdate();    // run the query
	        if (i == -1) {
	            System.out.println("db error : " + expression);
	        }
	        st.close();
	        instance.free(conn);	
	    }

	    public static void shutdown(Connection conn) throws SQLException {

	        Statement st = conn.createStatement();
	        st.execute("SHUTDOWN");
	        conn.close();    // if there are no other open connection
	    }
	    

}
