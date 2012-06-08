package net.sourceforge.actool.model.da;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import net.sourceforge.actool.db.DBManager;
import net.sourceforge.actool.db.DBManager.IResutlSetDelegate;
import net.sourceforge.actool.model.ia.IXReference;
import net.sourceforge.actool.jdt.model.JavaXReference;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;



public class Connector extends ArchitectureElement {

    public static final int ABSENT          = 0;
    public static final int CONVERGENT      = 1;
    public static final int DIVERGENT       = 2;
    
    public static final String ENVISAGED    = "ENVISAGED";
    public static final String STATE        = "STATE";
    public static final String SOURCE       = "SOURCE";
    public static final String TARGET       = "TARGET";
    public static final String COMMENT      = "COMMENT";
    public static final String XREFERENCES  = "XREFERENCES";
    public static final String TABLE_NAME = "connector_xref_mapping";
    
    private static Connection dbConn;  
    public static final IPropertyDescriptor[] propertyDescriptors = new IPropertyDescriptor[]  {
        new PropertyDescriptor(STATE, "Type"),
        new PropertyDescriptor(SOURCE, "Source"),
        new PropertyDescriptor(TARGET, "Target"),
        new TextPropertyDescriptor(COMMENT, "Comment")
    };


    private Component source, target;
    private boolean envisaged = true, connected = false;
    private String comment = "";

//    private LinkedList<IXReference> xrefs = new LinkedList<IXReference>();
    
    
    protected Connector(Component source, Component target) {
    	if (source == null || target == null || target.equals(source))
        	throw new IllegalArgumentException();
        construct(source, target, true);
    }

    protected Connector(Component source, Component target, boolean connect) {
    	if (source == null || target == null || target.equals(source))
        	throw new IllegalArgumentException();
        construct(source, target, connect);
    }

    private void construct(Component source, Component target, boolean connect) {   	
    	try {
			initDb();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	this.source = source;
        this.target = target;
        if (connect)
            connect();
    }
    
    public static Connector connect(Component source, Component target, boolean envisaged) {      
        Connector conn =  new Connector(source, target);
        conn.setEnvisaged(envisaged);
        return conn;
    }
 
    public boolean isEnvisaged() {
        return envisaged;
    }
    
    public void setEnvisaged(boolean envisaged) {
        if (isEnvisaged() == envisaged)
            return;

        this.envisaged = envisaged;
        firePropertyChange(ENVISAGED, !envisaged, envisaged);
    }

    public boolean isConnected() {
        return connected;
    }
    
    public void connect() {
        assert connected = false;
        getSource().addSourceConnector(this);
        getTarget().addTargetConnector(this);
        connected = true;
    }

    public void disconnect() {
        assert connected == true;
        getSource().removeSourceConnector(this);
        getTarget().removeTargetConnector(this);
        connected = false;
    }
    
    public Component getSource() {
        return source;
    }

    public Component getTarget() {
        return target;
    }
    
    public int getState() {
        if (isEnvisaged()) {
            if (getXrefcount()==0)
                return ABSENT;
            else
                return CONVERGENT;
        } else
            return DIVERGENT;
    }
    
   

	public boolean inState(int state) {
        if (state < ABSENT || state > DIVERGENT)
            throw new IllegalArgumentException();
        return getState() == state;
    }
    
    public ArchitectureModel getModel() {
        if (!source.getModel().equals(target.getModel()))
            throw new IllegalStateException("Connector can not connect components from different models!");
        return source.getModel();
    }
    
    public boolean hasXReferences() {
        return getXrefcount()>0;
    }

    public int getNumXReferences() {
        return getXrefcount();
    }
  
    public void addXReference(IXReference xref) {
    	storeXref(xref);     
        firePropertyChange(XREFERENCES,  null, xref);
    }

	public void addXReferences(Collection<IXReference> xrefs) {
    	for (IXReference xref: xrefs)
    		addXReference(xref);
    }
    
    public void removeXReference(IXReference xref) {
        deleteXref(xref);
        firePropertyChange(XREFERENCES, xref, null);
    }

	public Collection<IXReference> getXReferences() {
		return retriveXrefs();
    }
   
   

	public void accept(IArchitectureModelVisitor visitor) {
    	visitor.visit(this);
    }
    
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return propertyDescriptors;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (id.equals(STATE)) {
            switch (getState()) {
            case ABSENT:
                return "Absence";
            case CONVERGENT:
                return "Convergence";
            case DIVERGENT:
                return "Divergence";
            }
        } else if (id.equals(SOURCE)) {
            return source.getName();
        } else if (id.equals(TARGET)) {
            return target.getName();
        } else if (id.equals(COMMENT)) {
            return comment;
        }
        
        return null;
    }

    @Override
    public boolean isPropertySet(Object id) {
        if (id.equals(STATE) || id.equals(SOURCE) || id.equals(TARGET)) {
            return true;
        } else if (id.equals(COMMENT)) {
            return comment != null && !comment.isEmpty();
        }
        return false;
    }

    @Override
    public void resetPropertyValue(Object id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (id.equals(COMMENT))
            comment = (String) value;
    }
    
    public String toString() {
        return source.toString() + "->" + target.toString();
    }
    
    private int getXrefcount() {
    	
    	int[] result= new int[]{0};
    	try {
			DBManager.query("select count(xref) from "+TABLE_NAME+" where connector_id= '" + this.toString()+"'" , dbConn, new IResutlSetDelegate(){

				@Override
				public int invoke(ResultSet rs, Object... args) throws SQLException {
					if(args.length!=1) return -1;
					if(rs.next())
					((int[])args[0])[0]=rs.getInt(1);
					return 0;
				}
				
			},result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return result[0];
    	
    	
    	
		
	}
    private void deleteXref(IXReference xref) {
    	if(xref instanceof JavaXReference) deleteXref((JavaXReference)xref);
	    else throw new RuntimeException("deleting xrefs for this language is not implimented");	
	}
    
    private void deleteXref(JavaXReference xref) {
    	try {
			DBManager.update("delete from "+TABLE_NAME+" where xref='"+xref.toString()+"' and connector_id='"+this.toString()+"'",dbConn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    private void storeXref(IXReference xref) {
    	    if(xref instanceof JavaXReference) storeXref((JavaXReference)xref);
    	    else throw new RuntimeException("storeing xrefs for this language is not implimented");	
	}
    
    private void storeXref(JavaXReference xref) {
    	try {
			DBManager.update("insert into "+TABLE_NAME+" values ('"+xref.toString()+"' , '"+this.toString()+"' , '"+ JavaXReference.class.getName()+"' )",dbConn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    private Collection<IXReference> retriveXrefs() {
		LinkedList<IXReference> result= new LinkedList<IXReference>();
		try {
			DBManager.query("select distinct xref , type_name from "+TABLE_NAME+" where connector_id= '" + this.toString()+"'" , dbConn, new IResutlSetDelegate(){

				@Override
				public int invoke(ResultSet rs, Object... args) throws SQLException {
					if(args.length!=1||!(args[0] instanceof LinkedList<?>)) return -1;
					LinkedList<IXReference> result= (LinkedList<IXReference>) args[0];
					while(rs.next())result.add(createXref(rs.getString("xref"), rs.getString("type_name")));
					return 0;
				}
				
				private IXReference createXref(String xref,String typeName){
					if(typeName.equals(JavaXReference.class.getName())) return JavaXReference.fromString(xref);
					return null;
				}
				
			},result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
    
    public boolean containsXref(IXReference xref)
    { 
    	boolean[] result= new boolean[]{ false};
    	if(xref instanceof JavaXReference){
	    	try {
				DBManager.query("select count(xref)>0 as found from "+TABLE_NAME+" where connector_id= '" + this.toString()+"' and xref='"+((JavaXReference)xref).toString()+"'" , dbConn, new IResutlSetDelegate(){
	
					@Override
					public int invoke(ResultSet rs, Object... args) throws SQLException {
						if(args.length!=1) return -1;
						if(rs.next())
						((boolean[])args[0])[0]=rs.getBoolean("found");
						return 0;
					}
					
				},result);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	return result[0]; 
    }
    private void initDb() throws SQLException {
    	dbConn = DBManager.connect();
//		DBManager.update("CREATE TABLE if not exists "+TABLE_NAME+" ( xref_id INTEGER NOT NULL, connector_id VARCHAR(128) NOT NULL, FOREIGN KEY (xref_id) REFERENCES compilationUnit_xrefs(id)) ",  dbConn);
    	DBManager.update("CREATE TABLE if not exists "+TABLE_NAME+" ( xref VARCHAR(1024) NOT NULL, connector_id VARCHAR(128) NOT NULL,type_name VARCHAR(128) NOT NULL )",  dbConn);
	}
}
