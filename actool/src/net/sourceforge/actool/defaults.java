package net.sourceforge.actool;

public final class defaults {
    public static final String MODEL_FILE           = "model.xam";
	public static final String MARKER_TYPE 			= "net.sourceforge.actool.markers.Problem";
	public static final String MARKER_TYPE_OLD 		= "net.sourceforge.actool.markers._Problem";
	public static final String MARKER_UNMAPPED 		= "net.sourceforge.actool.markers.Unmapped";
	public static final String MODEL = "Model";
	public static final String CONNECTOR_ID = "CONNECTOR_ID";
	/**
	 * @since 0.1
	 */
	public static final String PROJECT = "PROJECT";
	/**
	 * @since 0.1
	 */
	public static final int MAX_THREADS = Runtime.getRuntime().availableProcessors()*2;
}
