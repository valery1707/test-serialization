package name.valery1707.test.serialization.util;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class EnvironmentTest {

	@Test
	public void simpleTest() {
		assertTrue(true);
	}

	@Test
	public void testAssertJ() {
		assertThat(Arrays.asList("1", "2", "3"))
				.isNotEmpty()
				.hasSize(3)
				.isSorted()
				.containsOnly("3", "1", "2")
				.doesNotContain("4")
				.extracting(Integer::valueOf)
				.doesNotContainNull()
				.isSubsetOf(Arrays.asList(1, 2, 3, 4, 5))
		;
	}
}
