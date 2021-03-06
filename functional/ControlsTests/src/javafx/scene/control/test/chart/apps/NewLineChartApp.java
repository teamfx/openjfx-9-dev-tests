/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package javafx.scene.control.test.chart.apps;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import static javafx.scene.control.test.chart.apps.CommonFunctions.*;
import javafx.scene.control.test.utils.CommonPropertiesScene;
import javafx.scene.control.test.utils.ptables.PropertiesTable;
import javafx.scene.control.test.utils.ptables.TabPaneWithControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import test.javaclient.shared.InteroperabilityApp;
import test.javaclient.shared.Utils;

/**
 * @author Alexander Kirov
 */
public class NewLineChartApp extends InteroperabilityApp implements ChartIDsInterface {

    public final static String REMOVE_ITEM_POS_TEXT_FIELD_ID = "REMOVE_ITEM_POS_TEXT_FIELD_ID";
    public final static String REMOVE_BUTTON_ID = "REMOVE_BUTTON_ID";
    public final static String ADD_LINE_TEXT_FIELD_ID = "ADD_LINE_TEXT_FIELD_ID";
    public final static String ADD_LINE_AT_POSITION_TEXT_FIELD_ID = "ADD_ITEM_POSITION_TEXT_FIELD_ID";
    public final static String ADD_LINE_BUTTON_ID = "ADD_ITEM_BUTTON_ID";
    public final static String REMOVE_LINE_FROM_POS_TEXT_FIELD_ID = "REMOVE_AREA_POS_TEXT_FIELD_ID";
    public final static String REMOVE_INDEX_TEXT_FIELD_ID = "REMOVE_INDEX_TEXT_FIELD_ID";
    public final static String ADD_LINE_POS_TEXT_FIELD_ID = "ADD_AREA_POS_TEXT_FIELD_ID";
    public final static String ADD_INDEX_TEXT_FIELD_ID = "ADD_INDEX_TEXT_FIELD_ID";
    public final static String ADD_VALUE_TEXT_FIELD_ID = "ADD_VALUE_TEXT_FIELD_ID";

    public static void main(String[] args) {
        Utils.launch(NewLineChartApp.class, args);
    }

    @Override
    protected Scene getScene() {
        Utils.setTitleToStage(stage, "LineChartTestApp");
        return new NewLineChartApp.LineChartScene();
    }

    class LineChartScene extends CommonPropertiesScene {

        //LineChart to be tested.
        LineChart testedLineChart;
        NumberAxis axis1;
        NumberAxis axis2;
        TabPaneWithControl pane;

        public LineChartScene() {
            super("LineChart", 1300, 800);
        }

        @Override
        final protected void prepareScene() {
            Utils.addBrowser(this);

            axis1 = new NumberAxis(0, 100, 10);
            axis2 = new NumberAxis(0, 100, 10);
            testedLineChart = getNewChart();
            testedLineChart.setId(TESTED_CHART_ID);

            Button hardResetButton = new Button("Hard reset");
            hardResetButton.setId(HARD_RESET_BUTTON_ID);
            hardResetButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    HBox hb = (HBox) getRoot();
                    hb.getChildren().clear();
                    prepareMainSceneStructure();
                    prepareScene();
                }
            });

            Button softResetButton = new Button("Soft reset");
            softResetButton.setId(SOFT_RESET_BUTTON_ID);
            softResetButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    //throw new UnsupportedOperationException("Not supported yet.");
                }
            });

            HBox resetButtonsHBox = new HBox();
            resetButtonsHBox.getChildren().addAll(hardResetButton, softResetButton);

            VBox vb = new VBox(5);
            vb.getChildren().addAll(resetButtonsHBox, getAddLineHBox(), getAddPointToDataDialog(), getRemoveDataDialog(), getRemovePointFromDataDialog());

            pane = getPaneFor(testedLineChart, CHART_TAB_NAME, axis1, AXIS_1_TAB_NAME, axis2, AXIS_2_TAB_NAME);

            setTestedControlContainerSize(500, 500);
            setTestedControl(testedLineChart);
            setPropertiesContent(pane);
            setControllersContent(vb);
        }

        public HBox getAddPointToDataDialog() {
            HBox hb = new HBox();

            Label lb1 = new Label("To area");
            final TextField tf1 = new TextField("0");
            tf1.setPrefWidth(50);
            tf1.setId(ADD_LINE_POS_TEXT_FIELD_ID);

            Label lb2 = new Label("to index");
            final TextField tf2 = new TextField("0");
            tf2.setPrefWidth(50);
            tf2.setId(ADD_INDEX_TEXT_FIELD_ID);

            Label lb3 = new Label("value");
            final TextField tf3 = new TextField("0");
            tf3.setPrefWidth(50);
            tf3.setId(ADD_VALUE_TEXT_FIELD_ID);

            Button bt = new Button("add point!");
            bt.setId(REMOVE_BUTTON_ID);
            bt.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    int area = Integer.parseInt(tf1.getText());
                    int point = Integer.parseInt(tf2.getText());
                    double value = Double.parseDouble(tf3.getText());

                    XYChart.Data newData = new XYChart.Data();
                    newData.setXValue((((Double) ((XYChart.Data) (((XYChart.Series) testedLineChart.getData().get(area)).getData().get(point))).getXValue()) + ((Double) ((XYChart.Data) (((XYChart.Series) testedLineChart.getData().get(area)).getData().get(point - 1))).getXValue())) / 2);
                    newData.setYValue(value);

                    ((XYChart.Series) testedLineChart.getData().get(area)).getData().add(point, newData);
                }
            });
            hb.getChildren().addAll(lb1, tf1, lb2, tf2, lb3, tf3, bt);
            return hb;
        }

        public HBox getRemovePointFromDataDialog() {
            HBox hb = new HBox();

            Label lb1 = new Label("From area");
            final TextField tf1 = new TextField("0");
            tf1.setPrefWidth(50);
            tf1.setId(REMOVE_LINE_FROM_POS_TEXT_FIELD_ID);

            Label lb2 = new Label("From index");
            final TextField tf2 = new TextField("0");
            tf2.setPrefWidth(50);
            tf2.setId(REMOVE_INDEX_TEXT_FIELD_ID);

            Button bt = new Button("remove point!");
            bt.setId(REMOVE_BUTTON_ID);
            bt.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    int area = Integer.parseInt(tf1.getText());
                    int point = Integer.parseInt(tf2.getText());
                    ((XYChart.Series) testedLineChart.getData().get(area)).getData().remove(point);
                }
            });
            hb.getChildren().addAll(lb1, tf1, lb2, tf2, bt);
            return hb;
        }

        public HBox getRemoveDataDialog() {
            HBox hb = new HBox();
            Label lb = new Label("From index");
            final TextField tf = new TextField("0");
            tf.setPrefWidth(50);
            tf.setId(REMOVE_ITEM_POS_TEXT_FIELD_ID);
            Button bt = new Button("remove line!");
            bt.setId(REMOVE_BUTTON_ID);
            bt.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    int index = Integer.parseInt(tf.getText());
                    testedLineChart.getData().remove(index);
                }
            });
            hb.getChildren().addAll(lb, tf, bt);
            return hb;
        }

        public HBox getAddLineHBox() {
            HBox hb = new HBox();
            Label lb = new Label("Add series named ");
            final TextField tf = new TextField();
            tf.setPrefWidth(50);
            tf.setId(ADDED_SERIES_NAME_TEXTFIELD_ID);

            Label minLabel = new Label(" minX ");
            final TextField minText = new TextField();
            minText.setPrefWidth(50);
            minText.setId(ADDED_SERIES_MIN_VALUE_TEXTFIELD_ID);

            Label maxLabel = new Label(" maxX ");
            final TextField maxText = new TextField();
            maxText.setPrefWidth(50);
            maxText.setId(ADDED_SERIES_MAX_VALUE_TEXTFIELD_ID);

            Label amountLabel = new Label(" with ");
            final TextField amountText = new TextField();
            amountText.setPrefWidth(50);
            amountText.setId(ADDED_SERIES_DOTS_COUNT_TEXTFIELD_ID);

            Button bt = new Button(" points add!");
            bt.setId(ADD_SERIES_COMMAND_BUTTON_ID);
            bt.setOnAction(new EventHandler() {
                public void handle(Event t) {
                    String serieName = tf.getText();
                    double min = Double.parseDouble(minText.getText());
                    double max = Double.parseDouble(maxText.getText());
                    int amount = Integer.parseInt(amountText.getText());

                    ObservableList list = FXCollections.observableArrayList();

                    for (int i = 0; i < amount; i++) {
                        XYChart.Data newData = new XYChart.Data();
                        newData.setXValue(new Random().nextDouble() * (max - min) + min);
                        newData.setYValue(new Random().nextDouble() * (max - min) + min);
                        list.add(newData);
                    }
                    Object[] array = list.toArray();
                    Arrays.sort(array, new Comparator() {
                        public int compare(Object t, Object t1) {
                            return (int) Math.round(((Double) ((XYChart.Data) t).getXValue()) - ((Double) ((XYChart.Data) t1).getXValue()));
                        }
                    });

                    XYChart.Series serie = new XYChart.Series(serieName, FXCollections.observableArrayList(array));

                    testedLineChart.getData().add(serie);
                    pane.addPropertiesTable(serieName, getTableForProperty(serie, min, max).getVisualRepresentation());
                }
            });
            hb.getChildren().addAll(lb, tf, minLabel, minText, maxLabel, maxText, amountLabel, amountText, bt);
            return hb;
        }

        protected PropertiesTable getTableForProperty(XYChart.Series serie, double min, double max) {
            PropertiesTable table = new PropertiesTable(serie);

            table.addSimpleListener(serie.chartProperty(), serie);
            table.addSimpleListener(serie.nameProperty(), serie);
            table.addSimpleListener(serie.dataProperty(), serie);

            for (Iterator it = serie.getData().iterator(); it.hasNext();) {
                final XYChart.Data data = (XYChart.Data) it.next();

                final DoubleProperty intermediateX = new SimpleDoubleProperty(null, "XValue");
                final DoubleProperty intermediateY = new SimpleDoubleProperty(null, "YValue");

                data.XValueProperty().addListener(new ChangeListener() {
                    public void changed(ObservableValue ov, Object t, Object t1) {
                        if (!intermediateX.isBound()) {
                            intermediateX.setValue((Double) data.XValueProperty().getValue());
                        }
                    }
                });

                data.YValueProperty().addListener(new ChangeListener() {
                    public void changed(ObservableValue ov, Object t, Object t1) {
                        if (!intermediateY.isBound()) {
                            intermediateY.setValue((Double) data.YValueProperty().getValue());
                        }
                    }
                });

                intermediateX.addListener(new ChangeListener() {
                    public void changed(ObservableValue ov, Object t, Object t1) {
                        data.setXValue(t1);
                    }
                });

                intermediateY.addListener(new ChangeListener() {
                    public void changed(ObservableValue ov, Object t, Object t1) {
                        data.setYValue(t1);
                    }
                });

                table.addDoublePropertyLine(intermediateX, min, max, (Double) data.getXValue(), data);
                table.addDoublePropertyLine(intermediateY, min, max, (Double) data.getYValue(), data);
            }

            return table;
        }

        public LineChart getNewChart() {
            LineChart chart = new LineChart(axis1, axis2);
            chart.setTitle("LineChart");
            chart.setStyle("-fx-border-color: darkgray;");
            return chart;
        }
    }
}