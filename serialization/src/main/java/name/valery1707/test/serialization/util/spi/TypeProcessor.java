package name.valery1707.test.serialization.util.spi;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface TypeProcessor<T> {
	boolean canProcess(Class<?> type);

	void write(Writer dst, T value) throws IOException;

	T read(Reader src) throws IOException;
}
