package io.github.dzulfikar68.noteoriousapp

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter

class HelloController {
    @FXML
    private lateinit var welcomeText: Label

    @FXML
    private lateinit var noteListView: ListView<String>

    @FXML
    private lateinit var noteInput: TextField

    private val notes = FXCollections.observableArrayList<String>()

    @FXML
    private fun onHelloButtonClick() {
        welcomeText.text = "Welcome to JavaFX Application!"
    }

    @FXML
    fun initialize() {
        noteListView.items = notes
    }

    @FXML
    fun handleAddNote() {
        val note = noteInput.text
        if (note.isNotBlank()) {
            notes.add(note)
            noteInput.clear()
        }
    }

    @FXML
    fun handleDeleteNote() {
        val selectedNote = noteListView.selectionModel.selectedItem
        notes.remove(selectedNote)
    }

    @FXML
    fun handleImportNotes() {
        val fileChooser = FileChooser()
        fileChooser.title = "Open CSV File"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("CSV Files", "*.csv"))
        val file = fileChooser.showOpenDialog(noteListView.scene.window as Stage) ?: return

        BufferedReader(FileReader(file)).use { reader ->
            notes.clear()
            reader.lineSequence().forEach { line ->
                notes.add(line)
            }
        }
    }

    @FXML
    fun handleExportNotes() {
        saveNotes(false)
    }

    @FXML
    fun handleDeleteAllNote() {
        notes.removeAll(noteListView.items)
    }

    @FXML
    fun handleMergeNotes() {
        saveNotes(true)
    }

    private fun saveNotes(append: Boolean) {
        val fileChooser = FileChooser()
        fileChooser.title = "Export Notes"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("CSV Files", "*.csv"))
        val file = fileChooser.showSaveDialog(noteListView.scene.window as Stage) ?: return

        FileWriter(file, append).use { writer ->
            notes.forEach { note ->
                writer.write("$note\n")
            }
        }
    }
}