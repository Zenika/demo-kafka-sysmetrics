package com.zenika.kafka.demo;


import com.google.common.base.MoreObjects;

public class Metrics {

    public final long time;
    public final double cpuPercUser;
    public final double cpuPercSys;
    public final double memTotal;
    public final double memActualUsed;
    public final double memUsed;
    public final double memFree;

    /**
     * Creates a new {@link Metrics} instance.
     */
    public Metrics(long time, double cpuPercUser, double cpuPercSys, double memTotal, double memActualUsed, double memUsed, double memFree) {
        this.time = time;
        this.cpuPercUser = cpuPercUser;
        this.cpuPercSys = cpuPercSys;
        this.memTotal = memTotal;
        this.memActualUsed = memActualUsed;
        this.memUsed = memUsed;
        this.memFree = memFree;
    }

    /**
     * Creates a new {@link Metrics} instance.
     */
    public Metrics(double cpuPercUser, double cpuPercSys, double memTotal, double memActualUsed, double memUsed, double memFree) {
        this(System.currentTimeMillis(), cpuPercUser, cpuPercSys, memTotal, memActualUsed, memUsed, memFree);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("time", time)
                .add("cpuPercUser", cpuPercUser)
                .add("cpuPercSys", cpuPercSys)
                .add("memTotal", memTotal)
                .add("memActualUsed", memActualUsed)
                .add("memUsed", memUsed)
                .add("memFree", memFree)
                .toString();
    }
}
