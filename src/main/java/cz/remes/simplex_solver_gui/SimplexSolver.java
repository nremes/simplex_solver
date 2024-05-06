package cz.remes.simplex_solver_gui;

import cz.remes.simplex_solver_gui.enums.ResultHashMapIdentifier;
import cz.remes.simplex_solver_gui.enums.OptimizationType;
import cz.remes.simplex_solver_gui.enums.SolverType;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

public class SimplexSolver {

    private final static String GREATER_OR_EQUALS = ">=";
    private final static String LOWER_OR_EQUALS = "<=";
    private final static String EQUALS = "=;";
    private static OptimizationType optimizationType;
    private static boolean auxiliaryObjectiveFunctionExists = false;
    private static final DecimalFormat df = new DecimalFormat("#.###");


    public static void main(String[] args) {
        final HashMap<ResultHashMapIdentifier, double[]> results = new HashMap<>();
        final double[] function = {2,1};
        final double[][] constraints = {{3,1}, {-1,1}, {0,1}};
        final double[] constraintsRightSide = {12,4,10};
        final String[] signs = {">=", ">=", "<="};

        /*final double[] function = {2,1};
        final double[][] constraints = {{3,1}, {11,1}, {0,1}};
        final double[] constraintsRightSide = {12,4,10};
        final String[] signs = {"<=", "<=", "<="};*/

        final OptimizationType optimizationType = OptimizationType.MAXIMIZE;

        process(function, constraints, constraintsRightSide, signs, optimizationType, results);
        printResult(results, constraints.length, function.length);
    }

    /**
     *
     * @param c function values
     * @param A constraints values
     * @param b right side of constraints
     * @param signs constraints signs
     * @param opt optimization type (Optimization enum)
     * @param results HashMap that will contain objective function value, objective function solution, dual function solution at the end of method
     */
    public static void process(double[] c, double[][] A, double[] b, String[] signs, OptimizationType opt, HashMap<ResultHashMapIdentifier, double[]> results) {
        optimizationType = opt;
        SolverType solverType = getSolver(signs);

        if (solverType == SolverType.ONEPHASE_SOLVER) {
            solveOnePhaseProblem(c, A, b, results);
        } else if (solverType == SolverType.MINIMIZATION) {
            minimize(c, A, b, results);
        } else {
            auxiliaryObjectiveFunctionExists = true;
            solveTwoPhaseProblem(c, A, b, results, signs);
        }
    }

    private static void solveOnePhaseProblem(double[] c, double[][] A, double[] b, HashMap<ResultHashMapIdentifier, double[]> results) {
        System.out.println(SolverType.ONEPHASE_SOLVER);
        final int m = A.length;
        final int n = c.length;
        final double[][] table = new double[m + 1][n + m + 1];

        //initialize table
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, table[i], 0, n);
            table[i][n + i] = 1;
            table[i][n + m] = b[i];
        }
        for (int i = 0; i < n; i++) {
            table[m][i] = -c[i];
        }

        simplex(table, results, m, n);
    }

    private static void solveTwoPhaseProblem(double[] c, double[][] A, double[] b, HashMap<ResultHashMapIdentifier, double[]> results, String[] signs) {
        System.out.println(SolverType.TWOPHASE_SOLVER);
        final int auxiliaryVarsCount = getCountOfAuxiliaryVars(signs);
        final int m = A.length;
        final int n = c.length;
        double[][] table = new double[m + 2][n + m + auxiliaryVarsCount + 1];

        //initialize table
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, table[i], 0, n);
            if (signs[i].equals(LOWER_OR_EQUALS)) {
                table[i][n + i] = 1;
            } else if (signs[i].equals(GREATER_OR_EQUALS)) {
                table[i][n + i] = -1;
                table[i][n + auxiliaryVarsCount + i] = 1;
            } else if (signs[i].equals(EQUALS)) {
                table[i][n + auxiliaryVarsCount + i] = 1;
            }
        }

        //put right side of each constraint into table
        for (int i = 0; i < m; i++) {
            table[i][table[i].length - 1] = b[i];
        }

        //put negatived objective function into table
        for (int i = 0; i < c.length; i++) {
            c[i] = -c[i];
        }
        System.arraycopy(c, 0, table[table.length - 2], 0, n);

        //create auxiliary function a put it into table
        double[][] constraintsWithAuxiliaryVar = new double[auxiliaryVarsCount][];
        int counter = 0;
        for (int i = 0; i < m; i++) {
            if (table[i][n + i] == -1) {
                constraintsWithAuxiliaryVar[counter] = table[i];
                counter++;
            }
        }
        double[] auxiliaryFunc = new double[n + m];
        double auxiliaryFuncValue = 0;
        for (int i = 0; i < constraintsWithAuxiliaryVar.length; i++) {
            for (int j = 0; j < auxiliaryFunc.length; j++) {
                auxiliaryFunc[j] += constraintsWithAuxiliaryVar[i][j];
            }
            auxiliaryFuncValue += constraintsWithAuxiliaryVar[i][table[0].length - 1];
        }
        System.arraycopy(auxiliaryFunc, 0, table[table.length - 1], 0, n + m);
        table[table.length - 1][table[0].length - 1] = auxiliaryFuncValue;



        while (true) {
            System.out.println("**********************************");
            printTable(table);

            //hledani klicoveho radku
            int q = 0;
            for (int i = 0; i < n + m; i++) {
                if (table[m + 1][i] > table[m + 1][q]) {
                    q = i;
                }
            }

            //je reseni optimalni?
            boolean isOptimal = false;
            for (int i = 0; i < n + m; i++) {
                if (table[m + 1][i] <= 0) {
                    isOptimal = true;
                } else {
                    isOptimal = false;
                    break;
                }
            }
            if (isOptimal) {
                break;
            }

            int p = 0;
            for (int i = 0; i <= m + 1; i++) {
                if (table[i][q] > 0) {
                    p = i;
                    break;
                }
            }
            if (table[p][q] <= 0) {
                throw new ArithmeticException("Linear program is unbounded");
            }

            //hledani klicoveho sloupce
            for (int i = p + 1; i < m; i++) {
                if (table[i][q] > 0) {
                    double alpha = table[i][n + m + auxiliaryVarsCount] / table[i][q];
                    if (alpha < table[p][n + m + auxiliaryVarsCount] / table[p][q]) {
                        p = i;
                    }
                }
            }

            //pivot
            for (int i = 0; i <= m + 1; i++) {
                if (i != p) {
                    double alpha = table[i][q] / table[p][q];
                    for (int j = 0; j <= n + m + auxiliaryVarsCount; j++) {
                        table[i][j] = Double.parseDouble(df.format(table[i][j]).replace(",", ".")) - Double.parseDouble(df.format(alpha).replace(",", ".")) * Double.parseDouble(df.format(table[p][j]).replace(",", "."));
                    }
                }
            }

            //scale pivot row
            double pivot = table[p][q];
            for (int j = 0; j <= n + m + auxiliaryVarsCount; j++) {
                table[p][j] = Double.parseDouble(df.format(table[p][j]).replace(",", ".")) / Double.parseDouble(df.format(pivot).replace(",", "."));
            }
        }

        double[][] newTable = new double[m + 1][n + m + 1];
        for (int i = 0; i < table.length - 1; i++) {
            for (int j = 0; j < table[i].length; j++) {
                if (j < n + m) {
                    newTable[i][j] = Double.parseDouble(df.format(table[i][j]).replace(",", "."));
                } else if (j == n + m + auxiliaryVarsCount) {
                    newTable[i][j - auxiliaryVarsCount] = Double.parseDouble(df.format(table[i][j]).replace(",", "."));
                }
            }
        }

        simplex(newTable, results, m, n);
    }

    private static void minimize(double[] c, double[][] A, double[] b, HashMap<ResultHashMapIdentifier, double[]> results) {
        System.out.println(SolverType.MINIMIZATION);
        double[] transposedC = new double[b.length];
        double[][] transposedA = new double[A[0].length][A.length];
        double[] transposedB = new double[c.length];

        //transpose c to b
        System.arraycopy(c, 0, transposedB, 0, c.length);

        //transpose b to c
        System.arraycopy(b, 0, transposedC, 0, b.length);

        //transpose A to A
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                transposedA[j][i] = A[i][j];
            }
        }

        solveOnePhaseProblem(transposedC, transposedA, transposedB, results);
    }

    private static void simplex(double[][] table, HashMap<ResultHashMapIdentifier, double[]> results, int m, int n) {
        //Simplex solving
        while (true) {
            System.out.println("**********************************");
            printTable(table);

            //hledani klicove sloupce
            int q = 0;
            for (int i = 0; i < n + m; i++) {
                if (table[m][i] < table[m][q]) {
                    q = i;
                }
            }

            //je reseni optimalni?
            boolean isOptimal = false;
            for (int i = 0; i < n + m; i++) {
                if (table[m][i] >= 0) {
                    isOptimal = true;
                } else {
                    isOptimal = false;
                    break;
                }
            }
            if (isOptimal) {
                break;
            }

            int p = 0;
            for (int i = 0; i < m; i++) {
                if (table[i][q] > 0) {
                    p = i;
                    break;
                }
            }
            if (table[p][q] <= 0) {
                throw new ArithmeticException("Linear program is unbounded");
            }

            for (int i = p + 1; i < m; i++) {
                if (table[i][q] > 0) {
                    double alpha = table[i][n + m] / table[i][q];
                    if (alpha < table[p][n + m] / table[p][q]) {
                        p = i;
                    }
                }
            }

            //pivot
            for (int i = 0; i <= m; i++) {
                if (i != p) {
                    double alpha = table[i][q] / table[p][q];
                    for (int j = 0; j <= n + m; j++) {
                        table[i][j] =  Double.parseDouble(df.format(table[i][j]).replace(",", ".")) - Double.parseDouble(df.format(alpha).replace(",", ".")) * Double.parseDouble(df.format(table[p][j]).replace(",", "."));
                    }
                }
            }

            //scale pivot row
            double pivot = table[p][q];
            for (int j = 0; j <= n + m; j++) {
                table[p][j] = Double.parseDouble(df.format(table[p][j]).replace(",", ".")) / Double.parseDouble(df.format(pivot).replace(",", "."));
            }
        }

        //solution
        double[] resultsArr = new double[n + m]; //plus dual problem solution
        for (int i = 0; i < n + m; i++) {
            double result = 0;
            Integer resultRow = null;
            boolean resultWasRead = false;
            for (int j = 0; j < table.length - 1; j++) {
                if ((int) table[j][i] == 1 && !resultWasRead) {
                    result = table[j][n + m];
                    resultWasRead = true;
                    resultRow = j;
                } else if (table[j][i] == 0) {
                    continue;
                } else {
                    break;
                }
            }
            resultsArr[i] = result;
            if (resultRow != null && result != 0) {
                table[resultRow][n + m] = 0;
            }
        }
        results.put(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION, resultsArr);

        //objective function solution
        results.put(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE, new double[]{table[table.length - 1][n + m]});

        //dual function solution
        double[] dualFunSol = new double[m];
        System.arraycopy(table[table.length - 1], n, dualFunSol, 0, m);
        results.put(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION, dualFunSol);
    }

    private static int getCountOfAuxiliaryVars(String[] signs) {
        int count = 0;
        for (String sign : signs){
            if (!sign.equals(LOWER_OR_EQUALS)) {
                count++;
            }
        }

        return count;
    }

    private static SolverType getSolver(String[] signs) {
        boolean allSignsAreSame = Arrays.stream(signs).allMatch(value -> value.equals(signs[0]));
        if (allSignsAreSame && signs[0].equals(LOWER_OR_EQUALS) && optimizationType == OptimizationType.MAXIMIZE) {
            return SolverType.ONEPHASE_SOLVER;
        } else if (allSignsAreSame && signs[0].equals(GREATER_OR_EQUALS) && optimizationType == OptimizationType.MINIMIZE) {
            return SolverType.MINIMIZATION;
        } else {
            return SolverType.TWOPHASE_SOLVER;
        }
    }

    private static void printTable(double[][] table) {
        int objectiveFuncCount = auxiliaryObjectiveFunctionExists ? 2 : 1;
        for (int i = 0; i < table.length; i++) {
            if (i == table.length - objectiveFuncCount) {
                System.out.println("---------------------------------");
            }
            for (int j = 0; j < table[i].length; j++) {
                System.out.printf("| %.3f |", table[i][j]);
            }
            System.out.print("\n");
        }
    }

    public static void printResult(HashMap<ResultHashMapIdentifier, double[]> results, int n, int m) {
        final double[] objFunSol = results.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION);
        final double[] objFunValue = results.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE);
        final double[] dualFunSol = results.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION);

        System.out.println("------ RESULT ------");
        for (int i = 0; i < objFunSol.length; i++) {
            System.out.printf("x%d = %.2f\n", i + 1, objFunSol[i]);
        }
        System.out.printf("f(x) = %.2f\n", objFunValue[0]);
        System.out.println("\n------ DUAL PROBLEM SOLUTION ------");
        for (int i = 0; i < dualFunSol.length; i++) {
            System.out.printf("x%d = %.2f\n", n + 1 + i, dualFunSol[i]);
        }
    }
}