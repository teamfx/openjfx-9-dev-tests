/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.oracle.appbundlers.tests.functionality.jdk9test;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.oracle.appbundlers.tests.BundlerProvider;
import com.oracle.appbundlers.tests.functionality.functionalinterface.VerifiedOptions;
import com.oracle.appbundlers.utils.AppWrapper;
import com.oracle.appbundlers.utils.BundlerUtils;
import com.oracle.appbundlers.utils.BundlingManager;
import com.oracle.appbundlers.utils.BundlingManagers;
import com.oracle.appbundlers.utils.Config;
import com.oracle.appbundlers.utils.ExtensionType;
import com.oracle.appbundlers.utils.PackageTypeFilter;
import com.oracle.appbundlers.utils.PackagerApiFilter;
import com.oracle.appbundlers.utils.SourceFactory;
import com.oracle.appbundlers.utils.Utils;
import com.oracle.appbundlers.utils.installers.AbstractBundlerUtils;
import com.oracle.tools.packager.ConfigException;
import com.oracle.tools.packager.RelativeFileSet;
import com.sun.javafx.tools.packager.bundlers.BundleParams;

import javafx.util.Pair;

/**
 * Aim: To test dependency on different module packages.
 * There are three module packages
 * 1. Modular Jar
 * 2. Jmods
 * 3. Exploded Modules
 * here is the different permutations and combination for test case
 *
 * SERVICE_INTERFACE is module which exposes API for service provider.
 * CIRCLE and RECTANGLE are modules which provides service for SERVICE_INTERFACE
 * Each row is a module path directory where each column says what type of module(modularjar, jmods, explodedmods) is containing in directory.
 *
 * ==============================================================
 *  ModularJar           | Jmods            | ExplodedMods
 * ==============================================================
 * | SERVICE_INTERFACE   |CIRCLE            | RECTANGLE         |
 * -------------------------------------------------------------|
 * | CIRCLE              |SERVICE_INTERFACE | RECTANGLE         |
 * -------------------------------------------------------------|
 * | RECTANGLE           |CIRCLE            | SERVICE_INTERFACE |
 * -------------------------------------------------------------|
 * | RECTANGLE           |SERVICE_INTERFACE | CIRCLE            |
 * -------------------------------------------------------------|
 * | CIRCLE              |RECTANGLE         | SERVICE_INTERFACE |
 * -------------------------------------------------------------|
 * | SERVICE_INTERFACE   |RECTANGLE         | CIRCLE            |
 * -------------------------------------------------------------|
 * @author Ramesh BG
 */
public class JmodExplodedModuleAndModularJarDependencyTest extends ModuleTestBase {

    private static String SI = COM_SHAPE_SERVICEINTERFACE_MODULE_NAME;
    private static String CIRCLE = COM_SHAPE_SERVICEPROVIDER_CIRCLE_MODULENAME;
    private static String RECTANGLE = COM_SHAPE_SERVICEPROVIDER_RECTANGLE_MODULE_NAME;

    private static String[][] array = new String[][] {
            { SI, CIRCLE, RECTANGLE },
            { CIRCLE, SI, RECTANGLE },
            { RECTANGLE, CIRCLE, SI },
            { RECTANGLE, SI, CIRCLE },
            { CIRCLE, RECTANGLE, SI },
            { SI, RECTANGLE, CIRCLE }
    };

    private List<Path> modulePathList = new ArrayList<Path>();

    public AppWrapper getApp() throws IOException {
        Map<String, String> classNameToTemplate = new HashMap<String, String>();
        classNameToTemplate.put(COM_SHAPE_SERVICEINTERFACE_SHAPEMAINCLASS,
                COM_SHAPE_SERVICEINTERFACE_SHAPEMAINCLASS_TEMPLATE);

        return new AppWrapper(Utils.getTempSubDir(WORK_DIRECTORY),
                COM_SHAPE_SERVICEINTERFACE_SHAPEMAINCLASS,
                SourceFactory.get_com_shape_serviceinterface_module(classNameToTemplate,
                        Collections.emptyMap(),
                        COM_SHAPE_SERVICEINTERFACE_SHAPEMAINCLASS),
                SourceFactory.get_com_shape_serviceprovider_circle_module(),
                SourceFactory.get_com_shape_serviceprovider_rectangle_module());
    }

    @Override
    public void overrideParameters(ExtensionType extension) throws IOException {
        this.currentParameter.setApp(getApp());
        this.currentParameter.setVerifiedOptions(getVerifiedOptions());
    }

    @Override
    protected void prepareTestEnvironment() throws Exception {
        for (ExtensionType javaExtensionFormat : ExtensionType.values()) {
            this.currentParameter = this.intermediateToParametersMap
                    .get(javaExtensionFormat);
            overrideParameters(javaExtensionFormat);
            initializeAndPrepareApp();
        }

        /*
         * create directories mixmods1 ... mixmods6
         * copy different ExtensionType format into mixmods* directory
         * finally each of mixmods* will be modulepath for test to run.
         *
         */
        for (int i = 0; i < array.length; i++) {
            Path mixmodsPath = Utils.getTempSubDir(WORK_DIRECTORY)
                    .resolve("mixmods" + i);
            modulePathList.add(mixmodsPath);
            Path mixmodsdir = Files.createDirectories(mixmodsPath);
            Path modularJarPath = intermediateToParametersMap
                    .get(ExtensionType.ModularJar).getApp().getModularJarsDir()
                    .resolve(array[i][0] + ".jar");
            Path jmodsPath = intermediateToParametersMap
                    .get(ExtensionType.Jmods).getApp().getJmodsDir()
                    .resolve(array[i][1] + ".jmod");
            Path explodedModsPath = intermediateToParametersMap
                    .get(ExtensionType.ExplodedModules).getApp()
                    .getExplodedModsDir().resolve(array[i][2]);
            Files.copy(modularJarPath, mixmodsdir.resolve(modularJarPath.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(jmodsPath, mixmodsdir.resolve(jmodsPath.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
            Path explodedDir = mixmodsdir
                    .resolve(explodedModsPath.getFileName());

            Files.walkFileTree(explodedModsPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        final BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(explodedDir
                            .resolve(explodedModsPath.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file,
                        final BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, explodedDir
                            .resolve(explodedModsPath.relativize(file)));
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    protected Map<String, Object> getAllParams(Path modulepath)
            throws Exception {
        Map<String, Object> allParams = new HashMap<String, Object>();
        allParams.put(APP_NAME,
                getResultingAppName());
        AppWrapper app = this.intermediateToParametersMap
                .get(ExtensionType.ModularJar).getApp();
        allParams.put(MAIN_MODULE,
                String.join("/", app.getMainModuleName(), app.getMainClass()));
        allParams.put(MODULEPATH, modulepath.toString());
        allParams.put(BundleParams.PARAM_APP_RESOURCES,
                new RelativeFileSet(modulepath.toFile(),
                        getModuleList(modulepath).stream().map(Path::toFile)
                                .collect(toSet())));
        allParams.put(STRIP_NATIVE_COMMANDS, false);
        allParams.put(ADD_MODS,
                app.getAllModuleNamesSeperatedByCommaExceptMainmodule());
        return allParams;
    }

    public VerifiedOptions getVerifiedOptions() {
        return () -> {
            Map<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put(AbstractBundlerUtils.MULTI_OUTPUT_CONTAINS,
                    Arrays.asList(CIRCLE_OUTPUT, RECTANGLE_OUTPUT));
            return hashMap;
        };
    }

    @Test(dataProvider = "getBundlers")
    public void runTest(BundlingManager bundlingManager) throws Exception {
        this.bundlingManager = bundlingManager;
        Path modulePath = this.bundlingManager.getModulePath();
        Map<String, Object> allParams = getAllParams(modulePath);
        String testName = this.getClass().getName() + "::"
                + testMethod.getName() + "$" + bundlingManager.toString() + "::"
                + bundlingManager.getModulePath().getFileName().toString();

        LOG.log(Level.INFO, "Starting test \"{0}\".", testName);

        try {
            validate();
            if (isConfigExceptionExpected(bundlingManager.getBundler())) {
                Assert.fail("ConfigException is expected, but isn't thrown");
            }
        } catch (ConfigException ex) {
            if (isConfigExceptionExpected(bundlingManager.getBundler())) {
                return;
            } else {
                LOG.log(Level.SEVERE, "Configuration error: {0}.",
                        new Object[] { ex });
                throw ex;
            }
        }

        try {
            AppWrapper app = intermediateToParametersMap
                    .get(ExtensionType.NormalJar).getApp();
            bundlingManager.execute(allParams, app);
            String path = bundlingManager.install(app,
                    getResultingAppName(),
                    false);
            LOG.log(Level.INFO, "Installed at: {0}", path);

            Pair<TimeUnit, Integer> tuple = getDelayAfterInstall();
            tuple.getKey().sleep(tuple.getValue());
            this.intermediateToParametersMap.get(ExtensionType.NormalJar)
                    .createNewVerifiedOptions()
                    .forEach((name, value) -> bundlingManager.verifyOption(name,
                            value, app, getResultingAppName(
                                   )));
        } finally {
            uninstallApp(modulePath);
            LOG.log(Level.INFO, "Finished test: {0}", testName);
        }
    }

    private void uninstallApp(Path modulepath) throws Exception {
        if (this.bundlingManager != null) {
            String appName = this.bundlingManager.getAppName(getAllParams(modulepath));
            this.bundlingManager.uninstall(this.intermediateToParametersMap
                    .get(ExtensionType.NormalJar).getApp(), appName);
        }
    }

    public List<Path> getModuleList(Path dir) throws IOException {
        try (DirectoryStream<Path> jarsStream = Files
                .newDirectoryStream(dir, "*")) {
            List<Path> modFiles = new ArrayList<>();
            jarsStream.forEach(modFiles::add);
            return modFiles;
        }
    }

    @DataProvider(name = "getBundlers")
    public Iterator<Object[]> getBundlers() {
        List<BundlingManagers> packagerInterfaces = Stream
                .of(getBundlingManagers()).filter(PackagerApiFilter::accept)
                .collect(Collectors.toList());

        List<AbstractBundlerUtils> installationPackageTypes = Stream
                .of(getBundlerUtils()).filter(BundlerUtils::isSupported)
                .filter(PackageTypeFilter::accept)
                .map(BundlerUtils::getBundlerUtils).collect(toList());

        return BundlerProvider.createBundlingManagers(installationPackageTypes,
                packagerInterfaces, modulePathList, true);
    }

    @AfterClass
    protected void cleanUp() throws IOException {
        super.cleanUp();

        for (Iterator<Path> iterator = modulePathList.iterator(); iterator
                .hasNext();) {
            Path path = iterator.next();
            if (!Config.CONFIG_INSTANCE.isNoCleanSet()) {
                LOG.log(Level.INFO, "Removing directory " + path);
                Utils.tryRemoveRecursive(path);
            } else {
                LOG.log(Level.INFO, "Skipped removing directory " + path);
            }
        }
        Utils.tryRemoveRecursive(this.intermediateToParametersMap
                .get(ExtensionType.NormalJar).getApp().getWorkDir());
    }
}

