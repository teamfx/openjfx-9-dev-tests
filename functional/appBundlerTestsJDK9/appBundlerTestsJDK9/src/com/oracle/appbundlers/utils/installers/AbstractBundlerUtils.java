/*
 * Copyright (c) 2014, 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
package com.oracle.appbundlers.utils.installers;

import static com.oracle.appbundlers.utils.Config.CONFIG_INSTANCE;
import static com.oracle.appbundlers.utils.Utils.waitUntil;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.testng.Assert;

import com.oracle.appbundlers.utils.AppWrapper;
import com.oracle.appbundlers.utils.BundlerUtils;
import com.oracle.appbundlers.utils.Constants;
import com.oracle.appbundlers.utils.ExtensionType;
import com.oracle.appbundlers.utils.ProcessOutput;
import com.oracle.appbundlers.utils.Utils;
import com.oracle.tools.packager.Bundler;
import com.oracle.tools.packager.Bundlers;
import com.oracle.tools.packager.StandardBundlerParam;

import javafx.util.Pair;

/**
 *
 * @author Dmitry Ginzburg &lt;dmitry.x.ginzburg@oracle.com&gt;
 * @author Dmitry Zinkevich &lt;dmitry.zinkevich@oracle.com&gt;
 */
public abstract class AbstractBundlerUtils implements Constants {

    private static final Logger LOG = Logger
            .getLogger(AbstractBundlerUtils.class.getName());
    public static final int ROOT_DIRECTORY_DEPTH = 1;

    public static enum BundleType {

        NONE("NONE"), INSTALLER("INSTALLER"), IMAGE("IMAGE"), ALL("ALL");

        private final String bundlerType;

        private BundleType(String bundlerType) {
            this.bundlerType = bundlerType;
        }

        public String getBundleType() {
            return bundlerType;
        }
    }

    public static final String OUTPUT_CONTAINS = "outputContains";
    public static final String MULTI_OUTPUT_CONTAINS = "multiOutputContains";
    public static final String OVERRIDE_DEFAULT_ARGS = "overrideDefArgs";
    public static final String SECOND_LAUNCHER_OUTPUT_CONTAINS = "secondLauncherOutputContains";
    public static final String SECOND_LAUNCHER_MULTI_OUTPUT_CONTAINS = "secondLauncherMultiOutputContains";
    public static final String ASSOCIATED_EXTENSIONS = "associatedExtensions";
    public static final String SAME_JAVA_EXECUTABLE = "SAME_JAVA_EXECUTABLE";
    public static final String USER_FRIENDLY_API = "USER_FRIENDLY_API";
    public static final String WIN_SYSTEM_WIDE_FILE_ASSOCIATIONS = "SYSTEM_WIDE_FILE_ASSOCIATIONS";
    public static final String WIN_USER_FILE_ASSOCIATIONS = "USER_FILE_ASSOCIATIONS";
    public static final String APPCDS_CACHE_FILE_EXISTS_RUN = "APPCDS_CACHE_FILE_EXISTS_RUN";
    public static final String APPCDS_CACHE_FILE_EXISTS_INSTALL = "APPCDS_CACHE_FILE_EXISTS_INSTALL";
    public static final String CHECK_MODULE_IN_JAVA_EXECUTABLE = "CHECK_MODULE_IN_JAVA_EXECUTABLE";
    public static final String CHECK_EXECUTABLES_AVAILABLE_IN_BIN = "EXECUTABLES_AVAILABLE";
    public static final String SAME_JAVA_EXECUTABLE_OUTPUT = "SAME_JAVA_EXECUTABLE_OUTPUT";

    private final Bundler bundler;

    protected final Map<String, VerificationMethod> verificators = new HashMap<>();

    protected void checkOutputContains(ProcessOutput process,
            List<String> expectedWords) {
        checkOutputContains(process, expectedWords, Collections.emptyList());
    }

    protected void checkOutputContains(ProcessOutput process,
            List<String> expectedWords, List<String> forbiddenWords) {
        List<String> output = new ArrayList<>();
        output.addAll(process.getErrorStream());
        output.addAll(process.getOutputStream());
        boolean containsAll = true;
        for (String word : expectedWords) {
            boolean containsWord = output.stream()
                    .anyMatch(str -> str.contains(word));
            LOG.log(Level.INFO, "Checking output contains {0}: {1}.",
                    new Object[] { word, containsWord ? "OK" : "FAIL" });
            containsAll &= containsWord;
        }
        for (String word : forbiddenWords) {
            boolean notContains = !output.stream()
                    .anyMatch(str -> str.contains(word));
            LOG.log(Level.INFO, "Checking output NOT contains {0}: {1}.",
                    new Object[] { word, notContains ? "OK" : "FAIL" });
            containsAll &= notContains;
        }
        assertTrue(containsAll, "Output must contain all "
                + "expected words and not contain forbidden words.");
    }

    public static final String CHECK_ARGUMENTS = "checkArguments";

    {
        verificators.put(OUTPUT_CONTAINS, (value, app, applicationTitle) -> {
            try {
                ProcessOutput process = runInstalledExecutable(app,
                        applicationTitle);
                checkOutputContains(process, Arrays.asList(value.toString()));
            } catch (IOException | ExecutionException e) {
                Assert.fail(e.getMessage(), e);
            }
        });

        verificators.put(SECOND_LAUNCHER_OUTPUT_CONTAINS,
                (value, app, applicationTitle) -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Pair<String, String> params = (Pair<String, String>) value;
                        final String execName = params.getKey();
                        final String expectedOutput = params.getValue();

                        ProcessOutput process = Utils.runCommand(
                                new String[] {
                                        getInstalledExecutableLocation(app,
                                                applicationTitle, execName)
                                                        .toString() },
                                /* verbose = */ true,
                                CONFIG_INSTANCE.getRunTimeout());
                        checkOutputContains(process,
                                Arrays.asList(expectedOutput));
                    } catch (IOException | ExecutionException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });

        verificators.put(MULTI_OUTPUT_CONTAINS,
                (value, app, applicationTitle) -> {
                    try {
                        ProcessOutput process = runInstalledExecutable(app,
                                applicationTitle);
                        checkOutputContains(process, (List<String>) value);
                    } catch (IOException | ExecutionException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });

        verificators.put(OVERRIDE_DEFAULT_ARGS,
                (value, app, applicationTitle) -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Pair<List<String>, List<String>> params = (Pair<List<String>, List<String>>) value;

                        final List<String> args = params.getKey();
                        final List<String> defaultArgs = params.getValue();

                        ProcessOutput process = runInstalledExecutableWithArgs(
                                app, applicationTitle,
                                args.toArray(new String[0]));
                        checkOutputContains(process, args, defaultArgs);

                    } catch (IOException | ExecutionException e) {
                        Assert.fail(e.getMessage(), e);
                    }
                });

        verificators.put(SECOND_LAUNCHER_MULTI_OUTPUT_CONTAINS,
                (value, app, applicationTitle) -> {
                    if (value instanceof List) {
                        Iterator<Pair<String, List<String>>> iterator = ((List<Pair<String, List<String>>>) value)
                                .iterator();
                        while (iterator.hasNext()) {
                            Pair<String, List<String>> args = iterator.next();
                            final String execName = args.getKey();
                            final List<String> expectedOutput = args.getValue();

                            try {
                                ProcessOutput process = Utils.runCommand(
                                        new String[] {
                                                getInstalledExecutableLocation(
                                                        app, applicationTitle,
                                                        execName).toString() },
                                        true, CONFIG_INSTANCE.getRunTimeout());
                                checkOutputContains(process, expectedOutput);
                            } catch (IOException | ExecutionException e) {
                                Assert.fail(e.getMessage(), e);
                            }
                        }
                    }
                });

        verificators.put(CHECK_ARGUMENTS, (value, app, applicationTitle) -> {
            try {
                @SuppressWarnings("unchecked")
                List<String> args = (List<String>) value;
                ProcessOutput process = runInstalledExecutableWithArgs(app,
                        applicationTitle, args.toArray(new String[0]));
                checkOutputContains(process, args);
            } catch (IOException | ExecutionException e) {
                Assert.fail(e.getMessage(), e);
            }
        });

        verificators.put(SAME_JAVA_EXECUTABLE,
                (value, app, applicationTitle) -> {
                    @SuppressWarnings("unchecked")

                    Map<String, Map<String, String>> params = (Map<String, Map<String, String>>) value;
                    Map<String, String> appNamesToOutput = params
                            .get(SAME_JAVA_EXECUTABLE_OUTPUT);
                    Set<String> appNames = appNamesToOutput.keySet();
                    Set<String> secondAppNameSet = appNames.stream()
                            .filter((String appName) -> !appName
                                    .equals(applicationTitle))
                            .collect(Collectors.toSet());
                    String secondAppName = secondAppNameSet.iterator().next();
                    LOG.log(Level.INFO, "first app name is {0}",
                            applicationTitle);
                    LOG.log(Level.INFO, "second app name is {0}",
                            secondAppName);
                    try {
                        ProcessOutput firstAppProcessOutput = runInstalledExecutable(
                                app, applicationTitle);
                        final String openedText = "[info][class,load] opened:";

                        List<String> outputStream = firstAppProcessOutput
                                .getOutputStream();

                        checkOutputContains(firstAppProcessOutput,
                                Arrays.asList(appNamesToOutput
                                        .get(applicationTitle)));
                        Optional<String> maybePath = outputStream.stream()
                                .filter(s -> s.contains(openedText))
                                .findFirst();
                        String[] firstAppVerboseOutput = maybePath
                                .orElseThrow(Exception::new).split("opened:");

                        Path root = getInstalledAppRootLocation(app,
                                applicationTitle).toAbsolutePath();

                        String firstAppJdk9ModulesPath = firstAppVerboseOutput[1]
                                .trim();
                        assertTrue(
                                firstAppJdk9ModulesPath
                                        .startsWith(root.toString()),
                                String.format("[%s does not start with %s]",
                                        firstAppJdk9ModulesPath,
                                        firstAppJdk9ModulesPath));

                        ProcessOutput secondAppProcessOutput = Utils.runCommand(
                                new String[] {
                                        getInstalledExecutableLocation(app,
                                                applicationTitle, secondAppName)
                                                        .toString() },
                                true, CONFIG_INSTANCE.getRunTimeout());

                        checkOutputContains(secondAppProcessOutput, Arrays
                                .asList(appNamesToOutput.get(secondAppName)));

                        List<String> outputStream2 = secondAppProcessOutput
                                .getOutputStream();
                        Optional<String> secondMaybePath = outputStream2
                                .stream().filter(s -> s.contains(openedText))
                                .findFirst();
                        String[] secondAppVerboseOutput = secondMaybePath
                                .orElseThrow(Exception::new).split("opened:");
                        LOG.log(Level.INFO,
                                "Root Location of Installed Application is {0}",
                                root.toString());
                        LOG.log(Level.INFO, "FirstAppVerboseOutput is {0}",
                                Arrays.toString(firstAppVerboseOutput));
                        LOG.log(Level.INFO, "Second App Verbose Output is {0}",
                                Arrays.toString(secondAppVerboseOutput));
                        String secondAppJdk9ModulesPath = secondAppVerboseOutput[1]
                                .trim();

                        assertEquals(firstAppJdk9ModulesPath,
                                secondAppJdk9ModulesPath,
                                "[Second app uses different java location]");

                    } catch (Exception e) {
                        fail(e.getMessage(), e);
                    }
                });

        verificators.put(ASSOCIATED_EXTENSIONS,
                (value, app, applicationTitle) -> {
                    @SuppressWarnings("unchecked")
                    List<String> extensions = (List<String>) value;
                    try {
                        // in MacOS we need to register an association by
                        // running an application
                        registerFileAssociations(app, applicationTitle);
                        for (String ext : extensions) {
                            Path tmp = Files.createTempFile("test", "." + ext);
                            assertTrue(Files.exists(tmp),
                                    "Something gone wrong, file should have been created by Files::createTempFile");
                            // here we expect the system default application to
                            // be set to our deleter application (RmApp)
                            // so, after running this method, the file should be
                            // deleted
                            openFileWithAssociatedApplication(tmp);
                            // wait for the file to be deleted
                            try {
                                waitUntil(() -> !Files.exists(tmp),
                                        CONFIG_INSTANCE.getRunTimeout());
                            } catch (TimeoutException e) {
                                fail("File was not deleted by 'rm' application");
                            }
                        }
                    } catch (IOException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                });

        verificators.put(USER_FRIENDLY_API, (value, app, applicationTitle) -> {

            List[] optionLists = (List[]) value;

            List<String> defaultOptions = optionLists[0];
            List<String> userOpts = optionLists[1];

            // I. Clear user JVM options.
            try {
                ProcessOutput process = runInstalledExecutableWithArgs(app,
                        applicationTitle,
                        new String[] { "USER_JVM_OPTIONS_CLEAR_ALL" });
            } catch (IOException | ExecutionException e) {
                Assert.fail(e.getMessage(), e);
            }

            // II. Run the app second time and check default options are used
            verificators.get(MULTI_OUTPUT_CONTAINS).verify(defaultOptions, app,
                    applicationTitle);

            // Set provided user JVM options
            try {
                ProcessOutput process = runInstalledExecutableWithArgs(app,
                        applicationTitle,
                        new String[] { "USER_JVM_OPTIONS_SET_PREDEFINED" });
            } catch (IOException | ExecutionException e) {
                Assert.fail(e.getMessage(), e);
            }

            // III. Run app and check that specific options were applied
            verificators.get(MULTI_OUTPUT_CONTAINS).verify(userOpts, app,
                    applicationTitle);

            // Clear options again
            try {
                ProcessOutput process = runInstalledExecutableWithArgs(app,
                        applicationTitle,
                        new String[] { "USER_JVM_OPTIONS_CLEAR_ALL" });
            } catch (IOException | ExecutionException e) {
                Assert.fail(e.getMessage(), e);
            }

            // IV. Run the app and check default options are still used
            verificators.get(MULTI_OUTPUT_CONTAINS).verify(defaultOptions, app,
                    applicationTitle);
        });

        verificators.put(APPCDS_CACHE_FILE_EXISTS_RUN,
                (value, app, applicationTitle) -> {
                    try {
                        ProcessOutput process = runInstalledExecutable(app,
                                applicationTitle);
                        process.shutdown();
                        checkFile((Path) value);
                    } catch (IOException | ExecutionException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                });

        verificators.put(APPCDS_CACHE_FILE_EXISTS_INSTALL,
                (value, app, applicationTitle) -> {
                    try {
                        checkFile((Path) value);
                        ProcessOutput process = runInstalledExecutable(app,
                                applicationTitle);
                        process.shutdown();
                        checkAndRemoveFile((Path) value);
                    } catch (IOException | ExecutionException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                });

        verificators.put(CHECK_MODULE_IN_JAVA_EXECUTABLE,
                (value, app, appName) -> {
                    List<String> stringList = new ArrayList<String>();
                    Path binPath = getJavaExecutableBinPathInInstalledApp(app, appName);
                    String joinedString = String.join(File.separator,
                            binPath.toString(),  getJavaExecutable());
                    stringList.add(joinedString);
                    stringList.add(DOUBLE_HYPHEN + LIST_MODULES);

                    try {
                        ProcessOutput processOutput = Utils
                                .runCommand(stringList, CONFIG_INSTANCE.getRunTimeout());
                        if (value instanceof List) {
                            List<String> list = (List) value;
                            checkOutputContains(processOutput, list);
                        } else {
                            checkOutputContains(processOutput,
                                    Arrays.asList(value.toString()));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        verificators.put(CHECK_EXECUTABLES_AVAILABLE_IN_BIN,
                (value, app, appName) -> {
                    Path binPath = getJavaExecutableBinPathInInstalledApp(app, appName);
                    try {
                        checkIfExecutablesAvailableInBinDir(binPath);
                    } catch (Exception e) {
                       throw new RuntimeException(e);
                    }
                });
    }

    public AbstractBundlerUtils(BundleType type, BundlerUtils id) {
        Bundlers bundlersInstance = Bundlers.createBundlersInstance();
        List<Bundler> bundlers = bundlersInstance
                .getBundlers(type.getBundleType()).stream()
                .filter(b -> id.getBundlerId().equalsIgnoreCase(b.getID()))
                .collect(toList());
        if (bundlers.size() != 1) {
            throw new IllegalArgumentException(
                    "Number of bundlers should be equals to one : " + bundlers);
        }
        bundler = bundlers.get(0);
    }

    public abstract void checkIfExecutablesAvailableInBinDir(Path binPath) throws FileNotFoundException, IOException;

    public Bundler getBundler() {
        return bundler;
    }

    public void verifyOption(String name, Object value, AppWrapper app,
            String applicationTitle) {
        // TODO: verify improvement
        String debugString = String.format("name=%s; value=%s; bundler=%s",
                name, value, bundler.getID());
        if (verificators.containsKey(name)) {
            try {
                verificators.get(name).verify(value, app, applicationTitle);
                LOG.info("Verification SUCCEEDED." + debugString);
            } catch (Throwable ex) {
                LOG.log(Level.WARNING, "Verification FAILED: {0}.",
                        ex.getMessage());
                throw ex;
            }
        } else {
            LOG.info("Verification SKIPPED." + debugString);
        }
    }

    // to be reloaded in MacOS
    public void registerFileAssociations(AppWrapper app,
            String applicationTitle) throws IOException, ExecutionException {
    }

    public void checkAndRemoveFile(Path file)
            throws IOException, ExecutionException {
        checkFile(file);
        Utils.runCommand(getRmCommand(file), true,
                CONFIG_INSTANCE.getRunTimeout());
    }

    public void checkFile(Path file) throws IOException, ExecutionException {
        assertTrue(Files.exists(file), "File " + file + " was created");
    }

    public abstract void openFileWithAssociatedApplication(Path path)
            throws IOException, ExecutionException;

    public abstract Path getInstalledAppRootLocation(AppWrapper app,
            String applicationTitle);

    public ProcessOutput runInstalledExecutable(AppWrapper app,
            String applicationTitle) throws IOException, ExecutionException {
        return Utils
                .runCommand(
                        new String[] { getInstalledExecutableLocation(app,
                                applicationTitle).toString() },
                        true, CONFIG_INSTANCE.getRunTimeout());
    }

    public ProcessOutput runInstalledExecutableWithArgs(AppWrapper app,
            String applicationTitle, String[] args)
                    throws IOException, ExecutionException {

        String[] cmd = new String[1 + args.length];
        cmd[0] = getInstalledExecutableLocation(app, applicationTitle)
                .toString();
        System.arraycopy(args, 0, cmd, 1, args.length);
        return Utils.runCommand(cmd, true, CONFIG_INSTANCE.getRunTimeout());
    }

    public abstract Path getInstalledExecutableLocation(AppWrapper app,
            String applicationTitle, String execName);

    public Path getInstalledExecutableLocation(AppWrapper app,
            String applicationTitle) {
        return getInstalledExecutableLocation(app, applicationTitle,
                applicationTitle);
    }

    /**
     * install application
     *
     * @param app
     *            application to install
     * @param applicationTitle
     *            application title
     * @return executable path
     * @throws java.io.IOException
     */
    public abstract String install(AppWrapper app, String applicationTitle)
            throws IOException;

    /**
     * Runs manual installer of the application
     *
     * @param app
     *            application to install
     * @throws java.io.IOException
     */
    public abstract void manualInstall(AppWrapper app) throws IOException;

    /**
     * uninstall application
     *
     * @param app
     *            application to uninstall
     * @param applicationTitle
     *            application title
     * @throws java.io.IOException
     */
    public abstract void uninstall(AppWrapper app, String applicationTitle)
            throws IOException;

    @FunctionalInterface
    public interface VerificationMethod {

        public void verify(Object value, AppWrapper app,
                String applicationTitle);
    }

    protected Path findByExtension(Path dir, String extension, int maxDepth)
            throws IOException {
        Optional<Path> result = Files.find(dir, maxDepth, (file, attr) -> file
                .toFile().getName().endsWith("." + extension)).findFirst();
        if (!result.isPresent()) {
            throw new FileNotFoundException("*." + extension
                    + " not found under " + dir + " with maxDepth=" + maxDepth);
        }
        return result.get();
    }

    protected Path findByExtension(Path dir, String extension, int maxDepth,
            String applicationTitle) throws IOException {
        Optional<Path> result = Files
                .find(dir, maxDepth,
                        (file, attr) -> file.toFile().getName().startsWith(
                                applicationTitle)
                        && file.toFile().getName().endsWith("." + extension))
                .findFirst();

        if (!result.isPresent()) {
            result = Files.find(dir, maxDepth, (file, attr) -> file.toFile()
                    .getName().endsWith("." + extension)).findFirst();
        }

        if (!result.isPresent()) {
            throw new FileNotFoundException("*." + extension
                    + " not found under " + dir + " with maxDepth=" + maxDepth);
        }
        return result.get();
    }

    protected List<String> getLinesFromOptions(Map<String, String> opts) {
        return opts.keySet().stream().map(key -> key + opts.get(key))
                .collect(toList());
    }

    public String getAppName(Map<String, Object> params) {
        return StandardBundlerParam.APP_NAME.fetchFrom(params);
    }

    abstract public Path getAppCDSCacheFile(AppWrapper app, String appName, ExtensionType extensionType);

    public String[] getRmCommand(Path file) {
        return new String[] { "sudo", "/bin/rm", file.toString() };
    };

    public abstract Path getJavaExecutableBinPathInInstalledApp(AppWrapper appWrapper, String appTitle);

    public abstract String getJavaExecutable();

}
