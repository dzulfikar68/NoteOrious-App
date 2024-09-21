package io.github.dzulfikar68.noteoriousapp

import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DragEvent
import javafx.scene.input.KeyCode
import javafx.scene.input.TransferMode
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter

class HelloController {
//    @FXML
//    private lateinit var welcomeText: Label

    @FXML
    private lateinit var noteListView: ListView<String>

    @FXML
    private lateinit var noteInput: TextField

    private var selectedIndex = -1

    private val notes = FXCollections.observableArrayList<String>()

//    @FXML
//    private fun onHelloButtonClick() {
//        welcomeText.text = "Welcome to JavaFX Application!"
//    }

    @FXML
    fun initialize() {
        noteListView.items = notes

        noteInput.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER){
                handleAddNote()
            }
        }

        noteListView.setOnMouseClicked { event ->
            if (event.clickCount == 2) {
                handleEditNote()
            }
        }

        noteListView.setOnKeyPressed { event ->
            if (event.code == KeyCode.BACK_SPACE) {
                val selectedItem = noteListView.selectionModel.selectedItem
                if (selectedItem != null) {
                    notes.remove(selectedItem)
                }
            }
        }

        // Mengatur cell factory untuk ListView, memungkinkan drag and drop reorder
        noteListView.cellFactory = Callback {
            object : ListCell<String>() {
                init {

                    // Deteksi ketika drag dimulai
                    setOnDragDetected { event ->
                        if (item == null) return@setOnDragDetected

                        // Memulai drag event
                        val dragBoard = startDragAndDrop(TransferMode.MOVE)
                        val content = ClipboardContent()
                        content.putString(item)
                        dragBoard.setContent(content)

                        event.consume()
                    }

                    // Mengatur area ketika item sedang di-drag di atas item lain
                    setOnDragOver { event: DragEvent ->
                        if (event.gestureSource !== this && event.dragboard.hasString()) {
                            event.acceptTransferModes(TransferMode.MOVE)
                        }
                        event.consume()
                    }

                    // Mengatur drop event, yaitu ketika item di-drop di posisi baru
                    setOnDragDropped { event ->
                        val dragBoard = event.dragboard
                        val success = if (dragBoard.hasString()) {
                            val draggedItem = dragBoard.string
                            val droppedIndex = index

                            // Mengambil index dari item yang sedang di-drag
                            val draggedIndex = listView.items.indexOf(draggedItem)

                            // Mengubah posisi item yang di-drag
                            listView.items.removeAt(draggedIndex)
                            listView.items.add(droppedIndex, draggedItem)

//                            success = true
                            true
                        } else {
                            false
                        }
                        event.isDropCompleted = success
                        event.consume()
                    }

                    // Ketika drag selesai, reset cell state
                    setOnDragDone { event ->
                        event.consume()
                    }
                }

                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) null else item
                }
            }
        }

        // Mengatur cell factory untuk mengatur tampilan teks dengan ellipsis
//        noteListView.cellFactory = Callback<ListView<String>, ListCell<String>> {
//            object : ListCell<String>() {
//                private val label = Label()
//
//                override fun updateItem(item: String?, empty: Boolean) {
//                    super.updateItem(item, empty)
//                    if (empty || item == null) {
//                        text = null
//                        graphic = null
//                    } else {
//                        // Mengatur teks dengan panjang maksimal satu baris, menggunakan ellipsis
//                        label.text = item
//                        label.isWrapText = false
//                        label.maxWidth = 100.0  // Lebar maksimal dari cell
//                        label.style = "-fx-text-overrun: ellipsis;"  // Menambahkan ellipsis
//                        graphic = label
//                    }
//                }
//            }
//        }
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
        if (selectedNote != null) {
            notes.remove(selectedNote)
        }
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
        if (noteListView.items.isNotEmpty()) {
            notes.removeAll(noteListView.items)
        }
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

    @FXML
    fun handleEditNote() {
        selectedIndex = noteListView.selectionModel.selectedIndex
        if (selectedIndex >= 0) {
            try {
                val loader = FXMLLoader(EditController::class.java.getResource("edit-window.fxml"))
                val root = loader.load<Parent>()

                val controller = loader.getController<EditController>()
                controller.setInitialText(noteListView.items[selectedIndex])

                val stage = Stage()
                stage.title = "Edit Item"
                stage.initModality(Modality.WINDOW_MODAL)
                stage.initOwner(noteListView.scene.window)
                stage.scene = Scene(root)
                stage.showAndWait()

                val newText = controller.getEditedText()
                if (newText != null) {
                    noteListView.items[selectedIndex] = newText
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val alert = Alert(Alert.AlertType.INFORMATION, "Please select an item to edit.", ButtonType.OK)
            alert.showAndWait()
        }
    }

    @FXML
    fun handleCopyNote() {
        val textToCopy = noteListView.selectionModel.selectedItem
        if (!textToCopy.isNullOrEmpty()) {
            // Membuat ClipboardContent untuk menyalin teks ke clipboard
            val clipboard = Clipboard.getSystemClipboard()
            val content = ClipboardContent()
            content.putString(textToCopy)

            // Menyalin teks ke clipboard
            clipboard.setContent(content)

            val alert = Alert(Alert.AlertType.INFORMATION, "Text copied to clipboard: $textToCopy", ButtonType.OK)
            alert.showAndWait()
        } else {
            val alert = Alert(Alert.AlertType.INFORMATION, "Text field is empty. Nothing to copy", ButtonType.OK)
            alert.showAndWait()
        }
    }
}