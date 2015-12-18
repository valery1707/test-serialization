package name.valery1707.test.serialization.util;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializerTest {

	private Serializer serializer;

	@Before
	public void setUp() throws Exception {
		serializer = new Serializer();
	}

	private <T> void test(T value) throws Exception {
		test(value.toString(), value);
	}

	private <T> void test(String message, T value) throws Exception {
		test(message, value, value.getClass());
	}

	private <T> void test(String message, T value, Class<? extends T> clazz) throws Exception {
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
	public void testSimpleEntity_empty() throws Exception {
		test(new SimpleEntity(null, null));
	}

	@Test
	public void testSimpleEntity_str1() throws Exception {
		test(new SimpleEntity("123", null));
	}

	@Test
	public void testSimpleEntity_str_escapeQuote() throws Exception {
		test(new SimpleEntity("123\"321", null));
	}

	@Test
	public void testSimpleEntity_str_escapeEscape() throws Exception {
		test(new SimpleEntity("123\\321", null));
	}
}
