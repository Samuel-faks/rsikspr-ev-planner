CREATE TABLE vehicle (
                         id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name                        VARCHAR(100)     NOT NULL,
                         battery_kwh                 DOUBLE PRECISION NOT NULL,
                         consumption_kwh_per100km    DOUBLE PRECISION NOT NULL,
                         max_charge_power_kw         DOUBLE PRECISION NOT NULL
);

CREATE TABLE trip (
                      id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                      vehicle_id          BIGINT      NOT NULL REFERENCES vehicle (id),
                      from_station_id     VARCHAR(10) NOT NULL REFERENCES station (id),
                      to_station_id       VARCHAR(10) NOT NULL REFERENCES station (id),
                      departure_time      TIMESTAMPTZ NOT NULL,
                      start_soc_percent   INTEGER     NOT NULL,
                      min_soc_percent     INTEGER     NOT NULL,
                      temperature_c       DOUBLE PRECISION NOT NULL,
                      total_distance_km   DOUBLE PRECISION NOT NULL,
                      driving_minutes     DOUBLE PRECISION NOT NULL,
                      charging_minutes    DOUBLE PRECISION NOT NULL,
                      total_minutes       DOUBLE PRECISION NOT NULL,
                      energy_kwh          DOUBLE PRECISION NOT NULL,
                      arrival_soc_percent DOUBLE PRECISION NOT NULL
);

CREATE TABLE trip_stop (
                           id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           trip_id               BIGINT      NOT NULL REFERENCES trip (id) ON DELETE CASCADE,
                           stop_order            INTEGER     NOT NULL,          -- "order" is an SQL keyword
                           station_id            VARCHAR(10) NOT NULL REFERENCES station (id),
                           arrival_soc_percent   DOUBLE PRECISION NOT NULL,
                           departure_soc_percent DOUBLE PRECISION NOT NULL,
                           charged_kwh           DOUBLE PRECISION NOT NULL,
                           charging_minutes      DOUBLE PRECISION NOT NULL,
                           UNIQUE (trip_id, stop_order)
);
CREATE INDEX idx_trip_vehicle ON trip (vehicle_id);