package com.company;

public class Main {

    public static void main(String[] args) {
        Job job = new Job();
        System.out.println("Hello world!" + job.getDepcode());
            DBUtil.testDatabase();
    }
}
