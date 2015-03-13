package net.sourceforge.actool.ui.editor.model;

public interface Visibility {

	public static final int INVISIBLE = 0;
	public static final int VISIBLE = 1;
	public static final int FADED = 2;

	public abstract int getVisibility();
	public abstract void setVisibility(int visibility);
}