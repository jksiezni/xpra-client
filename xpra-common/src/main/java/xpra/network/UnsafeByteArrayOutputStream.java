/**
 * 
 */
package xpra.network;

import java.io.ByteArrayOutputStream;

/**
 * @author Jakub Księżniak
 *
 */
class UnsafeByteArrayOutputStream extends ByteArrayOutputStream {

	public UnsafeByteArrayOutputStream(int initialSize) {
		super(initialSize);
	}
	
	/**
	 * Returns an internal bytes data. Use {@link #size()} method to get the size of actual data in the buffer.
	 * @return a byte array
	 */
	public byte[] getBytes() {
		return buf;
	}
	
}
