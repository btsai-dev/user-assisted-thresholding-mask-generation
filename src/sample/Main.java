package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(primaryStage);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(
                        "Display.fxml"
                )
        );
        primaryStage.setScene(
                new Scene(
                        (Pane) loader.load()
                )
        );
        primaryStage.setResizable(true);
        primaryStage.show();
        DisplayController controller = loader.<DisplayController>getController();

        File[] dirList = directory.listFiles();

        if (dirList == null) {
            Platform.exit();
            System.exit(0);
        }
        for (File file : dirList){
            BufferedImage img;
            try {
                img = ImageIO.read(file);
            } catch(Exception e){
                System.out.println("Could not opeen file.");
                continue;
            }
            if (img == null) {
                System.out.println("File is not an image.");
                continue;
            }
            Image image = SwingFXUtils.toFXImage(img, null);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
