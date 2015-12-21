package name.valery1707.test.serialization.app;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import name.valery1707.test.serialization.app.dto.TestTreeItem;
import name.valery1707.test.serialization.util.Serializer;

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
		tree.setCellFactory(item -> new TextFieldTreeCell<>(TestTreeItem.TreeStringConverter.INSTANCE));
		pane.getItems().add(new VBox(actions, tree));
		//endregion

		//region Serialization result
		presentation = new TextArea();
		presentation.setText(sampleTree);
		pane.getItems().add(presentation);
		//endregion

		primaryStage.setTitle("Serializer test app");
		primaryStage.setScene(new Scene(pane, 800, 600));
		primaryStage.show();
		deserialize.fire();
	}

	private void serialize(ActionEvent event) {

	}

	private void deserialize(ActionEvent event) {
		try {
			TestTreeItem root = serializer.readValue(presentation.getText(), TestTreeItem.class);
			tree.setRoot(root.toTreeItem());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
