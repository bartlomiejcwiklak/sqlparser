package ui;

import command.Command;
import engine.DatabaseEngine;
import engine.Result;
import exception.DatabaseException;
import model.Row;
import parser.SqlParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CommandLineInterface {
    private final DatabaseEngine engine;
    private final SqlParser parser;

    public CommandLineInterface(DatabaseEngine engine) {
        this.engine = engine;
        this.parser = new SqlParser();
    }

    public void start() {
        System.out.println("Simple Database Editor");
        System.out.println("Supported: SELECT, INSERT, UPDATE, DELETE, EXIT");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean running = true;

        while (running) {
            System.out.print("db> ");
            try {
                String input = reader.readLine();
                if (input == null) break;
                
                if (input.trim().isEmpty()) continue;

                Command command = parser.parse(input);
                Result result = command.execute(engine);
                
                printResult(result);

                if (command.shouldExit()) {
                    running = false;
                }

            } catch (DatabaseException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("FATAL I/O ERROR: " + e.getMessage());
                running = false;
            } catch (Exception e) {
                System.out.println("UNKNOWN ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void printResult(Result result) {
        if (result.hasData()) {
            List<Row> rows = result.getRows();
            if (rows.isEmpty()) {
                System.out.println("Empty set");
            } else {
                for (Row row : rows) {
                    System.out.println(row);
                }
                System.out.println(result.getMessage());
            }
        } else {
            System.out.println(result.getMessage());
        }
    }
}