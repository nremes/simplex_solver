module cz.remes.simplex_solver_gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;


    opens cz.remes.simplex_solver_gui to javafx.fxml;
    exports cz.remes.simplex_solver_gui;
    exports cz.remes.simplex_solver_gui.controller;
    exports cz.remes.simplex_solver_gui.enums;
}