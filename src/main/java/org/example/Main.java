package org.example;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 3 && args[0].equals("c") || args.length < 2 && args[0].equals("d")) {
            System.out.println("Invalid Arguments");
            return;
        }

        String mode = args[0];
        String filePath = args[1];
        Compressor compressor = new Compressor();
        Decompressor decompressor = new Decompressor();

        if ("c".equals(mode) && args.length == 3) {
            int n = Integer.parseInt(args[2]);
            compressor.compress(filePath, n);
        } else if ("d".equals(mode)) {
           decompressor.decompress(filePath);
        } else {
            System.out.println("Invalid Arguments");
        }

        /*
        I acknowledge that I am aware of the academic integrity guidelines of this course,
        and that I worked on this assignment independently without any unauthorized help
        */

    }

}
