package edu.java.test;

import java.time.Duration;
import java.time.Instant;

public class Timer {
    private Instant startTime = null;
    private boolean stopped = false;
    private final int duration;

    public Timer(int duration) {
        this.duration = duration;
    }

    public void start() {
        startTime = Instant.now();
        stopped = false;
    }

    public void stop() {
        stopped = true;
    }

    public boolean finished() {
        if (startTime == null) {
            return false;
        } else if (stopped) {
            return true;
        }
        stopped = Duration.between(startTime, Instant.now()).toSeconds() >= duration;
        return stopped;
    }
}
