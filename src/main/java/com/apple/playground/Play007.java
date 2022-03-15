package com.apple.playground;

import java.text.SimpleDateFormat;
import java.util.Date;
public class Play007 {
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        System.out.println(sdf.format(new Date(1646655214408L)));
    }
}
