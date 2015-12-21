package name.valery1707.test.serialization.app.dto;

import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestTreeItem {
	private String name;
	private List<TestTreeItem> children = new ArrayList<>();

	public TestTreeItem() {
	}

	public TestTreeItem(String name, TestTreeItem... children) {
		this();
		setName(name);
		setChildren(Arrays.asList(children));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TestTreeItem> getChildren() {
		return children;
	}

	public void setChildren(List<TestTreeItem> children) {
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

		TestTreeItem testTreeItem = (TestTreeItem) o;

		return new EqualsBuilder()
				.append(getName(), testTreeItem.getName())
				.append(getChildren(), testTreeItem.getChildren())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(getName())
				.append(getChildren())
				.toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", getName())
				.append("children", getChildren())
				.toString();
	}

	public TreeItem<TestTreeItem> toTreeItem() {
		TreeItem<TestTreeItem> item = new TreeItem<>(this);
		item.setExpanded(!getChildren().isEmpty());
		getChildren().forEach(children -> item.getChildren().add(children.toTreeItem()));
		return item;
	}
}
