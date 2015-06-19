/**
 * 
 */
package xpra.protocol;

/**
 * @author Jakub Księżniak
 *
 */
public enum PictureEncoding {
	jpeg,
	png,
	pngP("png/P"),
	pngL("png/L");

	private static final PictureEncoding[] values = values();
	private final String code;
	
	private PictureEncoding() {
		code = name();
		
	}
	
	private PictureEncoding(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		return code;
	}
	
	public static PictureEncoding decode(String code) {
		for(PictureEncoding encoding : values) {
			if(encoding.code.equals(code)) {
				return encoding;
			}
		}
		return null;
	}

	public static String[] toString(PictureEncoding[] encodings) {
		final String[] array = new String[encodings.length];
		for(int i = 0; i < encodings.length; ++i) {
			array[i] = encodings[i].toString();
		}
		return array;
	}

}
