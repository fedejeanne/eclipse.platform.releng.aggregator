package org.eclipse.test.tests.snippets.resources;

import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.AbortOnSetUpClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.AbortOnTearDownClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.ErrorOnSetUpClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.ErrorOnTearDownClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.FailOnSetUpClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.FailOnTearDownClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.IgnoreOnSetUpClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.IgnoreOnTearDownClass;
import org.eclipse.test.tests.snippets.resources.MyJUnit4BrokenTestClassesTestSuite.IgnoreWholeClass;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All of the test classes in this suite are broken, either they <em>fail</em>
 * or they
 */
@RunWith(Suite.class)
@SuiteClasses({ //
		FailOnSetUpClass.class, //
		FailOnTearDownClass.class, //
		ErrorOnSetUpClass.class, //
		ErrorOnTearDownClass.class, //
		AbortOnSetUpClass.class, //
		AbortOnTearDownClass.class,//
		IgnoreOnSetUpClass.class, //
		IgnoreOnTearDownClass.class,//
		IgnoreWholeClass.class, //
})
public class MyJUnit4BrokenTestClassesTestSuite {

	@RunWith(JUnit4.class)
	public static class FailOnSetUpClass {

		@BeforeClass
		public static void setupClass() {
			fail();
		}

		@Test
		public void test() {
		}
	}

	@RunWith(JUnit4.class)
	public static class FailOnTearDownClass {

		@AfterClass
		public static void tearDownClass() {
			fail();
		}

		@Test
		public void test() {
		}
	}

	@RunWith(JUnit4.class)
	public static class ErrorOnSetUpClass {

		@BeforeClass
		public static void setupClass() {
			throw new RuntimeException();
		}

		@Test
		public void test() {
		}
	}

	@RunWith(JUnit4.class)
	public static class ErrorOnTearDownClass {

		@AfterClass
		public static void tearDownClass() {
			throw new RuntimeException();
		}

		@Test
		public void test() {
		}
	}

	@RunWith(JUnit4.class)
	public static class AbortOnSetUpClass {

		@BeforeClass
		public static void setupClass() {
			Assume.assumeFalse(true);
		}

		@Test
		public void test() {
		}
	}

	@RunWith(JUnit4.class)
	public static class AbortOnTearDownClass {

		@AfterClass
		public static void tearDownClass() {
			Assume.assumeFalse(true);
		}

		@Test
		public void test() {
		}
	}

	@RunWith(JUnit4.class)
	public static class IgnoreOnSetUpClass {

		@BeforeClass
		@Ignore
		public static void setupClass() {
		}

		@Test
		public void test() {
		}
	}

	@RunWith(JUnit4.class)
	public static class IgnoreOnTearDownClass {

		@AfterClass
		@Ignore
		public static void tearDownClass() {
		}

		@Test
		public void test() {
		}
	}

	@Ignore
	@RunWith(JUnit4.class)
	public static class IgnoreWholeClass {

		@AfterClass
		public static void tearDownClass() {
		}

		@Test
		public void test() {
		}
	}
}
