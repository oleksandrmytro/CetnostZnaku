import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Optional;
import javafx.scene.input.KeyCode;


public class Main extends Application {

    private PieChart pieChart;
    private ListView<String> stringListView;
    private ObservableList<String> stringObservableList;
    private ObservableList<PieChart.Data> pieChartData;
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        stage.setOnCloseRequest(windowEvent -> {
            closeRequest(windowEvent);
        });

        BorderPane borderPane = new BorderPane();

        HBox mainLayoutHBox = new HBox();

        pieChartData = FXCollections.observableArrayList();
        stringObservableList = FXCollections.observableArrayList();
        stringObservableList.addListener((InvalidationListener) change -> {
            refillChart();
        });

        stringListView = new ListView<>(stringObservableList);
        stringListView.setMinWidth(300);
        stringListView.setPrefWidth(300);
        stringListView.setMaxWidth(300);

        stringListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                deleteEntry();
            }
        });

        pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Četnost znaků");
        pieChart.setLegendSide(Side.RIGHT);
        pieChart.setBorder(new Border(new BorderStroke(Color.rgb(0, 112, 192), BorderStrokeStyle.SOLID, null, new BorderWidths(3))));
        pieChart.setAnimated(true);

        mainLayoutHBox.getChildren().addAll(stringListView, pieChart);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        borderPane.setCenter(mainLayoutHBox);

        HBox buttonsHBox = new HBox(10);
        buttonsHBox.setPadding(new Insets(10));
        buttonsHBox.setBackground(new Background(new BackgroundFill(Color.rgb(211, 211, 211), null, null)));
        buttonsHBox.setAlignment(Pos.CENTER);

        Button endButton = new Button("Konec");
        endButton.setOnAction( actionEvent -> {
            closeRequest(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        Button addButton = new Button("Přidej");
        addButton.setOnAction(actionEvent -> {
            addEntry();
        });

        Button deleteButton = new Button("Odeber");
        deleteButton.setOnAction(actionEvent -> {
            deleteEntry();
        });

        Button deleteAllButton = new Button("Odeber vše");
        deleteAllButton.setOnAction(actionEvent -> {
            deleteAllEntries();
        });

        Button saveButton = new Button("Ulož");
        saveButton.setOnAction(actionEvent -> {
            save();
        });

        Button loadButton = new Button("Načti");
        loadButton.setOnAction(actionEvent -> {
            load();
        });

        buttonsHBox.getChildren().addAll(endButton, addButton, deleteButton, deleteAllButton, saveButton, loadButton);
        borderPane.setBottom(buttonsHBox);

        Scene scene = new Scene(borderPane, 600, 400);
        stage.setTitle("Četnost znaků a..z, A..Z bez diakritiky");
        stage.setScene(scene);
        stage.show();
    }

    private void closeRequest(WindowEvent windowEvent){
        Alert endAlert = new Alert(Alert.AlertType.CONFIRMATION);
        endAlert.setTitle("Konec");
        endAlert.setContentText("Opravdu chcete aplikaci ukončit?");
        Optional<ButtonType> result = endAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK){
            Platform.exit();
        }
        else {
            windowEvent.consume();
        }
    }

    private void refillChart() {
        HashMap<Character, Integer> map = new HashMap<>();
        for (String line : stringObservableList){
            for (int i = 0; i < line.length(); i++){
                char currentChar = line.charAt(i);
                boolean found = false;
                for (char keyInMap : map.keySet()){
                    if (keyInMap == Character.toUpperCase(currentChar)){
                        map.put(Character.toUpperCase(currentChar), map.get(Character.toUpperCase(currentChar)) + 1);
                        found = true;
                        break;
                    }
                }
                if (!found){
                    map.put(Character.toUpperCase(currentChar), 1);
                }

            }
        }

        pieChartData.clear();

        for (char currentChar : map.keySet()){
            PieChart.Data data = new PieChart.Data(String.valueOf(currentChar) + "(" + map.get(currentChar) + ")", map.get(currentChar));
            pieChartData.add(data);
        }

    }

    private void load() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("textovy format", "*.txt"),
                new FileChooser.ExtensionFilter("Vsechny soubory", "*.*")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null){
            try{
                stringObservableList.clear();
                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    String line = fileReader.readLine();
                    while (line != null){
                        stringObservableList.add(line);
                        line = fileReader.readLine();
                    }
                }

            } catch (IOException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Chyba nacteni");
                alert.setContentText("Chyba nacteni souboru " + file.getName());
                alert.showAndWait();
            }
        }


    }

    private void save() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("textovy format", "*.txt"),
                new FileChooser.ExtensionFilter("Vsechny soubory", "*.*")
        );
        File file = fileChooser.showSaveDialog(stage);
        if (file != null){
            try{
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String line : stringObservableList){
                        writer.write(line);
                        writer.newLine();
                    }
                }

            } catch (IOException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Chyba zapisu do souboru");
                alert.setContentText("Chyba zapisu do souboru souboru " + file.getName());
                alert.showAndWait();
            }
        }
    }

    private void deleteAllEntries() {
        stringObservableList.clear();
        stringListView.getSelectionModel().clearSelection();
    }

    private void deleteEntry() {
        String toDelete = stringListView.getSelectionModel().getSelectedItem();
        if (toDelete != null){
            stringObservableList.remove(toDelete);
            refillChart();
        }
        stringListView.getSelectionModel().clearSelection();
    }

    private void addEntry() {
        TextInputDialog textInputDialog = new TextInputDialog("");
        textInputDialog.setHeaderText("Zadani noveho textu");
        textInputDialog.setContentText("Novy text");
        Optional<String> result = textInputDialog.showAndWait();
        if (result.isPresent()){
            String trimmedResult = result.get().trim();
            if (!trimmedResult.isEmpty()){
                stringObservableList.add(trimmedResult);
                refillChart();
            }
        }

    }


    public static void main(String[] args) {
        Application.launch(args);
    }
}

