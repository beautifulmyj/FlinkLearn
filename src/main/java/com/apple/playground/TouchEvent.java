package com.apple.playground;

public class TouchEvent {
    public String user;
    public Long ts;

    public TouchEvent() {
    }

    public TouchEvent(String user, Long ts) {
        this.user = user;
        this.ts = ts;
    }

    public static TouchEvent of(String user, Long ts) {
        return new TouchEvent(user, ts);
    }

    @Override
    public String toString() {
        return "TouchEvent{" +
                "user='" + user + '\'' +
                ", ts=" + ts +
                '}';
    }
}
