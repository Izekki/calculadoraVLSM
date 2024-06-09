module com.personalprojects.calculadoravlsm {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens vlsm to javafx.fxml;
    exports VlsmApp;
    exports vlsm;
}