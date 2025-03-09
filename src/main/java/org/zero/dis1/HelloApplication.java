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
import org.zero.dis1.model.ReservedRepository;
import org.zero.dis1.model.TripRepository;
import org.zero.dis1.model.UserRepository;
import org.zero.dis1.repository.*;
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

        TextField textField = new TextField();
        textField.setText("select * from trip");

        Button retrieveWithJdbc = new Button();
        retrieveWithJdbc.setText("Retrieve with JDBC");
        retrieveWithJdbc.setOnMouseClicked(event -> {
            TripRepository repository = new JdbcTripRepository();
            UserRepository userRepository = new JdbcUserRepository();
            ReservedRepository reservedRepository = new JdbcReservedRepository();
            repository.getTripById(updateTrip, seatsAvailabilityCheck(repository, userRepository, reservedRepository));
        });

        Button retrieveWithVertx = new Button();
        retrieveWithVertx.setText("Retrieve with Vertx");
        retrieveWithVertx.setOnMouseClicked(event -> {

            TripRepository repository = new VertxTripRepository();
            UserRepository userRepository = new VertxUserRepository();
            ReservedRepository reservedRepository = new VertxReservedRepository();
            repository.startTransaction(runTripTransaction(repository, userRepository, reservedRepository));
        });

        req.getChildren().addAll(textField, retrieveWithJdbc, retrieveWithVertx);
        req.setSpacing(10);

        root.getChildren().addAll(req, table);


        Scene scene = new Scene(root, width, height);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    private Runnable runTripTransaction(TripRepository repository, UserRepository userRepository, ReservedRepository reservedRepository) {
        return () -> {
            userRepository.startTransaction(runUserTransaction(repository, userRepository, reservedRepository));
        };
    }

    private Runnable runUserTransaction(TripRepository repository, UserRepository userRepository, ReservedRepository reservedRepository) {
        return () -> {
            repository.getTripById(updateTrip, seatsAvailabilityCheck(repository, userRepository, reservedRepository));
        };
    }

    private Consumer<Trip> seatsAvailabilityCheck(TripRepository repository, UserRepository userRepository, ReservedRepository reservedRepository) {
        return value -> {
            if (value.getSeatsAvailable() <= 0) {
                System.out.println("Seats check -> failed");
                return;
            }

            System.out.println("Seats check -> success");
            userRepository.findUserById(user, checkBalanceConsumer1(repository, userRepository, reservedRepository, value.getPrice()));
        };
    }

    private Consumer<User> checkBalanceConsumer1(TripRepository repository, UserRepository userRepository, ReservedRepository reservedRepository, Double price) {
        return value -> {
            System.out.println(value);
            if (value.getBalance() < price) {
                System.out.println("Balance check -> failed");
                return;
            }

            System.out.println("Balance check -> success");
            value.setBalance(value.getBalance() - price);
            userRepository.updateUser(value, updateUserBalance(repository, userRepository, reservedRepository));
        };
    }

    private Consumer<Boolean> updateUserBalance(TripRepository repository, UserRepository userRepository, ReservedRepository reservedRepository) {

        return (value) -> {
            if (!value) {
                System.out.println("Balance update -> failed");
                repository.rollback();
                userRepository.rollback();
                return;
            }

            System.out.println("Balance update -> success");
            repository.decreaseTripAvailableSeatsById(updateTrip, reserveTrip(repository, userRepository, reservedRepository));
        };
    }

    private Consumer<Boolean> reserveTrip(TripRepository repository, UserRepository userRepository, ReservedRepository reservedRepository) {
        return (value) -> {
            if (!value) {
                System.out.println("Reserve -> failed");
                repository.rollback();
                userRepository.rollback();
                return;
            }

            var entity = Reserved.builder()
                    .userId(user)
                    .tripId(updateTrip)
                    .build();

            System.out.println("Reserve -> success");
            reservedRepository.save(entity, seatsUpdateConsumer4(repository, userRepository));
        };
    }

    private Consumer<Boolean> seatsUpdateConsumer4(TripRepository repository, UserRepository userRepository) {
        return value -> {
            if (!value) {
                System.out.println("Reservation of trip -> failed");
                repository.rollback();
                userRepository.rollback();
                return;
            }

            System.out.println("Reservation of trip -> success");
            repository.commit();
            userRepository.commit();
        };
    }

    private Consumer<Trip> tripConsumer(VBox root, Long start) {
        return trip -> {
            var end = System.nanoTime();

            Text text = new Text();
            text.setText("Completed in -> " + ((end - start) / 1_000_000) + "ms");

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
            text.setText("Completed in -> " + ((end - start) / 1_000_000) + "ms");

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