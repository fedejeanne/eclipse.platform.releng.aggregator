/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anthony Dahanne  <anthony.dahanne@compuware.com> - enhance ETF to be able to launch several tests in several bundles - https://bugs.eclipse.org/330613
 *     Lucas Bullen (Red Hat Inc.) - JUnit 5 support
 *******************************************************************************/
package org.eclipse.test;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.junitlauncher.TestExecutionContext;
import org.eclipse.ui.testing.dumps.TimeoutDumpTimer;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A TestRunner for JUnit that supports Ant JUnitResultFormatters and running
 * tests inside Eclipse. Example call: EclipseTestRunner -classname
 * junit.samples.SimpleTest
 * formatter=org.apache.tools.ant.taskdefs.optional.junit
 * .XMLJUnitResultFormatter
 */
public class EclipseTestRunner {

	private static class RunArguments {
		String className = null;
		String classesNames = null;
		String testPluginName = null;
		String testPluginsNames = null;
		String resultCheckerName = null;
		String resultCheckersNames = "";
		String resultPathString = null;
		String timeoutString = null;
		String junitReportOutput = null;
		Properties props = new Properties();

		RunArguments(String[] args) throws IOException {

			int startArgs = 0;
			if (args.length > 0) {
				// support the JUnit task commandline syntax where
				// the first argument is the name of the test class
				if (!args[0].startsWith("-")) {
					className = args[0];
					startArgs++;
				}
			}
			for (int i = startArgs; i < args.length; i++) {
				if (args[i].toLowerCase().equals("-classname")) {
					if (i < args.length - 1) {
						className = args[i + 1];
					}
					i++;
				} else if (args[i].toLowerCase().equals("-classesnames")) {
					if (i < args.length - 1) {
						classesNames = args[i + 1];
					}
					i++;
				} else if (args[i].toLowerCase().equals("-testpluginname")) {
					if (i < args.length - 1) {
						testPluginName = args[i + 1];
					}
					i++;
				} else if (args[i].toLowerCase().equals("-testpluginsnames")) {
					if (i < args.length - 1) {
						testPluginsNames = args[i + 1];
					}
					i++;
				} else if (args[i].toLowerCase().equals("-resultcheckername")) {
					if (i < args.length - 1) {
						resultCheckerName = args[i + 1];
					}
					i++;
				} else if (args[i].toLowerCase().equals("-resultcheckersnames")) {
					if (i < args.length - 1) {
						resultCheckersNames = args[i + 1];
					}
					i++;
				} else if (args[i].equals("-junitReportOutput")) {
					if (i < args.length - 1) {
						junitReportOutput = args[i + 1];
					}
					i++;
				} else if (args[i].startsWith("formatter=")) {
					String formatterString = args[i].substring(10);
					int seperatorIndex = formatterString.indexOf(',');
					resultPathString = seperatorIndex == -1 ? null : formatterString.substring(seperatorIndex + 1);
				} else if (args[i].startsWith("propsfile=")) {
					try (FileInputStream in = new FileInputStream(args[i].substring(10))) {
						props.load(in);
					}
				} else if (args[i].equals("-timeout")) {
					if (i < args.length - 1) {
						timeoutString = args[i + 1];
					}
					i++;
				}
			}
			// Add/overlay system properties on the properties from the Ant project
			props.putAll(System.getProperties());

		}
	}

	/**
	 * No problems with this test.
	 */
	public static final int SUCCESS = 0;
	/**
	 * Some tests failed.
	 */
	public static final int FAILURES = 1;
	/**
	 * An error occured.
	 */
	public static final int ERRORS = 2;

	/**
	 * The main entry point (the parameters are not yet consistent with the Ant
	 * JUnitTestRunner, but eventually they should be). Parameters
	 *
	 * <pre>
	 * -className=&lt;testSuiteName&gt;
	 * -testPluginName&lt;containingpluginName&gt;
	 * -formatter=&lt;classname&gt;(,&lt;path&gt;)
	 * </pre>
	 *
	 * Where &lt;classname&gt; is the formatter classname, currently ignored as only
	 * LegacyXmlResultFormatter is used. The path is either the path to the result
	 * file and should include the file extension (xml) if a single test is being
	 * run or should be the path to the result directory where result files should
	 * be created if multiple tests are being run. If no path is given, the standard
	 * output is used.
	 * 
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		System.exit(run(args));
	}

	public static int run(String[] args) throws IOException {
		RunArguments runArgs = new RunArguments(args);

		if (runArgs.timeoutString == null || runArgs.timeoutString.isEmpty()) {
			System.err.println("INFO: optional timeout was not specified.");
		} else {
			String timeoutScreenOutputDir = null;
			if (runArgs.junitReportOutput == null || runArgs.junitReportOutput.isEmpty()) {
				timeoutScreenOutputDir = "timeoutScreens";
			} else {
				timeoutScreenOutputDir = runArgs.junitReportOutput + "/timeoutScreens";
			}
			System.err.println("INFO: timeoutScreenOutputDir: " + timeoutScreenOutputDir);
			System.err.println("INFO: timeout: " + runArgs.timeoutString);
			TimeoutDumpTimer.startTimeoutDumpTimer(runArgs.timeoutString, new File(timeoutScreenOutputDir));
		}

		if (runArgs.testPluginsNames != null && runArgs.classesNames != null) {
			// we have several plugins to look tests for, let's parse their
			// names
			String[] testPlugins = runArgs.testPluginsNames.split(",");
			String[] suiteClasses = runArgs.classesNames.split(",");
			String[] checkerClasses = runArgs.resultCheckersNames.split(",");
			int returnCode = 0;
			int j = 0;
			for (String oneClassName : suiteClasses) {
				String checkerClassName = (checkerClasses.length > j) ? checkerClasses[j] : null;

				int result = runTests(runArgs.props, testPlugins[j], oneClassName, checkerClassName,
						runArgs.resultPathString, true);
				j++;
				if (result != 0) {
					returnCode = result;
				}
			}
			return returnCode;
		}
		// className = "org.eclipse.jdt.junit.tests.MyJUnit4ParameterizedTest";
		// testPluginName = "org.eclipse.jdt.ui.tests";
		// resultPathString = "C:\\Users\\visjee\\Desktop\\results.xml";
		if (runArgs.className == null) {
			throw new IllegalArgumentException("Test class name not specified");
		}
		return runTests(runArgs.props, runArgs.testPluginName, runArgs.className, runArgs.resultCheckerName,
				runArgs.resultPathString, false);
	}



	private static int runTests(Properties props, String testPluginName, String testClassName, String checkerClassName,
			String resultPath,
			boolean multiTest) {
		Thread thisThread = Thread.currentThread();
		ClassLoader currentTCCL = thisThread.getContextClassLoader();
		Bundle testPlugin = ClassLoaderTools.getTestBundle(testPluginName, testClassName);

		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
				.selectors(selectClass(testClassName)).build();

		AtomicBoolean executionFailed = new AtomicBoolean(false);
		try (LegacyXmlResultFormatter formatter = new LegacyXmlResultFormatter();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				OutputStream out = getResultOutputStream(resultPath, testClassName, multiTest)) {

			ClassLoader jUnit5Classloader = ClassLoaderTools.getJUnit5Classloader(getPlatformEngines());
			thisThread.setContextClassLoader(jUnit5Classloader);
			Launcher launcher = LauncherFactory.create(); // DO NOT MOVE! Uses the just set context-classloader

			thisThread.setContextClassLoader(ClassLoaderTools.getPluginClassLoader(testPlugin, jUnit5Classloader));

			formatter.setDestination(baos);
			formatter.setContext(createExecutionContext(props));

			launcher.execute(request, formatter, createExecutionListener(executionFailed));

			// write to System.out or to file
			out.write(baos.toByteArray());

			checkResult(baos.toByteArray(), checkerClassName, thisThread.getContextClassLoader());

		} catch (IOException e) {
			e.printStackTrace();
			return ERRORS;
		} finally {
			thisThread.setContextClassLoader(currentTCCL);
		}
		return executionFailed.get() ? FAILURES : SUCCESS;
	}

	private static void checkResult(byte[] byteArray, String checkerClassName, ClassLoader classLoader) {
		if (checkerClassName == null || checkerClassName.isBlank()) {
			return;
		}

		try {
			@SuppressWarnings("unchecked") // it's covered, there's a catch (ClassCastException e) below
			Consumer<Document> checker = (Consumer<Document>) classLoader.loadClass(checkerClassName).getConstructor()
					.newInstance();

			Document xml = buildXML(byteArray);
			checker.accept(xml);
		} catch (ClassNotFoundException e) {
			System.err.println("SKIPPING CHECK - Can't load checker class: " + checkerClassName);
			e.printStackTrace();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Couldn't parse the XML output");
			e.printStackTrace();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.err.println("Couldn't instantiate the checker of type: " + checkerClassName
					+ ". Are you sure it implements/extends the correct interface/class?");
			e.printStackTrace();
		} catch (ClassCastException e) {
			System.err.println("Couldn't cast the checker of type: " + checkerClassName);
		}
	}

	private static Document buildXML(byte[] resultFile) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = null;
		try (InputStream is = new ByteArrayInputStream(resultFile)) {
			doc = builder.parse(is);
			doc.getDocumentElement().normalize();

		}
		return doc;
	}

	private static OutputStream getResultOutputStream(String resultPathString, String testClassName, boolean multiTest)
			throws IOException {
		if (resultPathString == null || resultPathString.isEmpty()) {
			return System.out;
		}
		Path resultFile = Path.of(resultPathString);
		if (multiTest) {
			resultFile = resultFile.resolve("TEST-" + testClassName + ".xml");
		} else {
			if (Files.isDirectory(resultFile)) {
				resultFile = resultFile.resolve("TEST-" + testClassName + ".xml");
			}
		}
		Files.createDirectories(resultFile.getParent());
		return Files.newOutputStream(resultFile);
	}

	private static List<Bundle> getPlatformEngines() {
		BundleContext context = FrameworkUtil.getBundle(EclipseTestRunner.class).getBundleContext();
		return Arrays.stream(context.getBundles()).filter(bundle -> {
			try {
				return bundle.getEntry("META-INF/services/org.junit.platform.engine.TestEngine") != null;
			} catch (Exception e) {
				return false; // assume absent
			}
		}).collect(Collectors.toList());
	}

	private static TestExecutionListener createExecutionListener(AtomicBoolean executionFailed) {
		return new TestExecutionListener() {
			@Override
			public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult executionResult) {
				if (executionResult.getStatus() == org.junit.platform.engine.TestExecutionResult.Status.FAILED) {
					executionFailed.set(true);
				}
			}
		};
	}

	private static TestExecutionContext createExecutionContext(Properties props) {
		return new TestExecutionContext() {
			@Override
			public Properties getProperties() {
				return props;
			}

			@Override
			public Optional<Project> getProject() {
				return Optional.empty();
			}
		};
	}
}
