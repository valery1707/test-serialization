package name.valery1707.test.serialization.app;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(10);
		pane.setVgap(10);
		pane.setPadding(new Insets(25, 25, 25, 25));

		Scene scene = new Scene(pane, 800, 600);

		//region Actions
		Button serialize = new Button("Сериализовать");
		serialize.setOnAction(this::serialize);
		Button deserialize = new Button("Десериализовать");
		deserialize.setOnAction(this::deserialize);
		HBox actions = new HBox(serialize, deserialize);
		pane.add(actions, 0, 0);
		//endregion

		//region Tree
		tree = new TreeView<>();
		tree.setShowRoot(true);
		pane.add(tree, 0, 1);
		//endregion

		//region Serialization result
		presentation = new TextArea();
		presentation.setText(sampleTree);
		pane.add(presentation, 1, 0, 1, 2);
		//endregion

		primaryStage.setTitle("Serializer test app");
		primaryStage.setScene(scene);
		primaryStage.setScene(scene);
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
