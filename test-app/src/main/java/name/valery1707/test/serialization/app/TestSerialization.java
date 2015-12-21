package name.valery1707.test.serialization.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.ZonedDateTime;

public class TestSerialization extends Application {

	public static void main(String[] args) {
		System.out.println("ZonedDateTime.now() = " + ZonedDateTime.now());
		launch(args);
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
		Button deserialize = new Button("Десериализовать");
		HBox actions = new HBox(serialize, deserialize);
		pane.add(actions, 0, 0);
		//endregion

		//region Tree
		Text tree = new Text("Тут будет дерево");
		tree.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
		pane.add(tree, 0, 1);
		//endregion

		//region Serialization result
		TextArea presentation = new TextArea();
		pane.add(presentation, 1, 0, 1, 2);
		//endregion

		primaryStage.setTitle("Serializer test app");
		primaryStage.setScene(scene);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
