package name.valery1707.test.serialization.util.spi;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface TypeProcessor<T> {
	boolean canProcess(Class<?> type);

	void write(Writer dst, T value) throws IOException;

	<R extends T> R read(Reader src, Class<R> type) throws IOException;
}
