package name.valery1707.test.serialization.util;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializerTest {

	private Serializer serializer;

	@Before
	public void setUp() throws Exception {
		serializer = new Serializer();
	}

	private <T> void test(String message, T value) {
		test(message, value, value.getClass());
	}

	private <T> void test(String message, T value, Class<? extends T> clazz) {
		T dst;
		String result;

		result = serializer.writeValueAsString(value);
		dst = serializer.readValue(result, clazz);
		assertThat(result)
				.describedAs("write(%s).length()", message)
				.isNotNull();
		assertThat(dst)
				.describedAs("read(%s)", message)
				.isEqualTo(value);
	}

	@Test
	public void testNull() throws Exception {
		test("null", null, SimpleEntity.class);
	}

	@Test
	@Ignore
	public void testSimpleEntity() throws Exception {
		test("SimpleEntity(null, null)", new SimpleEntity(null, null));
	}
}
