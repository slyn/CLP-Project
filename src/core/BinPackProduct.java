package core;

import java.util.ArrayList;
import java.util.Collections;
import org.jacop.constraints.Constraint;
import org.jacop.constraints.XlteqC;
import org.jacop.constraints.XmulCeqZ;
import org.jacop.constraints.XplusYplusQeqZ;
import org.jacop.constraints.binpacking.Binpacking;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainMin;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSelect;

/**
 * Binpacking Problem - Product Scheduling(Resource Allocation) sources
 * [R1{10,10,10,10}, R2{5,5,5,5}, R3{2,2,2,2}] - total=68 Resources --- products
 * [P1{10x1 + 2x1}, P2{1x5 + 2x2}, P3{1x2}] trucks = 10xTrucks = 10, 5xTrucks =
 * 15
 *
 * @author Hüsnü ISLEYEN
 * @version 1.0
 */
public class BinPackProduct{

    /**
     * All variables and constraints are stored in a Store object
     */
    public Store store;
    /**
     * Keeps the Trucks and Capacity
     */
    public IntVar[] trucks;
    /**
     * Keeps string for gui - result of search(planning)
     */
    public String text;
    /**
     * Truck number - (from 0 to 15 all trucks can be usable)
     */
    IntVar[] b;

    /**
     * Keeps the products quantity - Quantities of P1,P2,P3
     */
    IntVar[] product;

    /**
     * Keeps the income values of solutions.
     */
    ArrayList<Integer> myList;

    /**
     * Method creates model of BinPacking Problem. Variables and constraint are
     * defined and BinPacking constraints are imposed.
     *
     */
    public void model() {
        store = new Store();
        trucks = new IntVar[15]; //Total Trucks
        // items
        // 10 x 4 -> R1
        // 5  x 4 -> R2
        // 2  x 4 -> R3
        int[] w = {10, 10, 10, 10, 5, 5, 5, 5, 2, 2, 2, 2};

        for (int i = 0; i < 10; i++) { // Truck type 1 - capacity 10
            trucks[i] = new IntVar(store, "Truck Container x " + (i + 1), 0, 10);
        }

        for (int i = 10; i < 15; i++) { // Truck type 2 - capacity 15
            trucks[i] = new IntVar(store, "Truck Container x " + (i + 1), 0, 15);
        }

        b = new IntVar[w.length]; // array of truck number
        for (int i = 0; i < w.length; i++) {
            b[i] = new IntVar(store, "b " + (i + 1), 0, 15);
        }

        Constraint ctr = new Binpacking(b, trucks, w); // items will load - it is enough to calculate trucks load
        store.impose(ctr);
        System.out.println(ctr);
        System.out.println(store);
        System.out.println(store.consistency());
        System.out.println("\nIntVar store size: " + store.size()
                + "\nNumber of constraints: " + store.numberConstraints());
    }

    /**
     * Method searches solution/solutions for modelled BinPacking Problem. It
     * displays trucks and truck status(carried items).
     *
     */
    public void search() { // search for trucks
        Search<IntVar> label = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(trucks,
                null,
                new IndomainMin<IntVar>());

        label.setAssignSolution(true);
        label.setPrintInfo(true);

        if (label.labeling(store, select)) {
            System.out.println("*** Yes");
            for (int i = 0; i < 15; i++) {
                System.out.println(java.util.Arrays.asList(trucks[i]));
            }
        } else {
            System.out.println("*** No");
        }

    }

    /**
     * Method creats model of second part of our problem(Product Planning).
     * First part, trucks are arranged. Second part, method needs to scheduling
     * product table for max. income. Given information:
     *
     * P1 -- 10x1 + 2x1 - 15PLN Income for per P1 product P2 -- 5x1 + 2x2 - 8 PLN
     * Income for per P2 product P3 -- 2x1 - 3 PLN Income for per P3 product
     */
    public void resourceAllocation() { // calculate max income
        /* Maximum income domain
         |  P1  |   P2  |  P3  |
         | 0..4 | 0..2  | 0..4 | --> (product domain) Will be searched for max income
         |  x   |   y   |   z  | --> quantity of product 
         |x<=4  | y<=4  | z<=4 | --> restriction from total resource
        x*P1 = x*R1 + x*R3
        y*P2 = y*R2 + y*2*R3
        z*P3 = z*R3
        As a result 
        # x <= 4 (x*R1 --> max. 4*R1)       //this situation handled at definition of domain
        # y <= 4 (y*R2 --> max. 4*R2)       //this situation handled at definition of domain        
        # x+2y+z <= 4 (x*R3+2*y*R3+z*R3)    //constarint will imposed for this situation.
        total R3 can be (0 to 4)
        
        xP1+yP2+zP3
        */
        product = new IntVar[3];
        product[0] = new IntVar(store, "Product Quantity P1", 0, 4);
        product[1] = new IntVar(store, "Product Quantity P2", 0, 2);
        product[2] = new IntVar(store, "Product Quantity P3", 0, 4);
        IntVar resultAdd = new IntVar(store, "Result of Addition(P)", 0, 20);
        IntVar resultMul = new IntVar(store, "Result of Multiplication(P)", 0, 20);
        //IntVar tot = new IntVar(store, "Total quantity", 0, 1000);
        IntVar[] mul = new IntVar[4];
        mul[0] = new IntVar(store, "Total income from P1", 0, 100);
        mul[1] = new IntVar(store, "Total income from P1", 0, 100);
        mul[2] = new IntVar(store, "Total income from P1", 0, 100);

        //PrimitiveConstraint[] a1 = new PrimitiveConstraint[3];
        /* no needed - handled with initial max values - with resources max product quantity
         a1[0] = new XlteqC(x, 4); //X ≤ Const	XlteqC(X, Const)
         a1[1] = new XlteqC(y, 4); //X ≤ Const	XlteqC(X, Const)
         a1[2] = new XlteqC(z, 4); //X ≤ Const	XlteqC(X, Const)
         */
        //|x+2y+z<=4|
        //resultAdd = x + 2*y + z;
        store.impose(new XmulCeqZ(product[1], 2, resultMul));
        store.impose(new XplusYplusQeqZ(product[0], resultMul, product[2], resultAdd));
        store.impose(new XlteqC(resultAdd, 4));

        // *** X ⋅ Const = Z	XmulCeqZ(X, Const, Z)
        //store.impose(new XmulCeqZ(product[0], 15, mul[0]));
        //store.impose(new XmulCeqZ(product[1], 8, mul[1]));
        //store.impose(new XmulCeqZ(product[2], 3, mul[2]));
        // X + Y + Q > Const	XplusYplusQgtC(X, Y, Q, Const)
        // ---------
        //store.impose(new XplusYplusQeqZ(mul[0], mul[1], mul[2], tot));
        //cost = new IntVar(store, "Total COST : ", tot.value(), tot.value());
        /*store.impose(new And(a1));*/
    }

    /**
     * Method searches solution/solutions for modelled second part of our
     * problem(Products). It displays max income and solution which gives max
     * income(carried items). Given information:
     *
     * P1 -- 10x1 + 2x1 - 15 Income for per P1 product P2 -- 5x1 + 2x2 - 8
     * Income for per P2 product P3 -- 2x1 - 3 Income for per P3 product
     *
     */
    public void searchMaxIncome() {
        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        myList = new ArrayList<Integer>();
        SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(product,
                null, new IndomainMin<IntVar>());
        search.getSolutionListener().searchAll(true);
        search.getSolutionListener().recordSolutions(true);
        boolean result = search.labeling(store, select);
        if (result) {
            System.out.println("*** Yes");
            for (int i = 1; i <= search.getSolutionListener().solutionsNo(); i++) {
                myList.add(Integer.parseInt(search.getSolution(i)[0].toString()) * 15
                        + 8 * Integer.parseInt(search.getSolution(i)[1].toString())
                        + 3 * Integer.parseInt(search.getSolution(i)[2].toString()));
            }
            System.out.println("MAX INCOME  --> " + Collections.max(myList));

            int index = myList.indexOf(Collections.max(myList)) + 1;
            System.out.print("Solution " + index + ": ");
            for (int j = 0; j < 3; j++) {
                System.out.print(search.getSolution(index)[j] + " ");
            }

            text = "MAX INCOME --> " + Collections.max(myList) + "\n" + "Solution ID " + index + " : \n"
                    + "Product - 1: " + search.getSolution(index)[0].toString() + "\n"
                    + "Product - 2: " + search.getSolution(index)[1].toString() + "\n"
                    + "Product - 3: " + search.getSolution(index)[2].toString() + "\n";

            System.out.println();
        } else {
            System.out.println("Failed to find any solution");
        }

    }
}
