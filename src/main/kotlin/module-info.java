module io.github.dzulfikar68.noteoriousapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens io.github.dzulfikar68.noteoriousapp to javafx.fxml;
    exports io.github.dzulfikar68.noteoriousapp;
}