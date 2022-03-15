package com.apple.playground;

import java.util.Timer;
import java.util.TimerTask;

public class Play009 {
    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Apple");
                    }
                }
        , 3000, 3000);
    }
}
