package name.valery1707.test.serialization.util.utils;

import java.io.IOException;
import java.io.Reader;

public class CombinedReader extends Reader {
	private final Reader[] parts;
	private int partIndex = 0;

	public CombinedReader(Reader... parts) {
		this.parts = parts;
	}

	private boolean eof() {
		return partIndex >= parts.length;
	}

	@Override
	public int read(char[] buf, int off, int len) throws IOException {
		//todo Чтение проводить внутри блока synchronized (lock)
		if (eof()) {
			return -1;
		}
		int readLen = 0;
		//пока ещё (не всё прочитано) и (есть где читать)
		while (readLen < len && !eof()) {
			int partLen = parts[partIndex].read(buf, off + readLen, len - readLen);
			if (partLen < 0) {
				partIndex++;
			} else {
				readLen += partLen;
			}
		}
		return eof() && readLen == 0 ? -1 : readLen;
	}

	@Override
	public void close() throws IOException {
		//todo Catch intermediate exceptions
		for (Reader r : parts) {
			r.close();
		}
	}
}
