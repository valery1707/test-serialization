package name.valery1707.test.serialization.util.type;

import name.valery1707.test.serialization.util.spi.TypeProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

public class StringProcessor implements TypeProcessor<CharSequence> {
	@Override
	public boolean canProcess(Class<?> type) {
		return CharSequence.class.isAssignableFrom(type);
	}

	private static final Pattern STR_ESCAPE_ITEMS = Pattern.compile("(\\\\|\")");

	@Override
	public void write(Writer dst, CharSequence value) throws IOException {
		dst
				.append('"')
				.append(STR_ESCAPE_ITEMS.matcher(value).replaceAll("\\\\$1"))
				.append('"');
	}

	@Override
	public <R extends CharSequence> R read(Reader src, Class<R> type) throws IOException {
		StringBuilder buf = new StringBuilder();
		boolean alreadyStarted = false;
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
						return wrap(buf, type);
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

	@SuppressWarnings("unchecked")
	private <R extends CharSequence> R wrap(StringBuilder buf, Class<R> type) {
		if (String.class.equals(type)) {
			return (R) buf.toString();
		} else if (CharSequence.class.equals(type)) {
			return (R) buf;
		}
		//todo Точно нужны корвертеры
		throw new IllegalStateException("Unknown target type: " + type);
	}
}
