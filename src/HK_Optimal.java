/* Author: Sinclert Perez (UC3M) */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static java.lang.Math.abs;
import static java.lang.Math.atan2;

/**
 * The Held Karp algorithm:
 * <p>
 * There are 2 possible cases in each iteration:
 * <p>
 * A) A base case where we already know the answer. (Stopping condition)
 * B) Decreasing the number of considered vertices and calling our algorithm again. (Recursion)
 * <p>
 * Explanation of every case:
 * <p>
 * A) If the list of vertices is empty, return the distance between starting point and vertex.
 * B) If the list of vertices is not empty, lets decrease our problem space:
 * <p>
 * 1) Consider each vertex in vertices as a starting point ("initial")
 * 2) As "initial" is the starting point, we have to remove it from the list of vertices
 * 3) Calculate the cost of visiting "initial" (costCurrentNode) + cost of visiting the rest from it ("costChildren")
 * 4) Return the minimum result from step 3
 */

public class HK_Optimal {

    /* ----------------------------- GLOBAL VARIABLES ------------------------------ */
    private static double[][] distances;
    private static double optimalDistance = Integer.MAX_VALUE;
    private static String optimalPath = "";


    /* ------------------------------ MAIN FUNCTION -------------------------------- */

    public static void main(String args[]) throws IOException {


        /* ----------------------------- IO MANAGEMENT ----------------------------- */

        Station s0 = new Station(0, 0);
        Station s1 = new Station(1, 1);
        Station s2 = new Station(4, 2);
        Station s3 = new Station(4, 3);
        Station s4 = new Station(1, 3);

        Station[] stations = new Station[5];
        stations[0] = s0;
        stations[1] = s1;
        stations[2] = s2;
        stations[3] = s3;
        stations[4] = s4;

        //System.out.println(calculateDistance(s1.getX(), s1.getY(), s2.getX(), s2.getY()));
        double[][] matrix = new double[5][5];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = calculateDistance(stations[i].getX(), stations[i].getY(), stations[j].getX(), stations[j].getY());
                }
            }
        }

        PrintWriter writer = new PrintWriter("DistancesMatrix.txt", StandardCharsets.UTF_8);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                writer.print(matrix[i][j] + " ");
            }
            writer.println("  ");
        }
        writer.close();
        Scanner in = new Scanner(System.in);
        System.out.println("Give starting point: ");
        int from = in.nextInt();
        System.out.println("Give end point: ");
        int to = in.nextInt();

        takeAngle(stations, from, to);

        // The path to the files with the distances is asked
        Scanner input = new Scanner(System.in);
        System.out.println("Please, introduce the path where the text file is stored");
        String file = input.nextLine();

        // The size of the distance matrix is asked
        System.out.println("Please, introduce the size of the matrix");
        int size = input.nextInt();

        // Distances array is initiated considering the size of the matrix
        distances = new double[size][size];

        // The file in that location is opened
        FileReader f = new FileReader(file);
        BufferedReader b = new BufferedReader(f);


        // Our matrix is filled with the values of the file matrix
        for (int row = 0; row < size; row++) {

            // Every value of each row is read and stored
            String line = b.readLine();
            String[] values = line.trim().split("\\s+");

            for (int col = 0; col < size; col++) {
                distances[row][col] = Integer.parseInt(values[col]);
            }
        }
        // Closing file
        b.close();

        /* ------------------------- ALGORITHM INITIALIZATION ----------------------- */

        // Initial variables to start the algorithm
        String path = "";
        int[] vertices = new int[size - 1];

        // Filling the initial vertices array with the proper values
        for (int i = 1; i < size; i++) {
            vertices[i - 1] = i;
        }

        // FIRST CALL TO THE RECURSIVE FUNCTION
        procedure(0, vertices, path, 0.0);

        System.out.print("Path: " + optimalPath + ". Distance = " + optimalDistance);
    }

    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        double distance = Math.sqrt(Math.pow((x1 + x2), 2) + Math.pow((y1 + y2), 2));
        return distance;
    }

    /* ------------------------------- RECURSIVE FUNCTION ---------------------------- */

    private static double procedure(int initial, int vertices[], String path, double costUntilHere) {

        // We concatenate the current path and the vertex taken as initial
        path = path + Double.toString(initial) + " - ";
        int length = vertices.length;
        double newCostUntilHere;


        // Exit case, if there are no more options to evaluate (last node)
        if (length == 0) {
            newCostUntilHere = costUntilHere + distances[initial][0];

            // If its cost is lower than the stored one
            if (newCostUntilHere < optimalDistance) {
                optimalDistance = newCostUntilHere;
                optimalPath = path + "0";
            }

            return (distances[initial][0]);
        }

        // If the current branch has higher cost than the stored one: stop traversing
        else if (costUntilHere > optimalDistance) {
            return 0;
        }


        // Common case, when there are several nodes in the list
        else {

            int[][] newVertices = new int[length][(length - 1)];
            double costCurrentNode, costChild;
            double bestCost = Double.MAX_VALUE;

            // For each of the nodes of the list
            for (int i = 0; i < length; i++) {

                // Each recursion new vertices list is constructed
                for (int j = 0, k = 0; j < length; j++, k++) {

                    // The current child is not stored in the new vertices array
                    if (j == i) {
                        k--;
                        continue;
                    }
                    newVertices[i][k] = vertices[j];
                }

                // Cost of arriving the current node from its parent
                costCurrentNode = distances[initial][vertices[i]];

                // Here the cost to be passed to the recursive function is computed
                newCostUntilHere = costCurrentNode + costUntilHere;

                // RECURSIVE CALLS TO THE FUNCTION IN ORDER TO COMPUTE THE COSTS
                costChild = procedure(vertices[i], newVertices[i], path, newCostUntilHere);

                // The cost of every child + the current node cost is computed
                double totalCost = costChild + costCurrentNode;

                // Finally we select from the minimum from all possible children costs
                if (totalCost < bestCost) {
                    bestCost = totalCost;
                }
            }

            return (bestCost);
        }
    }

    public static void takeAngle(Station[] stations, int from, int to) {


        double angle = 0.0;

       /* if (stations[to].getX()==stations[from].getX()) {
            if (stations[to].getY() < stations[from].getY()) {
                 angle = -180.0;
            } else if (stations[to].getY() > stations[from].getY()) {
                 angle = 180.0;
            }
            System.out.println(angle);
            return;
        }

        if (stations[to].getY()==stations[from].getY()){
            if (stations[to].getX() < stations[from].getX()) {
                 angle = -90.0;
            } else if (stations[to].getX() > stations[from].getX()) {
                 angle = 90.0;
            }
            System.out.println(angle);
            return;
        }

        */
       /* double dX = stations[2].getX() - stations[0].getX();
        double dY = stations[2].getY() - stations[0].getY();

        double angle2=Math.toDegrees(Math.atan2(dY, dX));

        */

        double dXX = stations[to].getX() - stations[from].getX();
        double dYY = stations[to].getY() - stations[from].getY();

        if (dXX == 0) {
            if (stations[to].getY() < stations[from].getY()) {
                angle = 180.0;
            } else if (stations[to].getY() > stations[from].getY()) {
                angle = 0;
            }
            System.out.println(angle);
            return;
        }

        if (dYY == 0) {
            if (stations[to].getX() < stations[from].getX()) {
                angle = 90.0;
                System.out.print("Counter clockwise: ");
            } else if (stations[to].getX() > stations[from].getX()) {
                angle = -90.0;
                System.out.print("Clockwise: ");

            }
            System.out.println(angle);
            return;
        }

        //double angle3 = 0.0;

        if (dXX > 0 && dYY > 0) {
            dXX = abs(stations[to].getX() - stations[from].getX());
            dYY = abs(stations[to].getY() - stations[from].getY());
            angle = Math.toDegrees(Math.atan2(dXX, dYY));
            // angle = 90 - angle;
            System.out.print("Clockwise: ");
        }

        if (dXX > 0 && dYY < 0) {
            dXX = abs(stations[to].getX() - stations[from].getX());
            dYY = abs(stations[to].getY() - stations[from].getY());
            angle = Math.toDegrees(Math.atan2(dYY, dXX));
            angle = 90 + angle;
            System.out.print("Clockwise: ");
        }

        if (dXX < 0 && dYY > 0) {
            dXX = abs(stations[to].getX() - stations[from].getX());
            dYY = abs(stations[to].getY() - stations[from].getY());
            angle = Math.toDegrees(Math.atan2(dXX, dYY));
            System.out.print("Counter clockwise: ");
        }

        if (dXX < 0 && dYY < 0) {
            dXX = abs(stations[to].getX() - stations[from].getX());
            dYY = abs(stations[to].getY() - stations[from].getY());
            angle = Math.toDegrees(Math.atan2(dXX, dYY));
            angle = 180 - angle;
            System.out.print("Counter clockwise: ");
        }



       /* if(stations[0].getX() < stations[2].getX()){
            angle2=angle2 + 10.0;
        }

        */

       /* float angle = (float) Math.toDegrees(Math.atan2(dY, dX));

        if (angle < 0) {
            angle += 360;
        } */

        //angle=180+angle;
        // System.out.println(angle2);
        System.out.println(angle);

    }
}