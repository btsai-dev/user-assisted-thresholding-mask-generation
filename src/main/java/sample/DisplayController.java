package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
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
    Button LOADBLUR;

    @FXML
    Button LOADCANY;

    @FXML
    Button LOADMASK;

    @FXML
    Button SAVE;

    @FXML
    GridPane IMGDISP;

    @FXML
    Button INTERRUPT;

    private static File imgFile;
    private static final int ROWS = 2;
    private static final int COLS = 2;
    private static final int MARGIN = 10;
    private static Mat orig;
    private static Mat gray;
    private static Mat blur;
    private static Mat cany;
    private static Mat mask;
    private static String stage = "NONE";
    private static Thread worker;

    public void initData(Image img, File file){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        orig = new Mat();
        gray = new Mat();
        blur = new Mat();
        cany = new Mat();
        mask = new Mat();

        SAVE.setDisable(true);
        ORIG.setImage(img);
        BLUR.setImage(null);
        CANY.setImage(null);
        MASK.setImage(null);
        IMGDISP.setAlignment(Pos.CENTER);
        ORIG.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        ORIG.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
        BLUR.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        BLUR.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
        CANY.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        CANY.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
        MASK.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        MASK.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
        imgFile = file;
    }

    public void saveMask(ActionEvent actionEvent) {

    }

    public void interrupt(ActionEvent actionEvent) {
        System.out.println("Interrupted Thread");
        if (stage.equals("NONE")){
            System.out.println("No stage detected.");
            return;
        }
        try {
            worker.interrupt();
        } catch(Exception e){
            System.out.println("Error when stopping Thread.");
            e.printStackTrace();
        }
        switch (stage){
            case "BLUR":
                disableAllButtons();
                LOADBLUR.setDisable(false);
                break;
            case "CANY":
                disableAllButtons();
                LOADBLUR.setDisable(false);
                LOADCANY.setDisable(false);
                break;
            case "MASK":
                disableAllButtons();
                LOADBLUR.setDisable(false);
                LOADCANY.setDisable(false);
                LOADMASK.setDisable(false);
                break;
        }
    }

    public void loadBlur(ActionEvent actionEvent) {
        disableAllButtons();
        worker = new Thread(){
            public void run() {
                System.out.println("Executing Blur Thread.");
                int filterSize = (int) ThreshSize.getValue();
                int filterSigma = (int) ThreshSigma.getValue();

                orig = Imgcodecs.imread(DisplayController.imgFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
                if (orig.empty()) {
                    System.out.println("Error opening image");
                    System.out.println(imgFile.getAbsolutePath());
                    return;
                }
                Imgproc.cvtColor(orig, gray, Imgproc.COLOR_BGR2GRAY);

                Imgproc.bilateralFilter(gray, blur, filterSize, filterSigma, filterSigma);
                MatOfByte blurMatByte = new MatOfByte();
                Imgcodecs.imencode(".bmp", blur, blurMatByte);
                Image blurImage = new Image(new ByteArrayInputStream(blurMatByte.toArray()));

                Platform.runLater(new Runnable() {
                    public void run() {
                        System.out.println("Updating Blur UI.");
                        BLUR.setImage(blurImage);
                        INTERRUPT.setDisable(true);
                        LOADBLUR.setDisable(false);
                        LOADCANY.setDisable(false);
                        stage = "NONE";
                    }
                });
            }
        };
        worker.start();
        stage = "BLUR";
        INTERRUPT.setDisable(false);
    }


    public void loadCany(ActionEvent actionEvent) {
        disableAllButtons();
        worker = new Thread(){
            public void run() {
                System.out.println("Executing Canny Thread.");

                int threshold001 = (int) Thresh001.getValue();
                int threshold002 = (int) Thresh002.getValue();
                int threshHigh = Math.max(threshold001, threshold002);
                int threshLow = Math.min(threshold001, threshold002);
                int kernelSize = 3;

                Imgproc.Canny(blur, cany, threshLow, threshHigh, kernelSize, true);
                cany.convertTo(cany, CvType.CV_8UC1);
                MatOfByte canyMatByte = new MatOfByte();
                Imgcodecs.imencode(".bmp", cany, canyMatByte);
                Image canyImage = new Image(new ByteArrayInputStream(canyMatByte.toArray()));

                Platform.runLater(new Runnable() {
                    public void run() {
                        System.out.println("Updating Canny UI.");
                        CANY.setImage(canyImage);
                        INTERRUPT.setDisable(true);
                        LOADBLUR.setDisable(false);
                        LOADCANY.setDisable(false);
                        LOADMASK.setDisable(false);
                        stage = "NONE";
                    }
                });
            }
        };
        worker.start();
        stage = "CANY";
        INTERRUPT.setDisable(false);
    }

    private void disableAllButtons() {
        SAVE.setDisable(true);
        LOADBLUR.setDisable(true);
        LOADCANY.setDisable(true);
        LOADMASK.setDisable(true);
        INTERRUPT.setDisable(true);
    }

    public void loadMask(ActionEvent actionEvent) {
        disableAllButtons();
        worker = new Thread(){
            @Override
            public void run(){
                System.out.println("Executing Mask Thread.");
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                // Imgproc.findContours(cany, contours, new Mat(), Imgproc.RETR_FLOODFILL, Imgproc.CHAIN_APPROX_SIMPLE);
                Imgproc.findContours(cany, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                mask = new Mat(orig.size(), orig.type(), Scalar.all(0));

                System.out.printf("Starting contour loop. Expect %d iterations.\n", contours.size());
                for (int i = 0; i < contours.size(); i++) {
                    if (Thread.interrupted()) {
                        System.out.println("Interrupted.");
                        return;
                    }
                    System.out.println("Contour execute.");
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
                        MASK.setImage(maskImage);
                        INTERRUPT.setDisable(true);
                        LOADBLUR.setDisable(false);
                        LOADCANY.setDisable(false);
                        LOADMASK.setDisable(false);
                        SAVE.setDisable(false);
                        stage = "NONE";
                    }
                });
            }
        };
        worker.start();
        stage = "MASK";
        INTERRUPT.setDisable(false);
    }
}
