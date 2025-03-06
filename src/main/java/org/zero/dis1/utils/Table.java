package org.zero.dis1.utils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;

import java.util.List;

public class Table<T> {
    private final ObservableList<T> list = FXCollections.observableArrayList();
    @Getter
    private final TableView<T> table;


    public Table(List<T> elements) {
        table = new TableView<>(list);

        var fields = elements.getFirst().getClass().getDeclaredFields();

        for (var field : fields) {
            field.setAccessible(true);
            var name = field.getName();

            TableColumn<T, ?> column = new TableColumn<>(name);
            column.setCellValueFactory(new PropertyValueFactory<>(name));
            table.getColumns().add(column);
        }
        refill(elements);
    }

    public void refill(List<T> vocabularies) {
        Platform.runLater(() -> {
            list.setAll(vocabularies);
        });
    }

    public void setWidth(double width) {
        table.setPrefWidth(width);
    }

    public void setHeight(double height) {
        table.setPrefHeight(height);
    }
}
