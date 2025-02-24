module com.pach.gsm {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.json;
    requires okhttp3;

    opens com.pach.gsm to javafx.fxml;
    exports com.pach.gsm;
    exports com.pach.gsm.controllers;
    opens com.pach.gsm.controllers to javafx.fxml;
}