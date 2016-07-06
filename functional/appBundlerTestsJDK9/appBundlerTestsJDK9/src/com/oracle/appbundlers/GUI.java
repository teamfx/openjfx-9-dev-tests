/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.oracle.appbundlers;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.oracle.appbundlers.utils.AppWrapper;
import com.oracle.appbundlers.utils.Constants;
import com.oracle.appbundlers.utils.Source;
import com.oracle.appbundlers.utils.Utils;
import com.oracle.tools.packager.Bundler;
import com.oracle.tools.packager.Bundlers;
import com.oracle.tools.packager.ConfigException;
import com.oracle.tools.packager.RelativeFileSet;
import com.oracle.tools.packager.UnsupportedPlatformException;
import com.sun.javafx.tools.packager.Log;
import com.sun.javafx.tools.packager.bundlers.BundleParams;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Dmitry Ginzburg &lt;dmitry.x.ginzburg@oracle.com&gt;
 */
public class GUI extends Application implements Constants {

    private final Logger LOG = Logger.getLogger(GUI.class.getName());

    @Override
    public void start(Stage primaryStage) {
        final CheckBox fxAppCheckBox = new CheckBox("JavaFX App");
        final CheckBox utilClassCheckBox = new CheckBox("Utility class used");
        final Button buildButton = new Button("Build application");
        final CheckBox verbose = new CheckBox("Verbose output/Debug");

        final VBox root = new VBox();
        root.getChildren().addAll(fxAppCheckBox, utilClassCheckBox, verbose,
                buildButton);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setSpacing(5);

        final Scene scene = new Scene(root);

        buildButton.setOnAction((ev) -> {
            try {
                if (verbose.isSelected()) {
                    Log.setLogger(new Log.Logger(true));
                    Log.setDebug(true);
                }
                String workDir = "test"
                        + (fxAppCheckBox.isSelected() ? "Fx" : "") + "App"
                        + (utilClassCheckBox.isSelected() ? "WithUtil" : "");
                String outputCommand = utilClassCheckBox.isSelected()
                        ? "testapp.util.Util.println" : SYSTEM_OUT_PRINTLN;
                @SuppressWarnings("serial")
                AppWrapper app = new AppWrapper(Utils.getTempSubDir(workDir),
                        COM_GREETINGS_APP1_QUALIFIED_CLASS_NAME,
                        new Source("custom.util", "util.module.template",
                                new HashMap<String, String>() {
                    {
                        put("Util.java.template", "testapp.util.Util");
                    }
                }, "custom.util.testapp.Util", CUSTOM_UTIL_CLASS_SIMPLE_NAME,
                                new HashMap<String, String>() {
                    {
                        put(PRINTLN_STATEMENT, outputCommand);
                        put(APP_NAME_REPLACEMENT_STATEMENT, "App1");
                        put(PASS_STRING_REPLACEMENT_STATEMENT, PASS_1);
                        put(PACKAGE_NAME_STATEMENT, "testapp.util");
                        put(CLASS_NAME_STATEMENT,
                                CUSTOM_UTIL_CLASS_SIMPLE_NAME);
                    }
                }),

                new Source(COM_GREETINGS_MODULE_CUM_PACKAGE_NAME,
                        COM_GREETINGS_MODULE_INFO_TEMPLATE,
                        new HashMap<String, String>() {
                    {
                        put(FXAPP_JAVA_TEMPLATE,
                                COM_GREETINGS_APP1_QUALIFIED_CLASS_NAME);
                    }
                }, COM_GREETINGS_APP1_QUALIFIED_CLASS_NAME,
                        COM_GREETINGS_JAR_NAME, new HashMap<String, String>() {
                    {
                        put(PRINTLN_STATEMENT, outputCommand);
                        put(APP_NAME_REPLACEMENT_STATEMENT, "App1");
                        put(PASS_STRING_REPLACEMENT_STATEMENT, PASS_1);
                        put(PACKAGE_NAME_STATEMENT,
                                COM_GREETINGS_MODULE_CUM_PACKAGE_NAME);
                    }
                }));
                System.out.println(
                        "Writing app to app directory: " + app.getWorkDir());
                app.preinstallApp();
                app.writeSourcesToAppDirectory();
                app.compileApp();
                try {
                    app.jarApp();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                System.out
                        .println("Building app with app the suitable Bundlers");
                Map<String, Object> params = new HashMap<>();
                params.put(BundleParams.PARAM_APP_RESOURCES,
                        new RelativeFileSet(app.getJarDir().toFile(),
                                app.getJarFilesList().stream().map(Path::toFile)
                                        .collect(toSet())));
                params.put(MAIN_JAR,
                        new RelativeFileSet(app.getJarDir()
                                .toFile(),
                        new HashSet<>(
                                Arrays.asList(app.getMainJarFile().toFile()))));
                params.put(CLASSPATH, app.getJarFilesList().stream()
                        .map(Path::getFileName).map(Path::toString)
                        .collect(Collectors.joining(File.pathSeparator)));
                // params.put(MODULEPATH, JMODS_PATH_IN_JDK);

                System.out.println(params);

                Collection<Bundler> bundlers2 = Bundlers
                        .createBundlersInstance().getBundlers("INSTALLER");
                System.out.println("value of bundler2 is " + bundlers2);

                bundlers2.stream().filter((bundler) -> {
                    try {
                        bundler.validate(params);
                        return true;
                    } catch (ConfigException ex) {
                        LOG.log(Level.SEVERE,
                                "Failed to create bundler \"{0}\": {1}.",
                                new Object[] { bundler.getID(), ex });
                        return false;
                    } catch (UnsupportedPlatformException ex) {
                        LOG.log(Level.INFO,
                                "Current platform is not supported by \"{0}\".",
                                bundler.getID());
                        return false;
                    }
                }).forEach((bundler) -> {
                    String paramStr = params.entrySet().stream()
                            .map((e) -> String.format("%s = %s", e.getKey(),
                                    e.getValue()))
                            .collect(Collectors.joining("\n"));
                    LOG.log(Level.INFO, "Executing bundler: {0}, params:\n{1}",
                            new Object[] { bundler.getID(), paramStr });
                    File result = bundler.execute(params,
                            app.getBundlesDir().toFile());
                    LOG.log(Level.INFO, "Finished with result: {0}", result);

                });
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to create application files: {0}",
                        ex.getMessage());
            } finally {
                Platform.exit();
            }
        });

        primaryStage.setTitle("AppBundlers Sample GUI");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}