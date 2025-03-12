package org.zero.dis1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.zero.dis1.entity.Reserved;
import org.zero.dis1.entity.Trip;
import org.zero.dis1.entity.User;
import org.zero.dis1.repository.JdbcTripRepository;
import org.zero.dis1.repository.JdbcUserRepository;
import org.zero.dis1.service.TripService;
import org.zero.dis1.utils.Table;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class HelloApplication extends Application {
    static double width = 650;
    static double height = 500;
    static int updateTrip = 5;
    static int user = 1;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        VBox root = new VBox();
        HBox req = new HBox();
        VBox table = new VBox();

        updateTables(table, 0L);

        Button update = new Button("Update");
        update.setOnMouseClicked(event -> {
            updateTables(table, 0L);
        });

        Button retrieveWithJdbc = new Button();
        retrieveWithJdbc.setText("Retrieve with JDBC");
        retrieveWithJdbc.setOnMouseClicked(event -> {
            var tripService = new TripService();
            var spentTime = tripService.reserveTripJdbc(updateTrip, user);
            updateTables(table, spentTime);
        });

        Button retrieveWithVertx = new Button();
        retrieveWithVertx.setText("Retrieve with Vertx");
        retrieveWithVertx.setOnMouseClicked(event -> {
            var tripService = new TripService();
            var spentTime = tripService.reserveTripVertx(updateTrip, user);
            updateTables(table, spentTime);
        });

        req.getChildren().addAll(retrieveWithJdbc, retrieveWithVertx, update);
        req.setSpacing(10);

        root.getChildren().addAll(req, table);


        Scene scene = new Scene(root, width, height);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private void updateTables(VBox table, Long spent) {
        var tripRepository = new JdbcTripRepository();
        var userRepository = new JdbcUserRepository();
        var trips = tripRepository.getTrip("select * from trip");
        var users = userRepository.getUsers("select * from users");
        drawTables(trips, users, table, 0L);
    }


    private void drawTables(List<Trip> trips, List<User> users, VBox root, Long spent) {

        Text text = new Text();
        text.setText("Completed in -> " + (spent / 1_000_000) + "ms");

        Text errorText = new Text();
        errorText.setText("Something went wrong.");

        if (trips.isEmpty() || users.isEmpty()) {
            Platform.runLater(() -> {
                root.getChildren().clear();
                root.getChildren().addAll(text, errorText);
            });

            return;
        }

        var table = new Table<Trip>(trips);
        table.setHeight(height);
        table.setWidth(width);

        var table2 = new Table<>(users);
        table2.setHeight(height);
        table2.setWidth(width);

        Platform.runLater(() -> {
            root.getChildren().clear();
            root.getChildren().addAll(text, table.getTable(), table2.getTable());
        });
    }

    public static void main(String[] args) {
        launch();
    }
}