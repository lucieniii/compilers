package main.java.c0;

import main.java.c0.analyser.Analyser;
import main.java.c0.instruction.Instruction;
import main.java.c0.util.*;
import main.java.c0.tokenizer.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void traverseData(String sourceDir) throws IOException {
        File pwd = new File(sourceDir);

        if (pwd.isDirectory()) {
            String[] fileList = pwd.list();
            if (fileList == null)
                return;
            for (String filename : fileList) {
                if (!filename.equals(".DS_Store"))
                    traverseData(sourceDir + "/" + filename);
            }
        } else testData(sourceDir, null);
    }

    public static void testData(String inputFileName, String outputFileName) throws IOException {

        if (!inputFileName.split("\\.")[1].equals("c0"))
            return;
        InputStream input;

        try {
            input = new FileInputStream(inputFileName);
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find input file.");
            e.printStackTrace();
            System.exit(2);
            return;
        }
        if (outputFileName == null)
            outputFileName = inputFileName.split("\\.")[0] + ".o0";
        PrintStream output;
        try {
            output = new PrintStream(new FileOutputStream(outputFileName));
        } catch (FileNotFoundException e) {
            try{
                File file = new File(outputFileName);
                var temp = file.createNewFile();
                output = new PrintStream(new FileOutputStream(outputFileName));
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }

        Scanner scanner;
        scanner = new Scanner(input);

        var iter = new StringIter(scanner);
        var tokenizer = new Tokenizer(iter);
/*
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
            output.println(e);
            // System.exit(-1);
            return;
        }
        for (Token token : tokens) {
            output.println(token.toString());
        }
    */
        var analyzer = new Analyser(tokenizer);
        try {
            analyzer.analyse(output);
        } catch (Exception e) {
            // 遇到错误不输出，直接退出
            //output.println(e);
            System.exit(-1);
        }
        /*output.println("Analysis accomplished!");
        for (Instruction instruction : instructions) {
            output.println(instruction.toString());
        }*/
    }

    public static void main(String[] args) throws IOException {
        //traverseData("data");
        //traverseData("onetest");
        testData(args[0], args[1]);
    }
}
