package edu.montana.csci.csci468.demo;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;

public class Scratch {
    int y;
    static int add(int i) {
        System.out.println("hi");
        return i + 13;
    }

    public int intFunc(int i1, int i2) {
        return i1 + i2;
    }

    public static void main(String[] args) {
        if(1 == 0){
            System.out.println("hi");
        }
        add(2);
    }
}
