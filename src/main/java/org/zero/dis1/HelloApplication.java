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
import org.zero.dis1.entity.Trip;
import org.zero.dis1.model.TripRepository;
import org.zero.dis1.repository.JdbcTripRepository;
import org.zero.dis1.repository.VertxTripRepository;
import org.zero.dis1.utils.Table;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class HelloApplication extends Application {
    static double width = 650;
    static double height = 500;
    static int updateTrip = 5;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        VBox root = new VBox();
        HBox req = new HBox();
        VBox table = new VBox();

        TextField textField = new TextField();
        textField.setText("select * from trip");

        Button retrieveWithJdbc = new Button();
        retrieveWithJdbc.setText("Retrieve with JDBC");
        retrieveWithJdbc.setOnMouseClicked(event -> {
            TripRepository repository = new JdbcTripRepository();
            repository.getTripById(updateTrip, seatsUpdateConsumer1(repository));
        });

        Button retrieveWithVertx = new Button();
        retrieveWithVertx.setText("Retrieve with Vertx");
        retrieveWithVertx.setOnMouseClicked(event -> {

            TripRepository repository = new VertxTripRepository();
            repository.startTransaction(runTransaction(repository));
        });

        req.getChildren().addAll(textField, retrieveWithJdbc, retrieveWithVertx);
        req.setSpacing(10);

        root.getChildren().addAll(req, table);


        Scene scene = new Scene(root, width, height);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private Runnable runTransaction(TripRepository repository) {
        return () -> {
            repository.getTripById(updateTrip, seatsUpdateConsumer1(repository));
        };
    }

    private Consumer<Trip> seatsUpdateConsumer1(TripRepository repository) {
        return value -> {
            System.out.println(value);
            if (value.getSeatsAvailable() <= 0) {
                System.out.println("Seats update 1 -> failed");
                return;
            }

            System.out.println("Seats update 1 -> success");
            repository.decreaseTripAvailableSeatsById(updateTrip, seatsUpdateConsumer2(repository));
        };
    }


    private Consumer<Boolean> seatsUpdateConsumer2(TripRepository repository) {
        return value -> {
            if (!value) {
                System.out.println("Seats update 2 -> failed");
                repository.rollback();
                return;
            }

            System.out.println("Seats update 2 -> success");
            repository.getTripById(updateTrip, seatsUpdateConsumer3(repository));
        };
    }

    private Consumer<Trip> seatsUpdateConsumer3(TripRepository repository) {
        return value -> {
            System.out.println(value);
            if (value.getSeatsAvailable() <= 0) {
                System.out.println("Seats update 3 -> failed");
                repository.rollback();
                return;
            }

            System.out.println("Seats update 3 -> success");
            repository.decreaseTripAvailableSeatsById(updateTrip, seatsUpdateConsumer4(repository));
        };
    }

    private Consumer<Boolean> seatsUpdateConsumer4(TripRepository repository) {
        return value -> {
            if (!value) {
                System.out.println("Seats update 4 -> failed");
                repository.rollback();
                return;
            }

            System.out.println("Seats update 4 -> success");
            repository.commit();
        };
    }

    private Consumer<Trip> tripConsumer(VBox root, Long start) {
        return trip -> {
            var end = System.nanoTime();

            Text text = new Text();
            text.setText("Completed in -> " + ((end - start)/1_000_000) + "ms");

            Text errorText = new Text();
            errorText.setText("Something went wrong.");

            if (trip == null) {
                Platform.runLater(() -> {
                    root.getChildren().clear();
                    root.getChildren().addAll(text, errorText);
                });

                return;
            }

            var table = new Table<Trip>(List.of(trip));
            table.setHeight(height);
            table.setWidth(width);



            Platform.runLater(() -> {
                root.getChildren().clear();
                root.getChildren().addAll(text, table.getTable());
            });
        };
    }

    private Consumer<List<Trip>> tripsConsumer(VBox root, Long start) {
        return trips -> {
            var end = System.nanoTime();

            Text text = new Text();
            text.setText("Completed in -> " + ((end - start)/1_000_000) + "ms");

            Text errorText = new Text();
            errorText.setText("Something went wrong.");

            if (trips.size() == 0) {
                Platform.runLater(() -> {
                    root.getChildren().clear();
                    root.getChildren().addAll(text, errorText);
                });

                return;
            }

            var table = new Table<Trip>(trips);
            table.setHeight(height);
            table.setWidth(width);



            Platform.runLater(() -> {
                root.getChildren().clear();
                root.getChildren().addAll(text, table.getTable());
            });
        };
    }

    public static void main(String[] args) {
        launch();
    }
}