package com.apple.playground;

public class Play016 {
    public static void main(String[] args) {
        String str1 = new String("jiaojiao").intern();
        String str2 = new String("jiaojiao").intern();

        System.out.println(str1 == str2);
    }
}
