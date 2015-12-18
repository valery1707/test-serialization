package name.valery1707.test.serialization.util;

import one.util.streamex.StreamEx;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;

public class Serializer {

	public <T> T readValue(InputStream src, Charset charset, Class<T> clazz) throws Exception {
		return readValue(new InputStreamReader(src, charset), clazz);
	}

	public <T> T readValue(String src, Class<T> clazz) throws Exception {
		return readValue(new StringReader(src), clazz);
	}

	public <T> T readValue(Reader src, Class<T> clazz) throws Exception {
		Map<String, Field> fields = StreamEx.of(clazz.getDeclaredFields()).toMap(Field::getName, f -> f);
		T dst = null;
		String fieldName = null;
		int i;
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			switch (c) {
				case '{'://Object start
					dst = clazz.newInstance();
					break;
				case '}'://Object end
					return dst;
				case '"'://Field name
					fieldName = intReadString(src, true);
					break;
				case ':'://Field value
					Field field = fields.get(fieldName);
					if (field != null) {
						Object value = intReadValue(src, field.getType());
						intWriteFieldValue(dst, field, value);
					} else {
						intSkipValue(src);
					}
					break;
				case ','://Next field
					fieldName = null;
			}
		}
		return dst;
	}

	private void intSkipValue(Reader src) throws IOException {
		int deepCol = 0;
		int deepObj = 0;
		int i;
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			switch (c) {
				case '['://Collection start
					deepCol++;
					break;
				case ']'://Collection end
					deepCol--;
					if (deepCol == 0) {
						return;
					}
					break;
				case '{'://Object start
					deepObj++;
				case '}'://Object end
					deepObj--;
					if (deepObj == 0) {
						return;
					}
					break;
				case '"'://String start
					intReadString(src, true);
					break;
			}
		}
	}

	private Object intReadValue(Reader src, Class<?> type) throws IOException {
		if (CharSequence.class.isAssignableFrom(type)) {
			return intReadString(src, false);
		}
		intSkipValue(src);
		return null;
	}

	private static final Pattern STR_ESCAPE_ITEMS = Pattern.compile("(\\\\|\")");

	private void intWriteString(Writer dst, CharSequence value) throws IOException {
		dst
				.append('"')
				.append(STR_ESCAPE_ITEMS.matcher(value).replaceAll("\\\\$1"))
				.append('"');
	}

	private String intReadString(Reader src, boolean alreadyStarted) throws IOException {
		StringBuilder buf = new StringBuilder();
		int i;
		boolean escaped = false;
		while ((i = src.read()) >= 0) {
			char c = (char) i;
			switch (c) {
				case '"':
					if (escaped) {
						buf.append('"');
						escaped = false;
					} else if (alreadyStarted) {
						return buf.toString();
					} else {
						alreadyStarted = true;
					}
					break;
				case '\\':
					if (escaped) {
						buf.append('\\');
						escaped = false;
					} else {
						escaped = true;
					}
					break;
				default:
					if (alreadyStarted) {
						buf.append(c);
					} else {
						throw new IllegalStateException("String is not started, but content is appeared: " + c);
					}
					break;
			}
		}
		throw new IllegalStateException("String is not ended: " + buf.toString());
	}

	public String writeValueAsString(Object src) throws IOException, IllegalAccessException {
		StringWriter dst = new StringWriter();
		writeValue(dst, src);
		return dst.toString();
	}

	public void writeValue(OutputStream dst, Charset charset, Object src) throws IOException, IllegalAccessException {
		writeValue(new OutputStreamWriter(dst, charset), src);
	}

	public void writeValue(Writer dst, Object src) throws IOException, IllegalAccessException {
		if (src != null) {
			dst.append('{');
			Class<?> clazz = src.getClass();
			for (Field field : clazz.getDeclaredFields()) {
				Object value = intReadFieldValue(src, field);
				if (value == null) {
					continue;
				}
				intWriteString(dst, field.getName());
				dst.append(':');
				if (CharSequence.class.isAssignableFrom(field.getType())) {
					intWriteString(dst, (CharSequence) value);
				} else {
					dst.append("{}");
				}
			}
			dst.append('}');
		}
	}

	private <T> void intWriteFieldValue(T dst, Field field, Object value) throws IllegalAccessException {
		if (value == null) {
			return;
		}
		boolean accessible = field.isAccessible();
		try {
			if (!accessible) {
				field.setAccessible(true);
			}
			field.set(dst, value);
		} finally {
			if (!accessible) {
				field.setAccessible(false);
			}
		}
	}

	private Object intReadFieldValue(Object src, Field field) throws IllegalAccessException {
		boolean accessible = field.isAccessible();
		try {
			if (!accessible) {
				field.setAccessible(true);
			}
			return field.get(src);
		} finally {
			if (!accessible) {
				field.setAccessible(false);
			}
		}
	}
}
