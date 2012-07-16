package net.sourceforge.actool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sourceforge.actool.defaults;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;


public class DBManager implements Runnable {
		private static DBManager instance = new DBManager();
		private  ConcurrentLinkedQueue<Connection> connectionPool;
		private  ConcurrentLinkedQueue<Connection> busyPool;
		private  static boolean  initDriver = true;
		
		private static  final int poolsize = defaults.MAX_THREADS*2;
		
	//--------------------------------------------------db helper functions-----------------------------------------------------------------
		protected DBManager(){
			connectionPool = new ConcurrentLinkedQueue<Connection>();
			busyPool = new ConcurrentLinkedQueue<Connection>();
			for(int i=0; i<poolsize; i++) {
			      connectionPool.add(connect());
			    }
			
			
		}
		
		/**
		 * @since 0.2
		 */
		public synchronized Connection getConnection()
			      throws SQLException {
			    if (!connectionPool.isEmpty()) {
			      Connection existingConnection =
			        (Connection)connectionPool.poll();
			      // If existing connection is closed
			      // then remove it. repeat the process of obtaining a connection.
			      // wake up threads that were waiting for a
			      // connection because maxConnection limit was reached.
			      if (existingConnection.isClosed()) {
			        notifyAll(); // Freed up a spot for anybody waiting
			        return(getConnection());
			      } else {
			    	  busyPool.add(existingConnection);
			        return(existingConnection);
			      }
			    } else {
			    	run();
			      return(getConnection());
			    }
			  }
		
		

		  /**
		 * @since 0.2
		 */
		public void run() {
		    try {
		      Connection connection = connect();
		      synchronized(this) {
		    	  connectionPool.add(connection);
		    	  notifyAll();
		      }
		    } catch(Exception e) { // SQLException or OutOfMemory
		      // Give up on new connection and wait for existing one
		      // to free up.
		    }
		  }
		
		  /**
		 * @since 0.2
		 */
		public synchronized void free(Connection connection) {
			  busyPool.remove(connection);
			  if(connectionPool.size()<poolsize)
				  connectionPool.add(connection);
				  notifyAll();
			  }
			    
			  /**
			 * @since 0.2
			 */
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
			 * @since 0.2
			   */

			  public synchronized void closeAllConnections() {
			    closeConnections(connectionPool);
			    connectionPool.clear();
			    closeConnections(busyPool);
			    busyPool.clear();
			  }

			  private void closeConnections(ConcurrentLinkedQueue<Connection> connectionPool2) {
			    try {
			    	Iterator<Connection> it = connectionPool2.iterator();
			    	while(it.hasNext()) {
				        Connection connection = it.next();
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
				if(initDriver){
					Class.forName("org.hsqldb.jdbcDriver");
					initDriver=false;
				}
			    conn = DriverManager.getConnection(connectionString, user, password);
			}
			catch(Exception e) {
			    System.out.println(e.toString());
			}
			return conn;
		}
	

		
		public static interface IResultSetDelegate{
	        public int invoke(ResultSet rs,Object... args)throws SQLException;
	    }
		
		//use for SQL command SELECT
	   
	    /**
		 * @since 0.2
		 */
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
	    /**
		 * @since 0.2
		 */
	    public static int preparedQuery(String expression,IResultSetDelegate delegate,Object... args ) throws SQLException {

	        return preparedQuery(expression, new Object[0],delegate,args);
	    }

	//use for SQL commands CREATE, DROP, INSERT and UPDATE

	    /**
		 * @since 0.2
		 */
	    public static void preparedUpdate(String expression) throws SQLException {
	    	preparedUpdate(expression,new Object[0]);
	        
	    }
	    /**
		 * @since 0.2
		 */
	    public static  void preparedUpdate(final String expression,final Object[] values) throws SQLException {
	    	
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
