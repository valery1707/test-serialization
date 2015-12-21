package name.valery1707.test.serialization.app;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import name.valery1707.test.serialization.app.dto.TestTreeItem;
import name.valery1707.test.serialization.util.Serializer;

import java.io.IOException;
import java.time.ZonedDateTime;

public class TestSerialization extends Application {
	private static final String sampleTree = (
			"{'name':'root','children':[" +
			"{'name':'root.1','children':[{'name':'root.1.1'},{'name':'root.1.2'}]}," +
			"{'name':'root.2','children':[{'name':'root.2.1'},{'name':'root.2.2'}]}" +
			"]}"
	).replace('\'', '"');

	private final Serializer serializer;

	private TreeView<TestTreeItem> tree;
	private TextArea presentation;
	private Label exception;

	public static void main(String[] args) {
		System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now());
		launch(args);
	}

	public TestSerialization() {
		serializer = new Serializer();
	}

	@Override
	public void start(Stage primaryStage) {
		SplitPane pane = new SplitPane();
		pane.setPadding(new Insets(25, 25, 25, 25));

		//region Actions
		Button serialize = new Button("Сериализовать");
		serialize.setOnAction(this::serialize);
		Button deserialize = new Button("Десериализовать");
		deserialize.setOnAction(this::deserialize);
		HBox actions = new HBox(serialize, deserialize);
		//endregion

		//region Tree
		tree = new TreeView<>();
		tree.setShowRoot(true);
		tree.setEditable(true);
		tree.setCellFactory(item -> new TextFieldTreeCell<>(TestTreeItem.TreeStringConverter.INSTANCE));
		tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		ContextMenu contextMenuFull = new ContextMenu(treeAddMenu(), treeDelMenu());
		ContextMenu contextMenuAdd = new ContextMenu(treeAddMenu());
		tree.setContextMenu(contextMenuFull);
		tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null || newValue.getParent() == null) {
				tree.setContextMenu(contextMenuAdd);
			} else {
				tree.setContextMenu(contextMenuFull);
			}
		});
		exception = new Label();
		exception.setTextFill(Color.RED);
		pane.getItems().add(new VBox(actions, tree, exception));
		//endregion

		//region Serialization result
		presentation = new TextArea();
		presentation.setText(sampleTree);
		presentation.setWrapText(true);
		pane.getItems().add(presentation);
		//endregion

		primaryStage.setTitle("Serializer test app");
		primaryStage.setScene(new Scene(pane, 800, 600));
		primaryStage.show();
		deserialize.fire();
	}

	private MenuItem treeAddMenu() {
		MenuItem treeAdd = new MenuItem("+");
		treeAdd.setOnAction(this::treeAddAction);
		return treeAdd;
	}

	private void treeAddAction(ActionEvent event) {
		TreeItem<TestTreeItem> selectedItem = tree.getSelectionModel().getSelectedItem();
		selectedItem.getChildren().add(new TestTreeItem("new").toTreeItem());
		selectedItem.setExpanded(true);
	}

	private MenuItem treeDelMenu() {
		MenuItem treeDel = new MenuItem("-");
		treeDel.setOnAction(this::treeDelAction);
		return treeDel;
	}

	@SuppressWarnings("unchecked")
	private void treeDelAction(ActionEvent event) {
		TreeItem<TestTreeItem> selectedItem = tree.getSelectionModel().getSelectedItem();
		selectedItem.getParent().getChildren().removeAll(selectedItem);
	}

	private void serialize(ActionEvent event) {
		exception.setText("");
		try {
			TestTreeItem root = TestTreeItem.fromTreeItem(tree.getRoot());
			presentation.setText(serializer.writeValueAsString(root));
		} catch (IOException | IllegalAccessException e) {
			exception.setText(e.getMessage());
			e.printStackTrace();
		}
	}

	private void deserialize(ActionEvent event) {
		exception.setText("");
		try {
			TestTreeItem root = serializer.readValue(presentation.getText(), TestTreeItem.class);
			tree.setRoot(root.toTreeItem());
		} catch (Exception e) {
			exception.setText(e.getMessage());
			e.printStackTrace();
		}
	}
}
