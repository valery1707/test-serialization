package name.valery1707.test.serialization.app.dto;

import javafx.scene.control.TreeItem;
import javafx.util.StringConverter;
import one.util.streamex.StreamEx;
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

	public static TestTreeItem fromTreeItem(TreeItem<TestTreeItem> root) {
		TestTreeItem item = root.getValue();
		item.setChildren(
				StreamEx.of(root.getChildren())
						.map(TestTreeItem::fromTreeItem)
						.toList()
		);
		return item;
	}

	public static class TreeStringConverter extends StringConverter<TestTreeItem> {
		public static final TreeStringConverter INSTANCE = new TreeStringConverter();

		@Override
		public String toString(TestTreeItem object) {
			return object != null ? object.getName() : null;
		}

		@Override
		public TestTreeItem fromString(String string) {
			return string != null ? new TestTreeItem(string) : null;
		}
	}
}
