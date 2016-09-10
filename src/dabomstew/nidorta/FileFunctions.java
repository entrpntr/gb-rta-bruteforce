package dabomstew.nidorta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileFunctions {

	public static byte[] readFileFullyIntoBuffer(String filename) throws IOException {
		File fh = new File(filename);
		if (!fh.exists() || !fh.isFile() || !fh.canRead()) {
			throw new FileNotFoundException(filename);
		}
		long fileSize = fh.length();
		if (fileSize > Integer.MAX_VALUE) {
			throw new IOException(filename + " is too long to read in as a byte-array.");
		}
		FileInputStream fis = new FileInputStream(filename);
		byte[] buf = readFullyIntoBuffer(fis, (int) fileSize);
		fis.close();
		return buf;
	}

	public static byte[] readFullyIntoBuffer(InputStream in, int bytes) throws IOException {
		byte[] buf = new byte[bytes];
		readFully(in, buf, 0, bytes);
		return buf;
	}

	public static void readFully(InputStream in, byte[] buf, int offset, int length) throws IOException {
		int offs = 0, read = 0;
		while (offs < length && (read = in.read(buf, offs + offset, length - offs)) != -1) {
			offs += read;
		}
	}

	public static void writeBytesToFile(String filename, byte[] data) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		fos.write(data);
		fos.close();
	}

}
