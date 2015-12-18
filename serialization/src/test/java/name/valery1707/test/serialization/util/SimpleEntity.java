package name.valery1707.test.serialization.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SimpleEntity {
	private String name;
	private Integer integer;

	public SimpleEntity(String name, Integer integer) {
		setName(name);
		setInteger(integer);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getInteger() {
		return integer;
	}

	public void setInteger(Integer integer) {
		this.integer = integer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SimpleEntity that = (SimpleEntity) o;

		return new EqualsBuilder()
				.append(getName(), that.getName())
				.append(getInteger(), that.getInteger())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(getName())
				.append(getInteger())
				.toHashCode();
	}
}
