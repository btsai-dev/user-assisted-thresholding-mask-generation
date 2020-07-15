package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(primaryStage);
        File saveDir = new File(directory.getAbsolutePath() + "/segmentation");
        saveDir.mkdirs();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/Display.fxml"));

        Stage stage = new Stage();
        Scene scene = new Scene(
                loader.load()
        );

        stage.setTitle("Select");
        stage.setResizable(true);
        // primaryStage.setScene(defalt);
        DisplayController controller = loader.<DisplayController>getController();

        File[] dirList = directory.listFiles();

        if (dirList == null) {
            Platform.exit();
            System.exit(0);
        }
        for (File file : dirList){
            System.out.printf("Attempting to read %s\n", file.getName());
            BufferedImage img;
            try {
                img = ImageIO.read(file);
            } catch(Exception e){
                System.out.println("Could not open file. Could be a folder.");
                continue;
            }
            if (img == null) {
                System.out.println("File is not an image.");
                continue;
            }

            Image image = SwingFXUtils.toFXImage(img, null);
            controller.saveDir = saveDir;
            controller.initData(image, file);
            stage.setScene(scene);
            stage.showAndWait();
        }
    }


    public static void main(String[] args){
        System.out.println("Version 0.1.0");
        try {
            File file = new File("C:\\Users\\godon\\Desktop\\test.log");
            PrintStream ps = new PrintStream(file);
            try {
                launch(args);
            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace(ps);
            }
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
