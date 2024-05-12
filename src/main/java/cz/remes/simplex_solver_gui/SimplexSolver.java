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
    private static int auxiliaryVarsCount = 0;
    private static final DecimalFormat df = new DecimalFormat("#.###");


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
        auxiliaryVarsCount = 0;
        auxiliaryObjectiveFunctionExists = false;

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
        double[][] table = new double[m + 1][n + m + 1];

        //initialize table
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, table[i], 0, n);
            table[i][n + i] = 1;

            //put right side of each constraint into table
            table[i][n + m] = b[i];
        }
        for (int i = 0; i < n; i++) {
            table[m][i] = -c[i];
        }

        simplex(table, m, n);
        roundTable(table, 2);
        processResults(table, n, m, results);
    }

    private static void solveTwoPhaseProblem(double[] c, double[][] A, double[] b, HashMap<ResultHashMapIdentifier, double[]> results, String[] signs) {
        System.out.println(SolverType.TWOPHASE_SOLVER);
        auxiliaryVarsCount = getCountOfAuxiliaryVars(signs);
        final int m = A.length;
        final int n = c.length;
        double[][] table = new double[m + 2][n + m + auxiliaryVarsCount + 1];

        //initialize table
        int auxiliaryVarWritten = 0;
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, table[i], 0, n);
            if (signs[i].equals(LOWER_OR_EQUALS)) {
                table[i][n + i] = 1;
            } else if (signs[i].equals(GREATER_OR_EQUALS)) {
                table[i][n + i] = -1;
                table[i][n + m + auxiliaryVarWritten++] = 1;
            } else if (signs[i].equals(EQUALS)) {
                table[i][n + auxiliaryVarsCount + i + 1] = 1;
            }

            //put right side of each constraint into table
            table[i][table[i].length - 1] = b[i];
        }

        //put negatived objective function into table
        for (int i = 0; i < n; i++) {
            c[i] = -c[i];
        }
        System.arraycopy(c, 0, table[table.length - 2], 0, n);

        //find count of auxiliary variables
        double[][] constraintsWithAuxiliaryVar = new double[auxiliaryVarsCount][];
        int counter = 0;
        for (int i = 0; i < table.length - 2; i++) {
            if (n + m +  counter == table[0].length - 1) {
                break;
            }
            if (table[i][n + m + counter] == 1) {
                constraintsWithAuxiliaryVar[counter] = table[i];
                counter++;
            }
        }

        //create auxiliary function a put it into table
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

        simplex(table, m, n);

        checkProblemIsFeasible(table, m, n);

        //create table without auxiliary objective function and solve one-phase simplex
        double[][] newTable = new double[m + 1][n + m + 1];
        for (int i = 0; i < table.length - 1; i++) {
            for (int j = 0; j < table[i].length; j++) {
                if (j < n + m) {
                    newTable[i][j] = table[i][j];
                } else if (j == n + m + auxiliaryVarsCount) {
                    newTable[i][j - auxiliaryVarsCount] = table[i][j];
                }
            }
        }

        auxiliaryObjectiveFunctionExists = false;
        auxiliaryVarsCount = 0;

        simplex(newTable, m, n);
        roundTable(table, 2);
        processResults(newTable, n, m, results);

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

        for (int i = 0; i < results.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION).length; i++) {
            double temp;
            temp = results.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION)[i];
            results.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION)[i] = results.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION)[i];
            results.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION)[i] = temp;
        }
    }

    private static void simplex(double[][] table, int m, int n) {
        //Simplex solving
        while (true) {
            //is solution optimal?
            boolean isOptimal = false;
            for (int i = 0; i < n + m; i++) {
                if (auxiliaryObjectiveFunctionExists) {
                    if (table[m + 1][i] <= 0) {
                        isOptimal = true;
                    } else {
                        isOptimal = false;
                        break;
                    }
                } else {
                    if (table[m][i] >= 0) {
                        isOptimal = true;
                    } else {
                        isOptimal = false;
                        break;
                    }
                }
            }
            if (isOptimal) {
                break;
            }

            //search key column
            int q = 0;
            for (int i = 0; i < n + m; i++) {
                if (auxiliaryObjectiveFunctionExists) {
                    if (table[m + 1][i] > table[m + 1][q]) {
                        q = i;
                    }
                } else {
                    if (table[m][i] < table[m][q]) {
                        q = i;
                    }
                }
            }

            //is problem unbounded?
            int p = 0;
            if (auxiliaryObjectiveFunctionExists) {
                for (int i = 0; i <= m + 1; i++) {
                    if (table[i][q] > 0) {
                        p = i;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < m; i++) {
                    if (table[i][q] > 0) {
                        p = i;
                        break;
                    }
                }
            }

            if (table[p][q] <= 0) {
                throw new ArithmeticException("Linear program is unbounded");
            }

            //search key row
            for (int i = p; i < m; i++) {
                if (table[i][q] > 0) {
                    double alpha = table[i][n + m + auxiliaryVarsCount] / table[i][q];
                    if (alpha < table[p][n + m + auxiliaryVarsCount] / table[p][q]) {
                        p = i;
                    }
                }
            }

            /*
            Key value is held at table[p][q]
            New optimal value is found as
             - we count alpha which is division of value at current row and key column and key value
             - optimal values if counted as difference of value that is optimized and multiple of alpha and key value
             */
            int actualRowCount = auxiliaryObjectiveFunctionExists ? m + 1 : m;
            for (int i = 0; i <= actualRowCount; i++) {
                if (i != p) {
                    double alpha = table[i][q] / table[p][q];
                    for (int j = 0; j <= n + m + auxiliaryVarsCount; j++) {
                        table[i][j] =  table[i][j] - (alpha * table[p][j]);
                    }
                }
            }

            //key row optimizing
            double pivot = table[p][q];
            for (int j = 0; j <= n + m + auxiliaryVarsCount; j++) {
                table[p][j] = table[p][j] / pivot;
            }
        }
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

    private static void processResults(double[][] table, int n, int m, HashMap<ResultHashMapIdentifier, double[]> results) {
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

    private static void roundTable(double[][] table, int decimalPlaces) {
        String decPlaces = "%." + decimalPlaces + "f";
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                table[i][j] = Double.parseDouble(String.format(decPlaces, table[i][j]).replace(",", "."));
            }
        }
    }

    private static void checkProblemIsFeasible(double[][] table, int m, int n) {
        boolean isInfeasible = false;
        // Check if Phase One LP problem is infeasible
        for (int j = n + m; j < n + m + auxiliaryVarsCount; j++) {
            for (int i = 0; i < m; i++) {
                if (table[i][j] == 0 || table[i][j] == 1) {
                    isInfeasible = true; // Problem is infeasible
                } else {
                    isInfeasible = false;
                    break;
                }
            }
            if (isInfeasible) {
                break;
            }
        }

        if (isInfeasible) {
            throw  new ArithmeticException("Problem is infeasible");
        }
    }
}