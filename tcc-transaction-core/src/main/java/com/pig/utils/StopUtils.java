package com.pig.utils;

public class StopUtils  {
    public static void stop() {
        new Thread(() -> System.exit(1)).start();
    }
}
