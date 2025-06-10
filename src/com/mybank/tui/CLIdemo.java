package com.mybank.tui;

import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import com.mybank.data.DataSource;
import com.mybank.reporting.CustomerReport;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.utils.*;
import org.fusesource.jansi.*;

/**
 * Console client for 'Banking' example.
 *
 * @author Alexander 'Taurus' Babich.
 */
public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private String[] commandsList;

    public void init() {
        commandsList = new String[]{"help", "customers", "customer", "report", "exit"};
    }

    public void run() {
        AnsiConsole.systemInstall();
        printWelcomeMessage();
        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completors = new LinkedList<>();

        completors.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completors));

        LineReader reader = readerBuilder.build();

        String line;
        PrintWriter out = new PrintWriter(System.out);

        while ((line = readLine(reader, "")) != null) {
            if ("help".equals(line)) {
                printHelp();
            } else if ("customers".equals(line)) {
                printCustomersSummary();
            } else if (line.startsWith("customer")) {
                printCustomerDetails(line);
            } else if ("report".equals(line)) {
                CustomerReport report = new CustomerReport();
                report.generateReport();
            }
            else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;
            } else {
                System.out
                        .println(ANSI_RED + "Invalid command, For assistance press TAB or type \"help\" then hit ENTER." + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void printCustomersSummary() {
        AttributedStringBuilder a = new AttributedStringBuilder()
                .append("\nThis is all of your ")
                .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                .append(":");

        System.out.println(a.toAnsi());
        if (Bank.getNumberOfCustomers() > 0) {
            System.out.println("\nLast name\tFirst Name\tBalance");
            System.out.println("---------------------------------------");
            for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                Customer customer = Bank.getCustomer(i);
                String balance = (customer.getNumberOfAccounts() > 0 && customer.getAccount(0) != null) ?
                                 String.format("$%.2f", customer.getAccount(0).getBalance()) :
                                 "N/A (No account)";
                System.out.println(customer.getLastName() + "\t\t" + customer.getFirstName() + "\t\t" + balance);
            }
        } else {
            System.out.println(ANSI_RED + "Your bank has no customers!" + ANSI_RESET);
        }
    }

    private void printCustomerDetails(String line) {
        try {
            int custNo = 0;
            if (line.length() > "customer ".length()) {
                String strNum = line.substring("customer ".length()).trim();
                custNo = Integer.parseInt(strNum);
            } else {
                System.out.println(ANSI_RED + "ERROR! Please specify customer number (e.g., customer 0)." + ANSI_RESET);
                return;
            }

            Customer cust = Bank.getCustomer(custNo);
            if (cust == null) {
                System.out.println(ANSI_RED + "ERROR! Customer with ID " + custNo + " not found." + ANSI_RESET);
                return;
            }

            AttributedStringBuilder a = new AttributedStringBuilder()
                    .append("\nThis is detailed information about customer #")
                    .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                    .append("!");

            System.out.println(a.toAnsi());

            System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
            System.out.println("-------------------------------------------------------");

            if (cust.getNumberOfAccounts() > 0) {
                for (int i = 0; i < cust.getNumberOfAccounts(); i++) {
                    com.mybank.domain.Account account = cust.getAccount(i);
                    String accType = "Unknown";
                    if (account instanceof CheckingAccount) {
                        accType = "Checking";
                    } else if (account instanceof SavingsAccount) {
                        accType = "Savings";
                    }
                    System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + accType + "\t\t$" + String.format("%.2f", account.getBalance()));
                }
            } else {
                System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\tN/A\t\t$0.00 (No accounts)");
            }

        } catch (NumberFormatException e) {
            System.out.println(ANSI_RED + "ERROR! Invalid customer number format. Please enter a number." + ANSI_RESET);
        } catch (IndexOutOfBoundsException e) {
            System.out.println(ANSI_RED + "ERROR! Customer ID out of bounds. Please enter a valid customer ID." + ANSI_RESET);
        } catch (Exception e) {
            System.out.println(ANSI_RED + "ERROR! An unexpected error occurred: " + e.getMessage() + ANSI_RESET);
        }
    }

    private void printWelcomeMessage() {
        System.out
                .println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET + "! \nFor assistance press TAB or type \"help\" then hit ENTER.");
    }

    private void printHelp() {
        System.out.println("help\t\t\t- Show help");
        System.out.println("customers\t\t- Show list of customers summary");
        System.out.println("customer <id>\t\t- Show customer details by ID (e.g., customer 0)");
        System.out.println("report\t\t\t- Generate customer report");
        System.out.println("exit\t\t\t- Exit the app");
    }

    private String readLine(LineReader reader, String promtMessage) {
        try {
            String line = reader.readLine(promtMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET);
            return line.trim();
        } catch (UserInterruptException e) {
            return null;
        } catch (EndOfFileException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Loading bank data from test.dat...");
            DataSource dataSource = new DataSource("test.dat");
            dataSource.loadData();

            System.out.println("Bank data loaded successfully. Total customers: " + Bank.getNumberOfCustomers());

        } catch (IOException e) {
            System.err.println(ANSI_RED + "ERROR! Failed to load data from test.dat: " + e.getMessage() + ANSI_RESET);
            System.err.println(ANSI_RED + "Please ensure test.dat is in the same directory as the JAR file when running." + ANSI_RESET);
            System.exit(1);
        } catch (Exception e) {
            System.err.println(ANSI_RED + "ERROR! An unexpected error occurred during data loading: " + e.getMessage() + ANSI_RESET);
            System.exit(1);
        }

        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.run();
    }
}