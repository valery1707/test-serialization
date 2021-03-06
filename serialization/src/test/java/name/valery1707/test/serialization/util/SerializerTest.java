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
	public void testString() throws Exception {
		assertThat(serializer.writeValueAsString("123"))
				.isEqualTo("\"123\"");
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

	@Test
	public void testSimpleEntity_int1() throws Exception {
		test(new SimpleEntity(null, 10));
	}

	private SimpleTree makeSimpleTree1() {
		return new SimpleTree("root"
				, new SimpleTree("root.1", new SimpleTree("root.1.1"), new SimpleTree("root.1.2"))
				, new SimpleTree("root.2", new SimpleTree("root.2.1"), new SimpleTree("root.2.2"))
		);
	}

	private String json(String src) {
		return src.replace('\'', '"');
	}

	@Test
	public void testSimpleTree_write() throws Exception {
		String dst = serializer.writeValueAsString(makeSimpleTree1());
		assertThat(dst).isEqualTo(json(
				"{'name':'root','children':[" +
				"{'name':'root.1','children':[{'name':'root.1.1'},{'name':'root.1.2'}]}," +
				"{'name':'root.2','children':[{'name':'root.2.1'},{'name':'root.2.2'}]}" +
				"]}"
		));
	}

	@Test
	public void testSimpleTree_read_broken() throws Exception {
		SimpleTree dst = serializer.readValue(json(
				"{'name':'root','children':[" +
				"{'name':'root.1','children_bad':[{'name':'root.1.1','children':[]},{'name':'root.1.2','children':[]}]}," +
				"{'name':'root.2','children-bad':[{'name':'root.2.1','children':[]},{'name':'root.2.2','children':[]}]}" +
				"]}"
		), SimpleTree.class);
		assertThat(dst).isEqualTo(new SimpleTree("root", new SimpleTree("root.1"), new SimpleTree("root.2")));
	}

	@Test
	public void testSimpleTree() throws Exception {
		test(makeSimpleTree1());
	}
}
