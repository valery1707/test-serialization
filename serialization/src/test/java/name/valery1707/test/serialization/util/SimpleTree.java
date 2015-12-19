package name.valery1707.test.serialization.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Arrays;
import java.util.List;

public class SimpleTree {
	private String name;
	private SimpleTree parent;
	private List<SimpleTree> children;

	public SimpleTree() {
	}

	public SimpleTree(String name, SimpleTree... children) {
		this();
		setName(name);
		setChildren(Arrays.asList(children));
//		this.children.forEach(child -> child.setParent(this));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SimpleTree getParent() {
		return parent;
	}

	public void setParent(SimpleTree parent) {
		this.parent = parent;
	}

	public List<SimpleTree> getChildren() {
		return children;
	}

	public void setChildren(List<SimpleTree> children) {
		this.children = children;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SimpleTree that = (SimpleTree) o;

		return new EqualsBuilder()
				.append(getName(), that.getName())
				.append(getParent(), that.getParent())
				.append(getChildren(), that.getChildren())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(getName())
				.append(getParent())
				.append(getChildren())
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", name)
				.append("children", children)
				.toString();
	}
}
