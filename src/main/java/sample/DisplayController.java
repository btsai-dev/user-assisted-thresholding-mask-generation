package sample;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DisplayController {
    @FXML
    Tab InstructionsTab, ApplyBlurTab, ApplyCannyTab, ApplyMorphTab, ApplyMaskTab;
    @FXML
    AnchorPane ApplyBlurAnchorPane, ApplyCannyAnchorPane, ApplyMorphAnchorPane, ApplyMaskAnchorPane;
    @FXML
    Slider BilateralFilteringFilterSize, BilateralFilteringFilterSigma,
            CannyThreshold01, CannyThreshold02, CannyKernelSize,
            MorphKernelSize;
    @FXML
    Label BilateralFilteringFilterSizeLabel, BilateralFilteringFilterSigmaLabel,
            CannyThreshold01Label, CannyThreshold02Label, CannyKernelSizeLabel,
            MorphKernelSizeLabel;
    @FXML
    Button LoadBlur, ResetBlur, LoadCanny, ResetCanny, LoadMorph, ResetMorph, GenerateMask, InterruptGenerateMask, SaveMask, NextImage;
    @FXML
    ImageView BlurImage, CannyImage, MorphImage, MaskImage;


    private static File imgFile;
    private static Mat orig, blur, canny, morph, mask;
    private static Thread worker;
    public static File saveDir;

    public void initData(Image img, File file){
        nu.pattern.OpenCV.loadShared();
        // Tie ImageView sizes to AnchorPane sizes
        BlurImage.fitHeightProperty().bind(ApplyBlurAnchorPane.heightProperty());
        BlurImage.fitWidthProperty().bind(ApplyBlurAnchorPane.heightProperty());
        CannyImage.fitHeightProperty().bind(ApplyCannyAnchorPane.heightProperty());
        CannyImage.fitWidthProperty().bind(ApplyCannyAnchorPane.heightProperty());
        MorphImage.fitHeightProperty().bind(ApplyMorphAnchorPane.heightProperty());
        MorphImage.fitWidthProperty().bind(ApplyMorphAnchorPane.heightProperty());
        MaskImage.fitHeightProperty().bind(ApplyMaskAnchorPane.heightProperty());
        MaskImage.fitWidthProperty().bind(ApplyMaskAnchorPane.heightProperty());

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
        CannyKernelSizeLabel.textProperty().bind(
                Bindings.format(
                        "KERNEL SIZE: %.0f",
                        CannyKernelSize.valueProperty()
                )
        );
        MorphKernelSizeLabel.textProperty().bind(
                Bindings.format(
                        "KERNEL SIZE: %.0f",
                        MorphKernelSize.valueProperty()
                )
        );

        // Disable and enable proper tabs
        InstructionsTab.setDisable(false);
        ApplyBlurTab.setDisable(false);
        ApplyCannyTab.setDisable(true);
        ApplyMaskTab.setDisable(true);
        ApplyMorphTab.setDisable(true);
        InterruptGenerateMask.setDisable(true);
        SaveMask.setDisable(true);

        // Load in images
        BlurImage.setImage(img);
        CannyImage.setImage(null);

        // Load in generic Image Mat
        orig = new Mat();
        blur = new Mat();
        canny = new Mat();
        morph = new Mat();
        mask = new Mat();

        imgFile = file;
        orig = Imgcodecs.imread(DisplayController.imgFile.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
        worker = null;
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
        SaveMask.setDisable(true);
        // Shut down tabs
        ApplyCannyTab.setDisable(true);
        ApplyMorphTab.setDisable(true);
        ApplyMaskTab.setDisable(true);

        new Thread(() -> {
            System.out.println("Executing Blur Thread.");
            int filterSize = (int) BilateralFilteringFilterSize.getValue();
            int filterSigma = (int) BilateralFilteringFilterSigma.getValue();

            if (orig.empty()) {
                System.out.println("Error opening image");
                System.out.println(imgFile.getAbsolutePath());
                return;
            }
            // Imgproc.cvtColor(orig, gray, Imgproc.COLOR_BGR2GRAY);

            Imgproc.bilateralFilter(orig, blur, filterSize, filterSigma, filterSigma);
            blur.convertTo(blur, CvType.CV_8UC1);
            Image blurImage = getJavaFXImage(blur);

            Platform.runLater(() -> {
                System.out.println("Updating Blur UI.");
                BlurImage.setImage(blurImage);
                LoadBlur.setDisable(false);
                ResetBlur.setDisable(false);
                ApplyCannyTab.setDisable(false);
                resetCanny();
            });
        }).start();
    }

    public void resetBlur(){
        // Shut down buttons
        LoadBlur.setDisable(true);
        ResetBlur.setDisable(true);
        SaveMask.setDisable(true);
        // Shut down tabs
        ApplyCannyTab.setDisable(true);
        ApplyMorphTab.setDisable(true);
        ApplyMaskTab.setDisable(true);
        new Thread(() -> {
            System.out.println("Resetting Blur Parameters.");
            int filterSize = 5;
            int filterSigma = 75;

            Platform.runLater(() -> {
                BlurImage.setImage(getJavaFXImage(orig));
                BilateralFilteringFilterSize.setValue(filterSize);
                BilateralFilteringFilterSigma.setValue(filterSigma);

                LoadBlur.setDisable(false);
                ResetBlur.setDisable(false);
            });
        }).start();
    }
    public void resetCanny(){
        // Shut down buttons
        LoadCanny.setDisable(true);
        ResetCanny.setDisable(true);
        SaveMask.setDisable(true);
        // Shut down tabs
        ApplyBlurTab.setDisable(true);
        ApplyMorphTab.setDisable(true);
        ApplyMaskTab.setDisable(true);
        new Thread(() -> {
            int upperThresh = (int) Imgproc.threshold(blur, new Mat(), 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            int lowerThresh = upperThresh / 2;
            int kernelSize = 3;

            Platform.runLater(() -> {
                System.out.println("Updating Canny UI.");
                CannyImage.setImage(getJavaFXImage(blur));
                CannyThreshold01.setValue(upperThresh);
                CannyThreshold02.setValue(lowerThresh);
                CannyKernelSize.setValue(kernelSize);

                LoadCanny.setDisable(false);
                ResetCanny.setDisable(false);

                ApplyBlurTab.setDisable(false);
            });
        }).start();
    }

    public void resetMorph(){
        // Shut down buttons
        LoadMorph.setDisable(true);
        ResetMorph.setDisable(true);
        SaveMask.setDisable(true);
        // Shut down tabs
        ApplyBlurTab.setDisable(true);
        ApplyCannyTab.setDisable(true);
        ApplyMaskTab.setDisable(true);
        new Thread(() -> {
            int kernelSize = 5;
            Platform.runLater(() -> {
                System.out.println("Updating Canny UI.");
                MorphImage.setImage(getJavaFXImage(canny));
                MorphKernelSize.setValue(kernelSize);

                LoadMorph.setDisable(false);
                ResetMorph.setDisable(false);

                ApplyBlurTab.setDisable(false);
                ApplyCannyTab.setDisable(false);
            });
        }).start();
    }

    public void loadCanny(){
        // Shut down buttons
        LoadCanny.setDisable(true);
        ResetCanny.setDisable(true);
        SaveMask.setDisable(true);
        // Shut down tabs
        ApplyBlurTab.setDisable(true);
        ApplyMorphTab.setDisable(true);
        ApplyMaskTab.setDisable(true);

        new Thread(() -> {
            int threshold01 = (int) CannyThreshold01.getValue();
            int threshold02 = (int) CannyThreshold02.getValue();
            int kernelSize = (int) CannyKernelSize.getValue();

            int threshHigh = Math.max(threshold01, threshold02);
            int threshLow = Math.min(threshold01, threshold02);

            Imgproc.Canny(blur, canny, threshLow, threshHigh, kernelSize, true);
            canny.convertTo(canny, CvType.CV_8UC1);
            Image cannyImage = getJavaFXImage(canny);

            Platform.runLater(() -> {
                System.out.println("Updating Canny UI.");
                CannyImage.setImage(cannyImage);
                LoadCanny.setDisable(false);
                ResetCanny.setDisable(false);
                ApplyBlurTab.setDisable(false);
                ApplyMorphTab.setDisable(false);
                resetMorph();
            });
        }).start();
    }

    public void loadMorph(){
        // Shut down buttons
        LoadMorph.setDisable(true);
        ResetMorph.setDisable(true);
        SaveMask.setDisable(true);
        // Shut down tabs
        ApplyBlurTab.setDisable(true);
        ApplyCannyTab.setDisable(true);
        ApplyMaskTab.setDisable(true);

        new Thread(() -> {
            int kernelSize = (int) MorphKernelSize.getValue();
            Mat kernel = new Mat(new Size(kernelSize, kernelSize), CvType.CV_8UC1, new Scalar(255));
            Imgproc.morphologyEx(canny, morph, Imgproc.MORPH_CLOSE, kernel);
            morph.convertTo(morph, CvType.CV_8UC1);
            Image morphImage = getJavaFXImage(morph);

            Platform.runLater(() -> {
                System.out.println("Updating Canny UI.");
                MaskImage.setImage(morphImage);
                MorphImage.setImage(morphImage);

                LoadMorph.setDisable(false);
                ResetMorph.setDisable(false);
                GenerateMask.setDisable(false);

                ApplyBlurTab.setDisable(false);
                ApplyCannyTab.setDisable(false);
                ApplyMorphTab.setDisable(false);
                ApplyMaskTab.setDisable(false);
            });
        }).start();
    }

    public void generateMask(ActionEvent actionEvent) {
        // Shut down buttons
        GenerateMask.setDisable(true);
        SaveMask.setDisable(true);
        InterruptGenerateMask.setDisable(true);
        // Shut down tabs
        ApplyBlurTab.setDisable(true);
        ApplyCannyTab.setDisable(true);
        ApplyMorphTab.setDisable(true);
        worker = new Thread() {
            @Override
            public void run() {
                System.out.println("Executing Mask Thread.");
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Imgproc.findContours(morph, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                mask = new Mat(orig.size(), orig.type(), Scalar.all(0));

                System.out.printf("Starting contour loop. Expect %d iterations.\n", contours.size());
                for (int i = 0; i < contours.size(); i++) {
                    if (Thread.interrupted()) {
                        System.out.println("Interrupted.");
                        Platform.runLater(() -> {
                            System.out.println("Updating UI.");
                            InterruptGenerateMask.setDisable(true);

                            ApplyBlurTab.setDisable(false);
                            ApplyCannyTab.setDisable(false);
                            ApplyMorphTab.setDisable(false);

                            GenerateMask.setDisable(false);
                            SaveMask.setDisable(false);
                        });
                        return;
                    }
                    System.out.println("Contour execute.");
                    Imgproc.drawContours(mask, contours, i, new Scalar(255, 255, 255), -1);
                }
                System.out.println("Ending contour loop.");

                Image maskImage = getJavaFXImage(mask);

                System.out.println("Processing complete.");
                Platform.runLater(() -> {
                    System.out.println("Updating UI.");
                    InterruptGenerateMask.setDisable(true);
                    MaskImage.setImage(maskImage);

                    ApplyBlurTab.setDisable(false);
                    ApplyCannyTab.setDisable(false);
                    ApplyMorphTab.setDisable(false);

                    GenerateMask.setDisable(false);
                    SaveMask.setDisable(false);
                });
            }
        };
        worker.start();
        InterruptGenerateMask.setDisable(false);
    }

    public void saveMask(){
        try{
            System.out.printf("Trying to save mask in directory %s\n", saveDir.getAbsolutePath());
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to save the mask? It will overwrite any previous mask saves for this file!",
                    ButtonType.YES, ButtonType.CANCEL);
            alert.setTitle("Save?");
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                System.out.printf("Attempting to save.");
                Imgcodecs.imwrite(saveDir.getAbsolutePath() + "/" + imgFile.getName() + ".tif", mask);
                Alert saved = new Alert(Alert.AlertType.INFORMATION,
                        "Mask Saved!",
                        ButtonType.OK);
                saved.showAndWait();
            }
        } catch(Exception e){
            Alert saved = new Alert(Alert.AlertType.INFORMATION,
                    "Something went wrong while saving the mask.",
                    ButtonType.OK);
            e.printStackTrace();
        }
    }

    public void nextImage(){
        System.out.printf("Trying to save mask in directory %s\n", saveDir.getAbsolutePath());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to move onto the next image? Don't forget to save your mask!",
                ButtonType.YES, ButtonType.CANCEL);
        alert.setTitle("Continue?");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            Stage stage = (Stage) NextImage.getScene().getWindow();
            stage.close();
        }

    }

    public void interruptGenerateMask(){
        try{
            worker.interrupt();
        } catch(Exception e){
            System.out.println("Problem with interrupting Thread.");
        }
    }

    private Image getJavaFXImage(Mat mat){
        MatOfByte matByte = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, matByte);
        return new Image(new ByteArrayInputStream(matByte.toArray()));
    }
}
