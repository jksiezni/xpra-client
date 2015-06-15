/**
 * 
 */
package xpra.protocol;

/**
 * @author Jakub Księżniak
 *
 */
public enum PictureEncoding {
	unknown,
	png,
	jpeg;
	
	public static PictureEncoding valueOfSafe(String name) {
		try {
			return valueOf(name);
		} catch(IllegalArgumentException e) {
			return unknown;
		}
	}
}
