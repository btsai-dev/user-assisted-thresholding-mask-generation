package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DisplayController {
    @FXML
    ImageView ORIG;

    @FXML
    ImageView BLUR;

    @FXML
    ImageView CANY;

    @FXML
    ImageView MASK;

    @FXML
    Slider ThreshSize;

    @FXML
    Slider ThreshSigma;

    @FXML
    Slider Thresh001;

    @FXML
    Slider Thresh002;

    @FXML
    Button LOAD;

    @FXML
    Button SAVE;

    private static File imgFile;

    public void initData(Image img, File file){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        SAVE.setDisable(true);
        ORIG.setImage(img);
        BLUR.setImage(null);
        CANY.setImage(null);
        MASK.setImage(null);

        imgFile = file;
    }

    public void saveMask(ActionEvent actionEvent) {

    }

    public void loadMask(ActionEvent actionEvent) {
        // Disable all the buttons
        SAVE.setDisable(true);
        LOAD.setDisable(true);
        new Thread(){
            public void run(){
                System.out.println("Executing thread.");
                int filterSize = (int) ThreshSize.getValue();
                int filterSigma = (int) ThreshSigma.getValue();
                int threshold001 = (int) Thresh001.getValue();
                int threshold002 = (int) Thresh002.getValue();
                int threshHigh = Math.max(threshold001, threshold002);
                int threshLow = Math.min(threshold001, threshold002);
                int kernelSize = 3;

                Mat orig = new Mat();
                Mat gray = new Mat();
                Mat blur = new Mat();
                Mat cany = new Mat();
                // Mat mask = new Mat();
                //Mat mask = new Mat(orig.size(), CvType.CV_8UC3, Scalar.all(0));


                orig = Imgcodecs.imread(DisplayController.imgFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
                if( orig.empty() ) {
                    System.out.println("Error opening image");
                    System.out.println(imgFile.getAbsolutePath());
                    return;
                }
                Imgproc.cvtColor(orig, gray, Imgproc.COLOR_BGR2GRAY);

                Imgproc.bilateralFilter(gray, blur, filterSize, filterSigma, filterSigma);
                MatOfByte blurMatByte = new MatOfByte();
                Imgcodecs.imencode(".bmp", blur, blurMatByte);
                Image blurImage = new Image(new ByteArrayInputStream(blurMatByte.toArray()));

                Imgproc.Canny(blur, cany, threshLow, threshHigh, kernelSize, false);
                cany.convertTo(cany, CvType.CV_8UC1);
                MatOfByte canyMatByte = new MatOfByte();
                Imgcodecs.imencode(".bmp", cany, canyMatByte);
                Image canyImage = new Image(new ByteArrayInputStream(canyMatByte.toArray()));

                Platform.runLater(new Runnable() {
                    public void run() {
                        System.out.println("Updating UI.");
                        BLUR.setImage(blurImage);
                        CANY.setImage(canyImage);
                    }
                });

                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                // Imgproc.findContours(cany, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.findContours(cany, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                Mat mask = new Mat(orig.size(), orig.type(), Scalar.all(0));

                System.out.printf("Starting contour loop. Expect %d iterations.\n", contours.size());
                for (int i = 0; i < contours.size(); i++) {
                    Imgproc.drawContours(mask, contours, i, new Scalar(255, 255, 255), -1);
                }
                System.out.println("Ending contour loop.");

                MatOfByte maskMatByte = new MatOfByte();
                Imgcodecs.imencode(".bmp", mask, maskMatByte);
                Image maskImage = new Image(new ByteArrayInputStream(maskMatByte.toArray()));

                System.out.println("Processing complete.");
                Platform.runLater(new Runnable() {
                    public void run() {
                        System.out.println("Updating UI.");
                        // BLUR.setImage(blurImage);
                        // CANY.setImage(canyImage);
                        MASK.setImage(maskImage);
                        LOAD.setDisable(false);
                        SAVE.setDisable(false);
                    }
                });
            }
        }.start();
    }

}
