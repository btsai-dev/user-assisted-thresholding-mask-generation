package sample;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
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
    Tab InstructionsTab, ApplyBlurTab, ApplyCannyTab, ApplyMaskTab;
    @FXML
    AnchorPane ApplyBlurAnchorPane, ApplyCannyAnchorPane;
    @FXML
    Slider BilateralFilteringFilterSize, BilateralFilteringFilterSigma, CannyThreshold01, CannyThreshold02;
    @FXML
    Label BilateralFilteringFilterSizeLabel, BilateralFilteringFilterSigmaLabel, CannyThreshold01Label, CannyThreshold02Label;
    @FXML
    Button LoadBlur, ResetBlur, LoadCanny, ResetCanny;
    @FXML
    ImageView BlurImage, CannyImage;


    private static File imgFile;
    private static Mat orig, gray, blur, canny, mask;

    public void initData(Image img, File file){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Tie ImageView sizes to AnchorPane sizes
        BlurImage.fitHeightProperty().bind(ApplyBlurAnchorPane.heightProperty());
        BlurImage.fitWidthProperty().bind(ApplyBlurAnchorPane.heightProperty());
        CannyImage.fitHeightProperty().bind(ApplyCannyAnchorPane.heightProperty());
        CannyImage.fitWidthProperty().bind(ApplyCannyAnchorPane.heightProperty());

        // Tie Label values to slider values
        BilateralFilteringFilterSizeLabel.textProperty().bind(
                Bindings.format(
                        "FILTER SIZE: %.0f",
                        BilateralFilteringFilterSize.valueProperty()
                )
        );
        BilateralFilteringFilterSigmaLabel.textProperty().bind(
                Bindings.format(
                        "FILTER SIGMA: %.0f",
                        BilateralFilteringFilterSigma.valueProperty()
                )
        );
        CannyThreshold01Label.textProperty().bind(
                Bindings.format(
                        "THRESHOLD 01 VALUE: %.0f",
                        CannyThreshold01.valueProperty()
                )
        );
        CannyThreshold02Label.textProperty().bind(
                Bindings.format(
                        "THRESHOLD 02 VALUE: %.0f",
                        CannyThreshold02.valueProperty()
                )
        );

        // Disable and enable proper tabs
        InstructionsTab.setDisable(false);
        ApplyBlurTab.setDisable(false);
        ApplyCannyTab.setDisable(true);
        ApplyMaskTab.setDisable(true);

        // Load in images
        BlurImage.setImage(img);
        CannyImage.setImage(null);

        // Load in generic Image Mat
        orig = new Mat();
        gray = new Mat();
        blur = new Mat();
        canny = new Mat();
        mask = new Mat();
        imgFile = file;
        orig = Imgcodecs.imread(DisplayController.imgFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        resetBlur();
        /**
        IMGDISP.setAlignment(Pos.CENTER);
        ORIG.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        ORIG.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
        BLUR.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        BLUR.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
        CANY.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        CANY.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
        MASK.fitHeightProperty().bind(IMGDISP.heightProperty().subtract(MARGIN).divide(ROWS));
        MASK.fitWidthProperty().bind(IMGDISP.widthProperty().subtract(MARGIN).divide(COLS));
         */
    }

    public void loadBlur(){
        // Shut down buttons
        LoadBlur.setDisable(true);
        ResetBlur.setDisable(true);
        // Shut down tabs
        ApplyCannyTab.setDisable(true);
        ApplyMaskTab.setDisable(true);

        new Thread(){
            public void run() {
                System.out.println("Executing Blur Thread.");
                int filterSize = (int) BilateralFilteringFilterSize.getValue();
                int filterSigma = (int) BilateralFilteringFilterSigma.getValue();

                if (orig.empty()) {
                    System.out.println("Error opening image");
                    System.out.println(imgFile.getAbsolutePath());
                    return;
                }
                Imgproc.cvtColor(orig, gray, Imgproc.COLOR_BGR2GRAY);

                Imgproc.bilateralFilter(gray, blur, filterSize, filterSigma, filterSigma);
                Image blurImage = getJavaFXImage(blur);

                Platform.runLater(() -> {
                    System.out.println("Updating Blur UI.");
                    BlurImage.setImage(blurImage);
                    CannyImage.setImage(blurImage);
                    LoadBlur.setDisable(false);
                    ResetBlur.setDisable(false);
                    ApplyCannyTab.setDisable(false);
                });
            }
        }.start();
    }

    public void resetBlur(){
        LoadBlur.setDisable(true);
        ResetBlur.setDisable(true);
        ApplyCannyTab.setDisable(true);
        ApplyMaskTab.setDisable(true);
        new Thread(() -> {
            System.out.println("Resetting Blur Parameters.");
            int filterSize = 5;
            int filterSigma = 75;

            Platform.runLater(new Runnable() {
                public void run() {
                    BlurImage.setImage(getJavaFXImage(orig));
                    BilateralFilteringFilterSize.setValue(filterSize);
                    BilateralFilteringFilterSigma.setValue(filterSigma);
                    LoadBlur.setDisable(false);
                    ResetBlur.setDisable(false);
                }
            });
        }).start();
    }
    public void resetCanny(){
        // Shut down buttons
        LoadCanny.setDisable(true);
        ResetCanny.setDisable(true);
        // Shut down tabs
        ApplyBlurTab.setDisable(true);
        ApplyMaskTab.setDisable(true);
        new Thread(() -> {
            double upperThresh = Imgproc.threshold(blur, new Mat(), 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            double lowerThresh = upperThresh / 2;
            Platform.runLater(new Runnable() {
                public void run() {
                    System.out.println("Updating Canny UI.");
                    CannyImage.setImage(getJavaFXImage(blur));
                    CannyThreshold01.setValue(upperThresh);
                    CannyThreshold02.setValue(lowerThresh);
                    LoadCanny.setDisable(false);
                    ResetCanny.setDisable(false);
                    ApplyBlurTab.setDisable(false);
                }
            });
        }).start();
    }

    public void loadCanny(){
        // Shut down buttons
        LoadCanny.setDisable(true);
        ResetCanny.setDisable(true);
        // Shut down tabs
        ApplyBlurTab.setDisable(true);
        ApplyMaskTab.setDisable(true);

        new Thread(() -> {
            int threshold01 = (int) CannyThreshold01.getValue();
            int threshold02 = (int) CannyThreshold02.getValue();

            int threshHigh = Math.max(threshold01, threshold02);
            int threshLow = Math.min(threshold01, threshold02);

            int kernelSize = 3;

            Imgproc.Canny(blur, canny, threshLow, threshHigh, kernelSize, true);
            canny.convertTo(canny, CvType.CV_8UC1);
            Image cannyImage = getJavaFXImage(canny);

            Platform.runLater(() -> {
                System.out.println("Updating Canny UI.");
                CannyImage.setImage(cannyImage);
                LoadCanny.setDisable(false);
                ResetCanny.setDisable(false);
                ApplyBlurTab.setDisable(false);
                ApplyMaskTab.setDisable(false);
            });
        }).start();
    }

    /**
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
     */

    private Image getJavaFXImage(Mat mat){
        MatOfByte matByte = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, matByte);
        return new Image(new ByteArrayInputStream(matByte.toArray()));
    }
}
