package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


//juhu! ja sam mali paradajz! jesi2
//TODO sklepati ovo pls

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GlavniProzor.fxml"));		
			Parent root =  loader.load();
			Scene scene = new Scene(root,900,600);
			primaryStage.setScene(scene);
			//primaryStage.setTitle("Muzički plejer - DJ Rajko");
			primaryStage.initStyle(StageStyle.UNIFIED);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
