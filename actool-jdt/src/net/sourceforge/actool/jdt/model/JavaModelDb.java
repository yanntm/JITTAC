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
import net.sourceforge.actool.db.*;
import net.sourceforge.actool.db.DBManager.IResutlSetDelegate;


public class JavaModelDb extends AbstractJavaModel{
	private String rootTableName = "compilationUnit_xrefs";
	
	private String removedTableName = rootTableName+"_removed";
	private String commonTableName = rootTableName+"_common";
	private String addedTableName = rootTableName+"_added";
//	
//	private String removedTableName = "removed";
//	private String commonTableName = "common";
//	private String addedTableName = "added";
	
	private Connection conn= null;
	private ICompilationUnit currentUnit;			/// Current compilation unit
	public JavaModelDb() {
		
	}

	
	@Override
	public void _restore(IPath path) {
		try {
//			rootTableName=path.removeLastSegments(2).lastSegment().replace("-", "_");
			if(conn==null||conn.isClosed())conn=DBManager.connect();
//			if(conn==null||conn.isClosed())conn=DBManager.connect(path);
			initDb();
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

	
	
	
//	private void connect(String connectionString, String user, String password) {
//		try {
//		    conn = DBManager.connect(connectionString, user, password);
//		    initDb();
//		}
//		catch(Exception e) {
//		    System.out.println(e.toString());
//		}
//	}


	private void initDb() throws SQLException {
		DBManager.update("CREATE TABLE if not exists "+rootTableName+" ( id INTEGER IDENTITY, CompilationUnitKey VARCHAR(1024), xref VARCHAR(1024))",  conn);
		clearCommon();
		clearAdded();
		clearRemoved();
	}

	
	
	@Override
	public void _updateListener(ImplementationChangeListener listener) {
		
		try {
				
			DBManager.query("SELECT CompilationUnitKey, xref FROM "+rootTableName+" order by CompilationUnitKey " , conn, new IResutlSetDelegate() {
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
			DBManager.query("select xref from "+ removedTableName+" where xref = '"+xref+"'" , conn, new IResutlSetDelegate(){

				@Override
				public int invoke(ResultSet rs, Object... args)
						throws SQLException {
					if(args.length!=1 || !(args[0] instanceof Boolean)) return -1;
					args[0]=rs.getFetchSize()>0;
					return 0;
				}
				
			},common);
			if(common)DBManager.update("insert into "+ commonTableName+" values ('"+xref+"')",conn);
			else {	
				DBManager.update("insert into "+ addedTableName+" values ('"+xref+"')",conn);
				DBManager.update("Insert into "+rootTableName+" ( CompilationUnitKey, xref) values ('"+currentUnit.getHandleIdentifier()+"' , '"+xref+"')",  conn);
			}

			DBManager.update("Delete from "+ removedTableName+" where xref = '"+xref+"'",  conn);
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
				  getXrefs(commonTableName),
				  getXrefs(addedTableName),
				  getXrefs(removedTableName)));
		
		// Removed all references and put the new ones.
		try {
			DBManager.update("Delete from "+rootTableName+"  where CompilationUnitKey='"+currentUnit.getHandleIdentifier()+"' AND xref in (select xref from "+ removedTableName+")",  conn);				
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
			DBManager.update("DROP TABLE if exists "+ addedTableName,  conn);
			DBManager.update("CREATE TABLE if not exists "+ addedTableName+" (xref VARCHAR(1024))",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void clearRemoved(){
		try {
			DBManager.update("DROP TABLE if exists "+ removedTableName,  conn);
			DBManager.update("CREATE TABLE if not exists "+ removedTableName+" (xref VARCHAR(1024))",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	
	
	private void createRemovedforUnit(ICompilationUnit unit){
		try {
			clearRemoved();
			DBManager.update("insert into "+ removedTableName+" ( SELECT distinct xref FROM "+rootTableName+" where CompilationUnitKey = '" +unit.getHandleIdentifier()+"'"+" )",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void clearCommon(){
		try {
			DBManager.update("DROP TABLE if exists "+ commonTableName,  conn);
			DBManager.update("CREATE TABLE if not exists "+ commonTableName+" (xref VARCHAR(1024))",  conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private String[] getXrefs(String table){
		LinkedList<String> result = new LinkedList<String>();
		try {
			
			DBManager.query("SELECT distinct xref FROM "+table+" " , conn, new IResutlSetDelegate() {
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
	
	
	
	
	
	
}
