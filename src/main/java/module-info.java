module com.unit8.unit8 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.json;

    opens com.unit8.unit8 to javafx.fxml;
    exports com.unit8.unit8;
}