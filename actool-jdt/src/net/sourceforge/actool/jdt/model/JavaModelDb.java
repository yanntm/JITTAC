package net.sourceforge.actool.jdt.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;


import net.sourceforge.actool.defaults;
import net.sourceforge.actool.model.ia.IXReference;
import net.sourceforge.actool.model.ia.IXReferenceFactory;
import net.sourceforge.actool.model.ia.ImplementationChangeDelta;
import net.sourceforge.actool.model.ia.ImplementationChangeListener;


public class JavaModelDb extends AbstractJavaModel{
	private String tableName = "sample_table";
	private Connection conn= null;
	private ICompilationUnit currentUnit;			/// Current compilation unit
	public JavaModelDb() {
		
	}

	@Override
	public IXReference createXReference(String xref) {
		return JavaXReference.fromString(xref);
	}

	@Override
	public void _restore(IPath path) {
		try {
//			tableName=path.removeLastSegments(2).lastSegment().replace("-", "_");
//			if(conn==null||conn.isClosed())connect();
			if(conn==null||conn.isClosed())connect(path); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void _store(IPath path) {
		
//			try {
//				if(conn!=null&&!conn.isClosed())shutdown(conn);
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	}

	@SuppressWarnings("unused")
	private void connect() {
		    IWorkspace workspace = ResourcesPlugin.getWorkspace();
		    IWorkspaceRoot workspaceRoot = workspace.getRoot();
		    IPath workspacedir = new Path(Platform.getLocation().toString()+"/"+workspaceRoot.getName()+"/JavaModleDb_file");
		    connect(workspacedir);
	}
	
	private void connect(IPath workspacedir) {
		try {
			
			
			connect("jdbc:hsqldb:"+workspacedir.toString()+"; shutdown=true", "sa", "");
		}
		catch(Exception e) {
		    System.out.println(e.toString());
		}
	}
	
	private void connect(String connectionString, String user, String password) {
		try {
		    Class.forName("org.hsqldb.jdbcDriver");
		    conn = DriverManager.getConnection(connectionString, user, password);
		    update("CREATE TABLE if not exists "+tableName+" ( id INTEGER IDENTITY, CompilationUnitKey VARCHAR(1024), xref VARCHAR(1024))",  conn);
		    clearCommon();
			clearAdded();
			clearRemoved();
		}
		catch(Exception e) {
		    System.out.println(e.toString());
		}
	}

	
	
	@Override
	public void _updateListener(ImplementationChangeListener listener) {
		
		try {
				
			query("SELECT CompilationUnitKey, xref FROM "+tableName+" order by CompilationUnitKey " , conn, new IResutlSetDelegate() {
			    @Override
			    public int invoke(ResultSet rs,Object... args) throws SQLException{
			    	LinkedList<String> added = new LinkedList<String>();
			    	if(args.length!=2) return-1;
			    	ImplementationChangeListener listener= (ImplementationChangeListener)args[0];
			    	IXReferenceFactory jModelDb = (IXReferenceFactory)args[1];
			        String key ="";
			        while(rs.next()) {
			        			
							String currentKey = rs.getString("CompilationUnitKey");
							if(!key.equals(currentKey)){
								key=currentKey;
								try {
									JavaCore.create(key).getResource().deleteMarkers(defaults.MARKER_TYPE_OLD, true, IResource.DEPTH_INFINITE);
								} catch (CoreException ex) {}
							}
							added.add(rs.getString("xref"));
			        }
			            
			            listener.implementationChangeEvent(new ImplementationChangeDelta(jModelDb, new String[0], added.toArray(new String[added.size()]), new String[0]));
			        
			        return 0;  //To change body of implemented methods use File | Settings | File Templates.
			    }
			},listener,this);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void addXReference(int type, IJavaElement source, IJavaElement target, int line, int offset, int length) {
		if (currentUnit == null)
			throw new IllegalStateException("No current compilation unit!");
		
		// Create a string representing the cross reference.
		String xref = (new JavaXReference(type, source, target, line, offset, length)).toString();
		
		
		
		// If the new reference was present in the old file it is a common reference,
		// otherwise it is new and should be added. The remaining ones are removed references.
		
		
		try {
			boolean common = false;
			query("select xref from removed where xref = '"+xref+"'" , conn, new IResutlSetDelegate(){

				@Override
				public int invoke(ResultSet rs, Object... args)
						throws SQLException {
					if(args.length!=1 || !(args[0] instanceof Boolean)) return -1;
					args[0]=rs.getFetchSize()>0;
					return 0;
				}
				
			},common);
			if(common)update("insert into common values ('"+xref+"')",conn);
			else {	
				update("insert into added values ('"+xref+"')",conn);
				update("Insert into "+tableName+" ( CompilationUnitKey, xref) values ('"+currentUnit.getHandleIdentifier()+"' , '"+xref+"')",  conn);
			}

			update("Delete from removed where xref = '"+xref+"'",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Start processing a compilation unit (this unit becomes current).
	 * 
	 * @throws IllegalStateException if there is already a current unit
	 */
	public void beginUnit(ICompilationUnit unit) {
		if (currentUnit != null)
			throw new IllegalStateException("Already processing a compilation unit!");
		currentUnit = unit;
		createRemovedforUnit(unit);		
	}
	
	/**
	 * End processing current compilation unit.
	 */
	public void endUnit() {
		// Fire the property change signal.
		fireModelChange(new ImplementationChangeDelta(this,
				  getXrefs("common"),
				  getXrefs("added"),
				  getXrefs("removed")));
		
		// Removed all references and put the new ones.
		try {
			update("Delete from "+tableName+"  where CompilationUnitKey='"+currentUnit.getHandleIdentifier()+"' AND xref in (select xref from removed)",  conn);				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clearUnit();

	}
	
	public void clearUnit() {
		clearAdded();
		clearCommon();
		clearRemoved();
		currentUnit = null;
	}

	private void clearAdded(){
		try {
			update("DROP TABLE if exists added",  conn);
			update("CREATE TABLE if not exists added (xref VARCHAR(1024))",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void clearRemoved(){
		try {
			update("DROP TABLE if exists removed",  conn);
			update("CREATE TABLE if not exists removed (xref VARCHAR(1024))",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	
	
	private void createRemovedforUnit(ICompilationUnit unit){
		try {
			clearRemoved();
			update("insert into removed ( SELECT xref FROM "+tableName+" where CompilationUnitKey = '" +unit.getHandleIdentifier()+"'"+" )",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void clearCommon(){
		try {
			update("DROP TABLE if exists common",  conn);
			update("CREATE TABLE if not exists common (xref VARCHAR(1024))",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private String[] getXrefs(String table){
		LinkedList<String> result = new LinkedList<String>();
		try {
			
			query("SELECT xref FROM "+table+" " , conn, new IResutlSetDelegate() {
			    @SuppressWarnings("unchecked")
				@Override
			    public int invoke(ResultSet rs,Object... args) throws SQLException{
			    	if(args.length!=1) return-1;
			    	LinkedList<String> rm = (LinkedList<String>)args[0];
			        while(rs.next()) {
							rm.add(rs.getString("xref"));
			        }
			        
			        return 0;  //To change body of implemented methods use File | Settings | File Templates.
			    }
			},result);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.toArray(new String[result.size()]);
	}
	
	
	
	
	
	//--------------------------------------------------db helper functions-----------------------------------------------------------------
	
	public static interface IResutlSetDelegate{
        public int invoke(ResultSet rs,Object... args)throws SQLException;
    }
	
	//use for SQL command SELECT
    public static synchronized int query(String expression, Connection conn,IResutlSetDelegate delegate,Object... args ) throws SQLException {
        Statement st = conn.createStatement();
        int result = delegate.invoke(st.executeQuery(expression),args);
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

    public static void shutdown(Connection conn) throws SQLException {

        Statement st = conn.createStatement();
        st.execute("SHUTDOWN");
        conn.close();    // if there are no other open connection
    }

}
