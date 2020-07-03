package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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

    public void initData(Image img){
        ORIG.setImage(img);
        BLUR.setImage(null);
        CANY.setImage(null);
        MASK.setImage(null);
    }

    public void saveMask(ActionEvent actionEvent) {

    }

    public void loadMask(ActionEvent actionEvent) {
        
    }
}
