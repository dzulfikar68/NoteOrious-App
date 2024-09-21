package io.github.dzulfikar68.noteoriousapp

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Stage

class EditController {
    @FXML
    private lateinit var textField: TextField

    private var editedText: String? = null

    fun setInitialText(text: String) {
        textField.text = text
    }

    @FXML
    fun onOkButtonClicked(actionEvent: ActionEvent) {
        editedText = textField.text

        val stage = textField.scene.window as Stage
        stage.close()
    }

    fun getEditedText(): String? {
        return editedText
    }
}