package com.zenika.kafka.demo;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

public class InfluxDBRepository {

    private String connectString;
    private String dbName;

    private String userName;
    private String userPassword;

    private InfluxDB influxDB;
    /**
     * Creates a new {@link InfluxDBRepository} instance.
     */
    public InfluxDBRepository(String connectString, String dbName, String userName, String userPassword) {
        this.connectString = connectString;
        this.dbName = dbName;
        this.userName = userName;
        this.userPassword = userPassword;

    }

    public void open() {
        if( this.influxDB == null ) {
            this.influxDB = InfluxDBFactory.connect(connectString, userName, userPassword);
            this.influxDB.createDatabase(dbName);
            influxDB.enableBatch(2000, 100, TimeUnit.MILLISECONDS);
        }
    }

    public void write(Metrics point) {

        Point point1 = Point.measurement("cpu")
                .time(point.time, TimeUnit.MILLISECONDS)
                .field("sys", point.cpuPercSys)
                .field("user", point.cpuPercUser)
                .build();

        Point point2 = Point.measurement("mem")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .field("actual", point.memActualUsed / 1024)
                .field("total", point.memTotal / 1024)
                .field("used", point.memUsed / 1024)
                .field("free", point.memFree / 1024)
                .build();

        influxDB.write(dbName, "default", point1);
        influxDB.write(dbName, "default", point2);
    }
}
