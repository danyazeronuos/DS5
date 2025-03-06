module org.zero.dis1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires io.vertx.core;
    requires io.vertx.client.sql.pg;
    requires io.vertx.client.sql;


    opens org.zero.dis1 to javafx.fxml;
    opens org.zero.dis1.model to javafx.base;
    exports org.zero.dis1;
    opens org.zero.dis1.entity to javafx.base;
}