package xpra.network.chunks;

import java.io.IOException;
import java.io.InputStream;

import xpra.network.XpraConnector;

/**
 * @author Jakub Księżniak
 *
 */
public interface StreamChunk {
	
	StreamChunk readChunk(InputStream is, XpraConnector connector) throws IOException;
}