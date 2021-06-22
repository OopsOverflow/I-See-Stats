package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.parser.ParserException;

public class AlertBaker extends Alert {

    private AlertBaker(AlertType alertType, String title, String header, String context) {
        super(alertType);
        this.setTitle(title);
        this.setContentText(context);
        this.setHeaderText(header);
    }

    private AlertBaker(AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
    }

    public static void bakeError(ParserException.Type bread){
        AlertBaker alertBaker = new AlertBaker(AlertType.ERROR, bread.name(),
                "Error Patrol: We Caught an Error", ParserException.getMessage(bread));
        alertBaker.showAndWait();
    }

}
