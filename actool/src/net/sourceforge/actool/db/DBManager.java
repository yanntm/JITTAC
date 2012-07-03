package net.sourceforge.actool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
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


public class DBManager {

		private static Map<String, Connection> connections = new HashMap<String, Connection>();
//		private static ConcurrentLinkedQueue<Connection> free = new ConcurrentLinkedQueue<Connection>();
//		private static ConcurrentLinkedQueue<Connection>locked = new ConcurrentLinkedQueue<Connection>();
	
	//--------------------------------------------------db helper functions-----------------------------------------------------------------
		
		public static Connection connect() {
		    IWorkspace workspace = ResourcesPlugin.getWorkspace();
		    IWorkspaceRoot workspaceRoot = workspace.getRoot();
		    IPath workspacedir = new Path(Platform.getLocation().toString()+"/"+workspaceRoot.getName()+"/JavaModleDb_file");
		    return connect(workspacedir);
		}

		public static Connection connect(IPath workspacedir) {
			return connect("jdbc:hsqldb:"+workspacedir.toString()+"; shutdown=true", "sa", "");
		}
		
		
	
		public static Connection connect(String connectionString, String user, String password) {
			Connection conn = null;
			try{
				String key = connectionString+user+password;
				Class.forName("org.hsqldb.jdbcDriver");
				if(connections.containsKey(key)){
					if(!(conn=connections.get(key)).isClosed())
					return connections.get(key);
					else connections.remove(key);
				}	
			    conn = DriverManager.getConnection(connectionString, user, password);
			    connections.put(key, conn);
				
				
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
	    public static int query(String expression, Connection conn,IResultSetDelegate delegate,Object... args ) throws SQLException {
	        Statement st = conn.createStatement();
	        int result = delegate.invoke(st.executeQuery(expression),args);
	        st.close();     // also closes ResultSet rs
	        return result;
	    }
	    
	    public static int preparedQuery(String expression, Object[] values,Connection conn,IResultSetDelegate delegate,Object... args ) throws SQLException {
	        PreparedStatement st=conn.prepareStatement(expression);
	        for(int i=0; i<values.length;i++)
	        st.setObject(1+i, values[i]);
	        int result = delegate.invoke(st.executeQuery(),args);
	        st.close();     // also closes ResultSet rs
	        return result;
	    }
	    public static int preparedQuery(String expression,Connection conn,IResultSetDelegate delegate,Object... args ) throws SQLException {
	        PreparedStatement st=conn.prepareStatement(expression);
	        int result = delegate.invoke(st.executeQuery(),args);
	        st.close();     // also closes ResultSet rs
	        return result;
	    }

	//use for SQL commands CREATE, DROP, INSERT and UPDATE
	    public static synchronized void update(String expression, Connection conn) throws SQLException {

	        Statement st = conn.createStatement();
	        int i = st.executeUpdate(expression);    // run the query
	        if (i == -1) {
	            System.out.println("db error : " + expression);
	        }

	        st.close();
	    } // void update()
	    
	    public static synchronized void preparedUpdate(String expression, Connection conn) throws SQLException {
	    	preparedUpdate(expression,new Object[0], conn);
	        
	    }
	    public static synchronized void preparedUpdate(String expression,Object[] values, Connection conn) throws SQLException {

	        PreparedStatement st = conn.prepareStatement(expression);
	        for(int i=0; i<values.length;i++)
		        st.setObject(1+i, values[i]);
	        int i = st.executeUpdate();    // run the query
	        if (i == -1) {
	            System.out.println("db error : " + expression);
	        }
	        st.close();
	    }

	    public static void shutdown(Connection conn) throws SQLException {

	        Statement st = conn.createStatement();
	        st.execute("SHUTDOWN");
	        conn.close();    // if there are no other open connection
	    }

}
