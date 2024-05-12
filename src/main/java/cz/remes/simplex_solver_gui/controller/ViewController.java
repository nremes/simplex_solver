package cz.remes.simplex_solver_gui.controller;

import cz.remes.simplex_solver_gui.SimplexSolver;
import cz.remes.simplex_solver_gui.enums.OptimizationType;
import cz.remes.simplex_solver_gui.enums.ResultHashMapIdentifier;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class ViewController implements Initializable {
    @FXML
    public TextField con1RightSide;

    @FXML
    public ComboBox<String> con1Sign;

    @FXML
    public TextField con1Var1;

    @FXML
    public TextField con1Var2;

    @FXML
    public TextField con1Var3;

    @FXML
    public TextField con1Var4;

    @FXML
    public TextField con1Var5;

    @FXML
    public TextField con1Var6;

    @FXML
    public TextField con1Var7;

    @FXML
    public TextField con2RightSide;

    @FXML
    public ComboBox<String> con2Sign;

    @FXML
    public TextField con2Var1;

    @FXML
    public TextField con2Var2;

    @FXML
    public TextField con2Var3;

    @FXML
    public TextField con2Var4;

    @FXML
    public TextField con2Var5;

    @FXML
    public TextField con2Var6;

    @FXML
    public TextField con2Var7;

    @FXML
    public TextField con3RightSide;

    @FXML
    public ComboBox<String> con3Sign;

    @FXML
    public TextField con3Var1;

    @FXML
    public TextField con3Var2;

    @FXML
    public TextField con3Var3;

    @FXML
    public TextField con3Var4;

    @FXML
    public TextField con3Var5;

    @FXML
    public TextField con3Var6;

    @FXML
    public TextField con3Var7;

    @FXML
    public TextField con4RightSide;

    @FXML
    public ComboBox<String> con4Sign;

    @FXML
    public TextField con4Var1;

    @FXML
    public TextField con4Var2;

    @FXML
    public TextField con4Var3;

    @FXML
    public TextField con4Var4;

    @FXML
    public TextField con4Var5;

    @FXML
    public TextField con4Var6;

    @FXML
    public TextField con4Var7;

    @FXML
    public TextField con5RightSide;

    @FXML
    public ComboBox<String> con5Sign;

    @FXML
    public TextField con5Var1;

    @FXML
    public TextField con5Var2;

    @FXML
    public TextField con5Var3;

    @FXML
    public TextField con5Var4;

    @FXML
    public TextField con5Var5;

    @FXML
    public TextField con5Var6;

    @FXML
    public TextField con5Var7;

    @FXML
    public TextField con6RightSide;

    @FXML
    public ComboBox<String> con6Sign;

    @FXML
    public TextField con6Var1;

    @FXML
    public TextField con6Var2;

    @FXML
    public TextField con6Var3;

    @FXML
    public TextField con6Var4;

    @FXML
    public TextField con6Var5;

    @FXML
    public TextField con6Var6;

    @FXML
    public TextField con6Var7;

    @FXML
    public TextField con7RightSide;

    @FXML
    public ComboBox<String> con7Sign;

    @FXML
    public TextField con7Var1;

    @FXML
    public TextField con7Var2;

    @FXML
    public TextField con7Var3;

    @FXML
    public TextField con7Var4;

    @FXML
    public TextField con7Var5;

    @FXML
    public TextField con7Var6;

    @FXML
    public TextField con7Var7;

    @FXML
    public TextField funVar1;

    @FXML
    public TextField funVar2;

    @FXML
    public TextField funVar3;

    @FXML
    public TextField funVar4;

    @FXML
    public TextField funVar5;

    @FXML
    public TextField funVar6;

    @FXML
    public TextField funVar7;

    @FXML
    public TextField constraintsCountTf;

    @FXML
    public ComboBox<OptimizationType> optimizationCb;

    @FXML
    public TextField variableCountTf;

    @FXML
    public TextField objectiveFunctionSolTf;

    @FXML
    public TextField dualProblemSolutionTf;

    @FXML
    public Label messageLb;


    private final int maxCount = 7;
    private final double[][] constraintsValues = new double[maxCount][maxCount];
    private final double[] functionValues = new double[maxCount];
    private final double[] rightSideValues = new double[maxCount];
    private final String[] signs = new String[maxCount];
    private final HashMap<ResultHashMapIdentifier, double[]> results = new HashMap<>();
    private OptimizationType optimizationType;
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    @FXML
    public void clean(ActionEvent event) {
        cleanWholeGui();
    }

    @FXML
    public void process(ActionEvent event) {
        try {
            cleanResults();
            final int variablesCount = Integer.parseInt(variableCountTf.getText());
            final int constraintsCount = Integer.parseInt(constraintsCountTf.getText());
            final double[][] nonZeroConValues = new double[constraintsCount][variablesCount];
            final double[] nonZeroFunValues = new double[variablesCount];
            final double[] nonZeroRightSideValues = new double[constraintsCount];
            final String[] nonZeroLinesSign = new String[constraintsCount];

            processDataFromGui();

            //Copy non-zero values from GUI to arrays
            for (int i = 0; i < constraintsCount; i++) {
                for (int j = 0; j < variablesCount; j++) {
                    nonZeroConValues[i][j] = constraintsValues[i][j];
                }
            }
            System.arraycopy(functionValues, 0, nonZeroFunValues, 0, variablesCount);
            System.arraycopy(rightSideValues, 0, nonZeroRightSideValues, 0, constraintsCount);
            System.arraycopy(signs, 0, nonZeroLinesSign, 0, constraintsCount);

            //Problem solving
            SimplexSolver.process(nonZeroFunValues, nonZeroConValues, nonZeroRightSideValues, nonZeroLinesSign, optimizationType, results);

            processResultToGui(results, nonZeroConValues.length, nonZeroFunValues.length);

        } catch (Exception e) {
            messageLb.setText(e.toString());
            log.error(e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String[] signs = {"<=", ">=", "="};
        optimizationCb.getItems().addAll(OptimizationType.MAXIMIZE, OptimizationType.MINIMIZE);
        optimizationCb.setValue(OptimizationType.MAXIMIZE);

        con1Sign.getItems().addAll(signs);
        con1Sign.setValue(signs[0]);

        con2Sign.getItems().addAll(signs);
        con2Sign.setValue(signs[0]);

        con3Sign.getItems().addAll(signs);
        con3Sign.setValue(signs[0]);

        con4Sign.getItems().addAll(signs);
        con4Sign.setValue(signs[0]);

        con5Sign.getItems().addAll(signs);
        con5Sign.setValue(signs[0]);

        con6Sign.getItems().addAll(signs);
        con6Sign.setValue(signs[0]);

        con7Sign.getItems().addAll(signs);
        con7Sign.setValue(signs[0]);
    }

    private void processDataFromGui() {
        //Values of function
        functionValues[0] = Double.parseDouble(funVar1.getText());
        functionValues[1] = Double.parseDouble(funVar2.getText());
        functionValues[2] = Double.parseDouble(funVar3.getText());
        functionValues[3] = Double.parseDouble(funVar4.getText());
        functionValues[4] = Double.parseDouble(funVar5.getText());
        functionValues[5] = Double.parseDouble(funVar6.getText());
        functionValues[6] = Double.parseDouble(funVar7.getText());

        //Values of constraints
        constraintsValues[0][0] = Double.parseDouble(con1Var1.getText());
        constraintsValues[0][1] = Double.parseDouble(con1Var2.getText());
        constraintsValues[0][2] = Double.parseDouble(con1Var3.getText());
        constraintsValues[0][3] = Double.parseDouble(con1Var4.getText());
        constraintsValues[0][4] = Double.parseDouble(con1Var5.getText());
        constraintsValues[0][5] = Double.parseDouble(con1Var6.getText());
        constraintsValues[0][6] = Double.parseDouble(con1Var7.getText());

        constraintsValues[1][0] = Double.parseDouble(con2Var1.getText());
        constraintsValues[1][1] = Double.parseDouble(con2Var2.getText());
        constraintsValues[1][2] = Double.parseDouble(con2Var3.getText());
        constraintsValues[1][3] = Double.parseDouble(con2Var4.getText());
        constraintsValues[1][4] = Double.parseDouble(con2Var5.getText());
        constraintsValues[1][5] = Double.parseDouble(con2Var6.getText());
        constraintsValues[1][6] = Double.parseDouble(con2Var7.getText());

        constraintsValues[2][0] = Double.parseDouble(con3Var1.getText());
        constraintsValues[2][1] = Double.parseDouble(con3Var2.getText());
        constraintsValues[2][2] = Double.parseDouble(con3Var3.getText());
        constraintsValues[2][3] = Double.parseDouble(con3Var4.getText());
        constraintsValues[2][4] = Double.parseDouble(con3Var5.getText());
        constraintsValues[2][5] = Double.parseDouble(con3Var6.getText());
        constraintsValues[2][6] = Double.parseDouble(con3Var7.getText());

        constraintsValues[3][0] = Double.parseDouble(con4Var1.getText());
        constraintsValues[3][1] = Double.parseDouble(con4Var2.getText());
        constraintsValues[3][2] = Double.parseDouble(con4Var3.getText());
        constraintsValues[3][3] = Double.parseDouble(con4Var4.getText());
        constraintsValues[3][4] = Double.parseDouble(con4Var5.getText());
        constraintsValues[3][5] = Double.parseDouble(con4Var6.getText());
        constraintsValues[3][6] = Double.parseDouble(con4Var7.getText());

        constraintsValues[4][0] = Double.parseDouble(con5Var1.getText());
        constraintsValues[4][1] = Double.parseDouble(con5Var2.getText());
        constraintsValues[4][2] = Double.parseDouble(con5Var3.getText());
        constraintsValues[4][3] = Double.parseDouble(con5Var4.getText());
        constraintsValues[4][4] = Double.parseDouble(con5Var5.getText());
        constraintsValues[4][5] = Double.parseDouble(con5Var6.getText());
        constraintsValues[4][6] = Double.parseDouble(con5Var7.getText());

        constraintsValues[5][0] = Double.parseDouble(con6Var1.getText());
        constraintsValues[5][1] = Double.parseDouble(con6Var2.getText());
        constraintsValues[5][2] = Double.parseDouble(con6Var3.getText());
        constraintsValues[5][3] = Double.parseDouble(con6Var4.getText());
        constraintsValues[5][4] = Double.parseDouble(con6Var5.getText());
        constraintsValues[5][5] = Double.parseDouble(con6Var6.getText());
        constraintsValues[5][6] = Double.parseDouble(con6Var7.getText());

        constraintsValues[6][0] = Double.parseDouble(con7Var1.getText());
        constraintsValues[6][1] = Double.parseDouble(con7Var2.getText());
        constraintsValues[6][2] = Double.parseDouble(con7Var3.getText());
        constraintsValues[6][3] = Double.parseDouble(con7Var4.getText());
        constraintsValues[6][4] = Double.parseDouble(con7Var5.getText());
        constraintsValues[6][5] = Double.parseDouble(con7Var6.getText());
        constraintsValues[6][6] = Double.parseDouble(con7Var7.getText());

        //Right sides of constraints
        rightSideValues[0] = Double.parseDouble(con1RightSide.getText());
        rightSideValues[1] = Double.parseDouble(con2RightSide.getText());
        rightSideValues[2] = Double.parseDouble(con3RightSide.getText());
        rightSideValues[3] = Double.parseDouble(con4RightSide.getText());
        rightSideValues[4] = Double.parseDouble(con5RightSide.getText());
        rightSideValues[5] = Double.parseDouble(con6RightSide.getText());
        rightSideValues[6] = Double.parseDouble(con7RightSide.getText());

        //Signs of constraints
        signs[0] = con1Sign.getValue();
        signs[1] = con2Sign.getValue();
        signs[2] = con3Sign.getValue();
        signs[3] = con4Sign.getValue();
        signs[4] = con5Sign.getValue();
        signs[5] = con6Sign.getValue();
        signs[6] = con7Sign.getValue();

        //Objective
        optimizationType = optimizationCb.getValue();
    }

    private void processResultToGui(HashMap<ResultHashMapIdentifier, double[]> results, int m, int n) {
        StringBuilder sb = new StringBuilder();
        final double[] objFunSol = results.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_SOLUTION);
        final double[] objFunValue = results.get(ResultHashMapIdentifier.OBJECTIVE_FUNCTION_VALUE);
        final double[] dualFunSol = results.get(ResultHashMapIdentifier.DUAL_PROBLEM_SOLUTION);

        //objective function solution
        for (int i = 0; i < objFunSol.length; i++) {
            if (i == 0) {
                sb.append(String.format("x%d = %.2f", i + 1, objFunSol[i]));
                continue;
            }
            sb.append(String.format("  |  x%d = %.2f", i + 1, objFunSol[i]));
        }

        //objective function value
        sb.append(String.format("  |  f(x) = %.2f", objFunValue[0]));

        objectiveFunctionSolTf.setText(sb.toString());

        //dual problem solution
        sb = new StringBuilder();
        for (int i = 0; i < dualFunSol.length; i++) {
            if (i == 0) {
                sb.append(String.format("x%d = %.2f", n + i + 1, dualFunSol[i]));
                continue;
            }
            sb.append(String.format("  |  x%d = %.2f", n + i + 1, dualFunSol[i]));
        }
        dualProblemSolutionTf.setText(sb.toString());
    }

    private void cleanResults() {
        messageLb.setText("");
        objectiveFunctionSolTf.clear();
        dualProblemSolutionTf.clear();
    }

    private void cleanWholeGui() {
        cleanResults();

        variableCountTf.clear();
        constraintsCountTf.clear();
        optimizationCb.setValue(OptimizationType.MAXIMIZE);

        //function
        funVar1.setText("0");
        funVar2.setText("0");
        funVar3.setText("0");
        funVar4.setText("0");
        funVar5.setText("0");
        funVar6.setText("0");
        funVar7.setText("0");

        //constraints
        con1Var1.setText("0");
        con1Var2.setText("0");
        con1Var3.setText("0");
        con1Var4.setText("0");
        con1Var5.setText("0");
        con1Var6.setText("0");
        con1Var7.setText("0");
        con1Sign.setValue("<=");
        con1RightSide.setText("0");

        con2Var1.setText("0");
        con2Var2.setText("0");
        con2Var3.setText("0");
        con2Var4.setText("0");
        con2Var5.setText("0");
        con2Var6.setText("0");
        con2Var7.setText("0");
        con2Sign.setValue("<=");
        con2RightSide.setText("0");

        con3Var1.setText("0");
        con3Var2.setText("0");
        con3Var3.setText("0");
        con3Var4.setText("0");
        con3Var5.setText("0");
        con3Var6.setText("0");
        con3Var7.setText("0");
        con3Sign.setValue("<=");
        con3RightSide.setText("0");

        con4Var1.setText("0");
        con4Var2.setText("0");
        con4Var3.setText("0");
        con4Var4.setText("0");
        con4Var5.setText("0");
        con4Var6.setText("0");
        con4Var7.setText("0");
        con4Sign.setValue("<=");
        con4RightSide.setText("0");

        con5Var1.setText("0");
        con5Var2.setText("0");
        con5Var3.setText("0");
        con5Var4.setText("0");
        con5Var5.setText("0");
        con5Var6.setText("0");
        con5Var7.setText("0");
        con5Sign.setValue("<=");
        con5RightSide.setText("0");

        con6Var1.setText("0");
        con6Var2.setText("0");
        con6Var3.setText("0");
        con6Var4.setText("0");
        con6Var5.setText("0");
        con6Var6.setText("0");
        con6Var7.setText("0");
        con6Sign.setValue("<=");
        con6RightSide.setText("0");

        con7Var1.setText("0");
        con7Var2.setText("0");
        con7Var3.setText("0");
        con7Var4.setText("0");
        con7Var5.setText("0");
        con7Var6.setText("0");
        con7Var7.setText("0");
        con7Sign.setValue("<=");
        con7RightSide.setText("0");
    }
}
