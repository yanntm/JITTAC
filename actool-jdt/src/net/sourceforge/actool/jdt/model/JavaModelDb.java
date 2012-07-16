package net.sourceforge.actool.jdt.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;


import net.sourceforge.actool.defaults;
import net.sourceforge.actool.model.ia.IXReferenceFactory;
import net.sourceforge.actool.model.ia.ImplementationChangeDelta;
import net.sourceforge.actool.model.ia.ImplementationChangeListener;
import net.sourceforge.actool.db.*;
import net.sourceforge.actool.db.DBManager.IResultSetDelegate;


/**
 * @since 0.2
 */
public class JavaModelDb extends AbstractJavaModel{
	private String rootTableName = "compilationUnit_xrefs";
	
	private String removedTableName = rootTableName+"_removed";
	private String commonTableName = rootTableName+"_common";
	private String addedTableName = rootTableName+"_added";
	private boolean initDb = true;
	private ICompilationUnit currentUnit;			/// Current compilation unit
	public JavaModelDb() {
		
	}

	
	@Override
	public void _restore(IPath path) {
		try {
			initDb();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void _store(IPath path) {

	}

	
	
	


	private void initDb() throws SQLException {
		if(!initDb)return;
		DBManager.preparedUpdate("CREATE TABLE if not exists "+rootTableName+" ( id INTEGER IDENTITY, CompilationUnitKey VARCHAR(1024), xref VARCHAR(1024))");
		clearCommon();
		clearAdded();
		clearRemoved();
		initDb=false;
	}

	
	
	@Override
	public void _updateListener(ImplementationChangeListener listener) {
		try {
			LinkedList<String> compilationUnits = new LinkedList<String>();
			DBManager.preparedQuery("SELECT distinct CompilationUnitKey, xref FROM "+rootTableName, new IResultSetDelegate(){

				@Override
				public int invoke(ResultSet rs, Object... args)
						throws SQLException {
					if(args.length!=1 || !(args[0] instanceof LinkedList<?>)) return -1;
					@SuppressWarnings("unchecked")
					LinkedList<String> result =((LinkedList<String>)args[0]);
					while(rs.next())
					result.add(rs.getString("CompilationUnitKey"));
					return 0;
				}
				
			},compilationUnits);
			Iterator<String> iter = compilationUnits.iterator();
			String current="";
			while(iter.hasNext()){
				DBManager.preparedQuery("SELECT distinct xref FROM "+rootTableName+" where CompilationUnitKey= ?" , new Object[]{(current=iter.next())}, new IResultSetDelegate() {
				    @Override
				    public int invoke(ResultSet rs,Object... args) throws SQLException{
				    	LinkedList<String> added = new LinkedList<String>();
				    	if(args.length!=3) return-1;
				    	ImplementationChangeListener listener= (ImplementationChangeListener)args[0];
				    	IXReferenceFactory jModelDb = (IXReferenceFactory)args[1];
				        String key =(String)args[2];
				        try {
							JavaCore.create(key).getResource().deleteMarkers(defaults.MARKER_TYPE_OLD, true, IResource.DEPTH_INFINITE);
						} catch (CoreException ex) {}
				        while(rs.next()){	
								added.add(rs.getString("xref"));
				        }
				        if(added.size()!=0)listener.implementationChangeEvent(new ImplementationChangeDelta(jModelDb, new String[0], added.toArray(new String[added.size()]), new String[0]));
				        return 0;  //To change body of implemented methods use File | Settings | File Templates.
				    }
				},listener,this,current);
			}
			
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
			boolean[] common= new boolean[]{false};
			DBManager.preparedQuery("select count(xref) as rowcount from "+ removedTableName+" where xref = ?",new Object[]{xref} , new IResultSetDelegate(){

				@Override
				public int invoke(ResultSet rs, Object... args)
						throws SQLException {
					if(args.length!=1 ) return -1;
					if(rs.next())
					((boolean[])args[0])[0]=rs.getInt("rowcount")>0;
					return 0;
				}
				
			},common);
			if(common[0])
				DBManager.preparedUpdate("insert into "+ commonTableName+" values (?)",new Object[]{xref});
			else {	
				DBManager.preparedUpdate("insert into "+ addedTableName+" values (?)",new Object[]{xref});
				DBManager.preparedUpdate("Insert into "+rootTableName+" ( CompilationUnitKey, xref) values (? , ?)",new Object[]{currentUnit.getHandleIdentifier(),xref});
			}

			DBManager.preparedUpdate("Delete from "+ removedTableName+" where xref = ?",new Object[]{xref});
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
			DBManager.preparedUpdate("Delete from "+rootTableName+"  where CompilationUnitKey=? AND xref in (select xref from "+ removedTableName+")",new Object[]{currentUnit.getHandleIdentifier()});				
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
			DBManager.preparedUpdate("DROP TABLE if exists "+ addedTableName);
			DBManager.preparedUpdate("CREATE TABLE if not exists "+ addedTableName+" (xref VARCHAR(1024))");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void clearRemoved(){
		try {
			DBManager.preparedUpdate("DROP TABLE if exists "+ removedTableName);
			DBManager.preparedUpdate("CREATE TABLE if not exists "+ removedTableName+" (xref VARCHAR(1024))");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	
	
	private void createRemovedforUnit(ICompilationUnit unit){
		try {
			clearRemoved();
			DBManager.preparedUpdate("insert into "+ removedTableName+" ( SELECT distinct xref FROM "+rootTableName+" where CompilationUnitKey = ?)",new Object[]{unit.getHandleIdentifier()}/*,  conn*/);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private void clearCommon(){
		try {
			DBManager.preparedUpdate("DROP TABLE if exists "+ commonTableName/*,  conn*/);
			DBManager.preparedUpdate("CREATE TABLE if not exists "+ commonTableName+" (xref VARCHAR(1024))"/*,  conn*/);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	private String[] getXrefs(String table){
		LinkedList<String> result = new LinkedList<String>();
		try {
			DBManager.preparedQuery("SELECT distinct xref FROM "+table, new IResultSetDelegate() {
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
