package name.valery1707.test.serialization.util.utils;

import one.util.streamex.StreamEx;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class CombinedReaderTest {

	private String readOneByOne(Reader src) {
		StringBuilder buf = new StringBuilder();
		try {
			int i;
			while ((i = src.read()) >= 0) {
				buf.append((char) i);
			}
			src.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return buf.toString();
	}

	private String readWithBuffer(Reader src) {
		StringBuilder buf = new StringBuilder();
		char[] cbuf = new char[16];
		try {
			int len;
			while ((len = src.read(cbuf)) > 0) {
				buf.append(cbuf, 0, len);
			}
			src.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return buf.toString();
	}

	private String readWithPartialBuffer(Reader src) {
		StringBuilder buf = new StringBuilder();
		int bufSize = 32;
		int bufOff = 16;
		int buffUsableLen = bufSize - bufOff;
		char[] cbuf = new char[bufSize];
		try {
			int len;
			while ((len = src.read(cbuf, bufOff, buffUsableLen)) > 0) {
				buf.append(cbuf, bufOff, len);
			}
			src.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return buf.toString();
	}

	private void test(Function<Reader, String> reader, String... parts) {
		CombinedReader combinedReader = new CombinedReader(StreamEx.of(parts).map(StringReader::new).toArray(Reader[]::new));
		String actual = reader.apply(combinedReader);
		String expected = StreamEx.of(parts).joining();
		assertEquals(expected, actual);
	}

	@Test
	public void testEmpty() throws Exception {
		test(this::readOneByOne);
		test(this::readWithBuffer);
		test(this::readWithPartialBuffer);
	}

	@Test
	public void testEmpty_1() throws Exception {
		test(this::readOneByOne, "123");
		test(this::readWithBuffer, "123");
		test(this::readWithPartialBuffer, "123");
	}

	@Test
	public void testEmpty_2() throws Exception {
		test(this::readOneByOne, "123", "456");
		test(this::readWithBuffer, "123", "456");
		test(this::readWithPartialBuffer, "123", "456");
	}
}