package dabomstew.rta;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BufferUtils {

	public static ByteBuffer loadByteBufferFromFile(String filename) throws IOException {
		byte[] byteArray = FileFunctions.readFileFullyIntoBuffer(filename);
		ByteBuffer res = ByteBuffer.allocateDirect(byteArray.length).order(ByteOrder.nativeOrder());
		for(int i = 0; i < byteArray.length; i++) {
			res.put(byteArray[i]);
		}
		return res;
	}
}