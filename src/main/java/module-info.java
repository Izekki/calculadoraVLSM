module com.personalprojects.calculadoravlsm {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens VlsmApp to javafx.fxml;
    exports VlsmApp;
}