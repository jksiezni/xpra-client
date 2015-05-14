package xpra.client;


/**
 * @author Jakub Księżniak
 *
 */
public interface XpraInput {

	void movePointer(int x, int y);
	
	void mouseAction(int button, boolean pressed, int x, int y);
	
	void keyboardAction(int keycode, String keyname, boolean pressed);
}
