/*
================= users_database ================
*/

CREATE TABLE users
(
    id       uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    username character varying(24) NOT NULL,
    balance    double precision      NOT NULL
);

INSERT INTO "public"."users"("username", "balance") VALUES('danyazero', 2600);
INSERT INTO "public"."users"("username", "balance") VALUES('shevchuk', 3900);

/*
================= trip_database ================
*/

CREATE TABLE trip
(
    id              integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    destination     text             NOT NULL,
    price           double precision NOT NULL,
    seats_available integer          NOT NULL
);

INSERT INTO "public"."trip"("destination", "price", "seats_available") VALUES('Kiev', 250, 3);
INSERT INTO "public"."trip"("destination", "price", "seats_available") VALUES('Lviv', 450, 2);
INSERT INTO "public"."trip"("destination", "price", "seats_available") VALUES('Kherson', 200, 2);
INSERT INTO "public"."trip"("destination", "price", "seats_available") VALUES('Odessa', 240, 4);
INSERT INTO "public"."trip"("destination", "price", "seats_available") VALUES('Dnipro', 280, 3);
INSERT INTO "public"."trip"("destination", "price", "seats_available") VALUES('Ternopil', 320, 2);


CREATE TABLE reserved
(
    user_id uuid    NOT NULL,
    trip_id integer NOT NULL REFERENCES trip (id)
);
