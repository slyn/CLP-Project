package core;

import java.util.ArrayList;
import org.jacop.constraints.Alldistinct;
import org.jacop.constraints.Among;
import org.jacop.constraints.And;
import org.jacop.constraints.Or;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.XplusCeqZ;
import org.jacop.core.IntVar;
import org.jacop.core.IntervalDomain;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;
import org.jacop.search.SmallestDomain;
import org.jacop.search.SmallestMin;

/**
 * This class is created for solving a Hidato puzzle with using JaCoP library.
 * Initially define the puzzle map with given numbers Later regroup the puzzle
 * cell (3x3) and search for the solution.
 * 
 * @author Hüsnü ISLEYEN
 * @version 1.0
 */
public class HidatoSolver{
    /**
     * Given puzzle map
     */
    int[][] tableList = {
        {0,  33, 35, 0,  0,  65, 65, 65},
        {0,  0,  24, 22, 0,  65, 65, 65},
        {0,  0,  0,  21, 0,  0,  65, 65},
        {0,  26, 0,  13, 40, 11, 65, 65},
        {27, 0,  0,  0,  9,  0,  1,  65},
        {65, 65, 0,  0,  18, 0,  0,  65},
        {65, 65, 65, 65, 0,  7,  0,  0},
        {65, 65, 65, 65, 65, 65, 5,  0}};
    /**
     * For the keeping puzzle map in IntVar format
     */
    IntVar[][] table = new IntVar[8][8];

    /**
     * This arraylist helps us, when constraints are imposed. and GUI interface easily reach solution
     */
    public ArrayList<IntVar> ourTable;

    /**
     * All variables and constraints are stored in a Store object
     */
    public static Store store;

    /**
     * Model method creates a puzzle map in IntVar format and imposes defined
     * constraints.
     */
    public void model() {
        store = new Store();
        ourTable = new ArrayList<IntVar>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (tableList[i][j] == 0) { //unknown box
                    table[i][j] = new IntVar(store, "table[" + i + " , " + j + "]", 1, 40);
                } else if (tableList[i][j] == 65) {
                    // unusable box 
                    table[i][j] = null;
                } else {
                    table[i][j] = new IntVar(store, "table[" + i + " , " + j + "]", tableList[i][j], tableList[i][j]); //known box
                }
            }
        }
        //Group the boxes 
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (tableList[i][j] != 0 && tableList[i][j] != 65) // if the index of array has a known value
                {
                    group3x3(i, j);
                } else if (tableList[i][j] != 65) {
                    group3x3Or(i, j);
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (table[i][j] != null) {
                    ourTable.add(table[i][j]);
                }
            }
        }

        // Constraints .. (group3x3 and group3x3Or methods impose constarints to store.)
        store.impose(new Alldistinct(ourTable)); // all values will be different

    }

    /**
     * Search method will search to domains for correct solution. If it finds a
     * solution, it will display.
     */
    public void search() {
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(ourTable.toArray(new IntVar[1]),
                new SmallestMin<IntVar>(),
                new SmallestDomain<IntVar>(),
                new IndomainMin<IntVar>());
        //search.getSolutionListener().searchAll(true);
        boolean result = search.labeling(store, select);

        System.out.println(result);
        for (int i = 0; i < ourTable.size(); i++) {
            System.out.println(ourTable.toArray(new IntVar[1])[i]);
        }
    }
    
    /**
     * This method create a group 3x3(neighbours of given value) and impose
     * Among constraints to it. This method is used for only known box. It takes a box and regroup its neighbour and impose constarints that
     * sequential numbers will take place in this group. (value - centeral
     * box,value + 1, and value - 1) these three values are contained by group.
     *
     * @param indeX x coordinate of given cell
     * @param indeY y coordinate of given cell
     */
    public void group3x3(int indeX, int indeY) {
        // A[i,j] group with 
        // A[i-1,j-1], A[i-1,j], A[i-1,j+1]
        // A[i,j-1],   A[i,j],   A[i,j+1]
        // A[i+1,j-1], A[i+1,j], A[i+1,j+1]
        //sequence value of A[i,j] must be element of this group.
        ArrayList<IntVar> group = new ArrayList<>();
        IntVar count;
        IntervalDomain domain = new IntervalDomain(table[indeX][indeY].value() - 1, table[indeX][indeY].value() + 1);
        if (domain.min() <= 0 || domain.max() >= 41) { //max and min values
            count = new IntVar(store, "two", 2, 2);
        } else {
            count = new IntVar(store, "three", 3, 3);
        }
        for (int i = indeX - 1; i < indeX + 2; i++) {
            for (int j = indeY - 1; j < indeY + 2; j++) {
                if (i >= 0 && j >= 0 && i <= 7 && j <= 7) {
                    if (tableList[i][j] != 65) //if table[i][j] is not null)
                    {
                        group.add(table[i][j]);
                    }
                    //System.out.println(i+ " - " + j);
                }
            }
        }
        /*
         System.out.println("----------------------");
         System.out.println(indeX + " - " + indeY);
         System.out.println(domain);
         System.out.println("----------------------");
         System.out.println("\n");
         */
        store.impose(new Among(group, domain, count));

    }

    /**
     * This method create a group and impose primitive constraints for unknown
     * box. This method is used for known and unknown box. It takes a box and regroup its neighbour and impose constarints that
     * sequential numbers will take place in this group. (value - centeral
     * box,value + 1, and value - 1) these three values are contained by group.
     *
     * @param indeX x coordinate of centeral cell
     * @param indeY y coordinate of centeral cell
     */
    public void group3x3Or(int indeX, int indeY) {
        // A[i,j] group with 
        // A[i-1,j-1], A[i-1,j], A[i-1,j+1]
        // A[i,j-1],   A[i,j],   A[i,j+1]
        // A[i+1,j-1], A[i+1,j], A[i+1,j+1]
        //sequence value of A[i,j] must be element of this group.
        ArrayList<PrimitiveConstraint> myLimitPositive = new ArrayList<PrimitiveConstraint>();
        ArrayList<PrimitiveConstraint> myLimitNegative = new ArrayList<PrimitiveConstraint>();

        for (int i = indeX - 1; i < indeX + 2; i++) {
            for (int j = indeY - 1; j < indeY + 2; j++) {
                if (i >= 0 && j >= 0 && i <= 7 && j <= 7) {
                    if (tableList[i][j] != 65 && (i != indeX || j != indeY)) { //if table[i][j] is not null
                        myLimitPositive.add(new XplusCeqZ(table[indeX][indeY], 1, table[i][j]));
                        myLimitNegative.add(new XplusCeqZ(table[indeX][indeY], -1, table[i][j]));
                        //System.out.println(i+ " - " + j);
                    }
                }
            }
        }
        /*
         System.out.println("----------------------");
         System.out.println(indeX + " - " + indeY);
         System.out.println("----------------------");
         System.out.println("\n");
         */
        store.impose(new And(new Or(myLimitNegative), new Or(myLimitPositive)));
    }
}
