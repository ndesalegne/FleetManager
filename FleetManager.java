//Nahom Desalegne
//11/30/2025
//CSC 120
//Final Project


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FleetManager {

    private static final String DB_FILE = "FleetData.db";

    private enum BoatType {
        SAILING, POWER
    }

    private static class Boat implements Serializable {
        private static final long serialVersionUID = 1L;

        BoatType type;
        String name;
        int year;
        String make;
        int lengthFeet;
        double pricePaid;
        double expenses;

        public Boat(BoatType type, String name, int year, String make,
                    int lengthFeet, double pricePaid, double expenses) {
            this.type = type;
            this.name = name;
            this.year = year;
            this.make = make;
            this.lengthFeet = lengthFeet;
            this.pricePaid = pricePaid;
            this.expenses = expenses;
        }

        /** EXACT LINE MATCHING PROFESSOR OUTPUT */
        public String formatReportLine() {
            return String.format(
                    "    %-8s %-12s %4d %-10s %3d' : Paid $ %10.2f : Spent $ %7.2f",
                    type.name(),
                    name,
                    year,
                    make,
                    lengthFeet,
                    pricePaid,
                    expenses
            );
        }
    }

    // -------- FILE HANDLING --------

    private static List<Boat> loadFromCsv(String fileName) {
        List<Boat> boats = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                BoatType type = BoatType.valueOf(p[0].trim().toUpperCase());
                String name = p[1].trim();
                int year = Integer.parseInt(p[2].trim());
                String make = p[3].trim();
                int length = Integer.parseInt(p[4].trim());
                double price = Double.parseDouble(p[5].trim());

                boats.add(new Boat(type, name, year, make, length, price, 0.0));
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file.");
        }
        return boats;
    }

    private static List<Boat> loadFromDb() {
        File f = new File(DB_FILE);
        if (!f.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<Boat>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void saveToDb(List<Boat> boats) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DB_FILE))) {
            oos.writeObject(boats);
        } catch (IOException ignored) {}
    }


    // -------- CORE FEATURES --------

    private static Boat findBoat(List<Boat> boats, String name) {
        name = name.trim().toLowerCase();
        for (Boat b : boats) {
            if (b.name.toLowerCase().equals(name)) return b;
        }
        return null;
    }

    private static void printFleet(List<Boat> boats) {
        System.out.println();
        System.out.println("Fleet report:");

        double totalPaid = 0.0;
        double totalSpent = 0.0;

        for (Boat b : boats) {
            System.out.println(b.formatReportLine());
            totalPaid += b.pricePaid;
            totalSpent += b.expenses;
        }

        System.out.printf(
                "    %-8s %-12s %-4s %-10s %3s  : Paid $ %10.2f : Spent $ %7.2f%n",
                "Total", "", "", "", "",
                totalPaid, totalSpent
        );
    }

    private static void addBoat(List<Boat> boats, Scanner in) {
        System.out.print("Please enter the new boat CSV data      : ");
        String line = in.nextLine();
        String[] p = line.split(",");

        BoatType type = BoatType.valueOf(p[0].trim().toUpperCase());
        String name = p[1].trim();
        int year = Integer.parseInt(p[2].trim());
        String make = p[3].trim();
        int length = Integer.parseInt(p[4].trim());
        double price = Double.parseDouble(p[5].trim());

        boats.add(new Boat(type, name, year, make, length, price, 0.0));
    }

    private static void removeBoat(List<Boat> boats, Scanner in) {
        System.out.print("Which boat do you want to remove?       : ");
        String name = in.nextLine();

        Boat b = findBoat(boats, name);
        if (b == null) {
            System.out.println("Cannot find boat " + name);
        } else {
            boats.remove(b);
        }
    }

    private static void addExpense(List<Boat> boats, Scanner in) {
        System.out.print("Which boat do you want to spend on?    : ");
        String name = in.nextLine();
        Boat b = findBoat(boats, name);

        if (b == null) {
            System.out.println("Cannot find boat " + name);
            return;
        }

        System.out.print("How much do you want to spend?         : ");
        double amt = Double.parseDouble(in.nextLine());

        double remaining = b.pricePaid - b.expenses;

        if (amt <= remaining) {
            b.expenses += amt;
            System.out.printf("Expense authorized, $%.2f spent.%n", b.expenses);
        } else {
            System.out.printf("Expense not permitted, only $%.2f left to spend.%n", remaining);
        }
    }

    // -------- MENU LOOP --------

    private static void menuLoop(List<Boat> boats) {
        Scanner in = new Scanner(System.in);

        System.out.println("Welcome to the Fleet Management System");
        System.out.println("--------------------------------------");

        while (true) {
            System.out.print("\n(P)rint, (A)dd, (R)emove, (E)xpense, e(X)it : ");
            String s = in.nextLine().trim();

            if (s.isEmpty()) continue;

            char c = Character.toUpperCase(s.charAt(0));

            switch (c) {
                case 'P':
                    printFleet(boats);
                    break;

                case 'A':
                    addBoat(boats, in);
                    break;

                case 'R':
                    removeBoat(boats, in);
                    break;

                case 'E':
                    addExpense(boats, in);
                    break;

                case 'X':
                    System.out.println();
                    System.out.println("Exiting the Fleet Management System");
                    saveToDb(boats);


                    return;


                default:
                    System.out.println("Invalid menu option, try again");
            }
        }
    }

    public static void main(String[] args) {

        List<Boat> boats;

        // Check if DB exists
        File f = new File(DB_FILE);
        if (f.exists()) {
            // Load DB version
            boats = loadFromDb();
        } else {
            // Load CSV version
            boats = loadFromCsv("FleetData.csv");
        }

        menuLoop(boats);
    }


}
