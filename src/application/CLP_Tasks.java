package application;

// import core.BinPackProduct;
// import core.HidatoSolver;
import gui.AppFrame;

/**
 * Class is created for solving the problem - it has objects of all classes.It
 * models and searches with instances of classes.
 *
 * @author Hüsnü ISLEYEN
 * @version 1.0
 */
public class CLP_Tasks {

    /**
     * MAIN METHOD - Starts to GUI, to solve problems.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*
         //object creation - modelling and search
         System.out.println("HidatoSolver class - Combinatorial Problem - Problem 1");
         HidatoSolver q1 = new HidatoSolver();
         q1.model();
         q1.search();

         System.out.println("BinPacking & Product Scheduling/Planning");
         BinPackProduct q2 = new BinPackProduct();
         System.out.println("Trucks - (BinPacking)");
         q2.model();
         q2.search();

         System.out.println("Product Scheduling - Max Income");
         q2.resourceAllocation();
         q2.searchMaxIncome();
         */
        AppFrame myGui = new AppFrame();
        myGui.setVisible(true);

    }

}
