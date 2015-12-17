package name.valery1707.test.serialization.util;

import java.io.*;
import java.nio.charset.Charset;

public class Serializer {

	public <T> T readValue(InputStream src, Charset charset, Class<T> clazz) {
		return readValue(new InputStreamReader(src, charset), clazz);
	}

	public <T> T readValue(String src, Class<T> clazz) {
		return readValue(new StringReader(src), clazz);
	}

	public <T> T readValue(Reader src, Class<T> clazz) {
		//todo Implement
		return null;
	}

	public String writeValueAsString(Object src) {
		StringWriter dst = new StringWriter();
		writeValue(dst, src);
		return dst.toString();
	}

	public void writeValue(OutputStream dst, Charset charset, Object src) {
		writeValue(new OutputStreamWriter(dst, charset), src);
	}

	public void writeValue(Writer dst, Object src) {
		//todo Implement
	}
}
