package cz.remes.simplex_solver_gui;

import cz.remes.simplex_solver_gui.enums.OptimizationType;
import cz.remes.simplex_solver_gui.enums.ResultHashMapIdentifier;
import cz.remes.simplex_solver_gui.enums.SolverType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SimplexSolverTest {

    @Test
    void maximizeTwoPhaseTest() {
        final HashMap<ResultHashMapIdentifier, double[]> results = new HashMap<>();
        final double[] function = {2,1};
        final double[][] constraints = {{3,1}, {-1,1}, {0,1}};
        final double[] constraintsRightSide = {12,4,10};
        final String[] signs = {">=", ">=", "<="};
        final OptimizationType optimizationType = OptimizationType.MAXIMIZE;

        SimplexSolver.process(function, constraints, constraintsRightSide, signs, optimizationType, results);
        compareResults(createExpectedResults(new double[]{6,10,16,0,0}, new double[]{22}, new double[]{0,2,3}), results);
    }

    @Test
    void maximizeOnePhaseTest() {
        final HashMap<ResultHashMapIdentifier, double[]> results = new HashMap<>();
        final double[] function = {2,1};
        final double[][] constraints = {{3,1}, {11,1}, {0,1}};
        final double[] constraintsRightSide = {12,4,10};
        final String[] signs = {"<=", "<=", "<="};
        final OptimizationType optimizationType = OptimizationType.MAXIMIZE;

        SimplexSolver.process(function, constraints, constraintsRightSide, signs, optimizationType, results);
        compareResults(createExpectedResults(new double[]{0,4,8,0,6}, new double[]{4}, new double[]{0,1,0}), results);
    }

    @Test
    void maximizeTwoPhaseUnboundedTest() {
        final HashMap<ResultHashMapIdentifier, double[]> results = new HashMap<>();
        final double[] function = {5,1,3};
        final double[][] constraints = {{3,1,3}, {2,-2,2}};
        final double[] constraintsRightSide = {5,2};
        final String[] signs = {">=", "<="};
        final OptimizationType optimizationType = OptimizationType.MAXIMIZE;

        try {
            SimplexSolver.process(function, constraints, constraintsRightSide, signs, optimizationType, results);
            fail("Linear program is bounded");
        } catch (ArithmeticException ignored) {}
    }

    @Test
    void maximizeTwoPhaseSolutionNotExistTest() {
        final HashMap<ResultHashMapIdentifier, double[]> results = new HashMap<>();
        final double[] function = {63,27,56};
        final double[][] constraints = {{2,2,3}, {1,2,2}};
        final double[] constraintsRightSide = {16,20};
        final String[] signs = {"<=", ">="};
        final OptimizationType optimizationType = OptimizationType.MINIMIZE;

        try {
            SimplexSolver.process(function, constraints, constraintsRightSide, signs, optimizationType, results);
            fail("Linear program is feasible");
        } catch (ArithmeticException ignored) {}
    }

    private void compareResults(HashMap<ResultHashMapIdentifier, double[]> expected, HashMap<ResultHashMapIdentifier, double[]> actual) {
        //objective function solution
        assertEquals(expected.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION).length, actual.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION).length);

        for (int i = 0; i < expected.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION).length; i++) {
            assertEquals(expected.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION)[i], actual.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION)[i]);
        }

        //objective function value
        assertEquals(expected.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE).length, actual.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE).length);

        for (int i = 0; i < expected.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE).length; i++) {
            assertEquals(expected.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE)[i], actual.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE)[i]);
        }

        //dual problem solution
        assertEquals(expected.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION).length, actual.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION).length);

        for (int i = 0; i < expected.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION).length; i++) {
            assertEquals(expected.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION)[i], actual.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION)[i]);
        }
    }

    private HashMap<ResultHashMapIdentifier, double[]> createExpectedResults(double[] objectiveFunctionSolution, double[] objectiveFunctionValue, double[] dualProblemSolution) {
        final HashMap<ResultHashMapIdentifier, double[]> results = new HashMap<>();
        results.put(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION, objectiveFunctionSolution);
        results.put(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE, objectiveFunctionValue);
        results.put(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION, dualProblemSolution);

        return results;
    }
}