package main.java.c0;

import main.java.c0.util.*;
import main.java.c0.tokenizer.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    /*public static void copyDirectory(String sourceDir, String targetDir) throws IOException {
        File src = new File(sourceDir);
        File dst = new File(targetDir);
        if (src.isDirectory()) {
            if (!dst.mkdir()) {
                System.out.println("Failed to create directory " + dst.getPath());
                return;
            }
            String[] fileList = src.list();
            if (fileList == null)
                return;
            for (String filename : fileList)
                copyDirectory(sourceDir + "/" + filename, targetDir + "/" + filename);
        } else TestCopyFile.copyFile(sourceDir, targetDir);
    }*/

    public static void main(String[] args) {

        String inputFileName = "data/0-basic/wa1-1-empty.c0";
        String outputFileName = "result.out";

        InputStream input;

        try {
            input = new FileInputStream(inputFileName);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find input file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }


        PrintStream output;

        try {
            output = new PrintStream(new FileOutputStream(outputFileName));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open output file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }

        Scanner scanner;
        scanner = new Scanner(input);
        var iter = new StringIter(scanner);
        var tokenizer = new Tokenizer(iter);

        var tokens = new ArrayList<Token>();
        try {
            while (true) {
                var token = tokenizer.nextToken();
                if (token.getTokenType().equals(TokenType.EOF)) {
                    break;
                }
                tokens.add(token);
            }
        } catch (Exception e) {
            // 遇到错误不输出，直接退出
            output.println(e);
            System.exit(-1);
            return;
        }
        for (Token token : tokens) {
            output.println(token.toString());
        }

    }
}
