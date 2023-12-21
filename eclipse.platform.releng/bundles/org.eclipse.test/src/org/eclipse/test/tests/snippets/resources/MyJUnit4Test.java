package org.eclipse.test.tests.snippets.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MyJUnit4Test {
    @Rule public TestName testName = new TestName();

	@Before
	public void setup() {
		Assume.assumeFalse(testName.getMethodName().equals("testAbortUsingAssumeInSetUpMethod"));
		assertFalse(testName.getMethodName().equals("testFailOnSetUp"));
		if (testName.getMethodName().equals("testErrorOnSetUp")) {
			throw new RuntimeException("Error on setup");
		}
	}

	@After
	public void tearDown() {
		Assume.assumeFalse(testName.getMethodName().equals("testAbortUsingAssumeInTearDownMethod"));
		assertFalse(testName.getMethodName().equals("testFailOnTearDown"));
		if (testName.getMethodName().equals("testErrorOnTearDown")) {
			throw new RuntimeException("Error on tearDown");
		}
	}

	@Test
	public void testAbortUsingAssumeInSetUpMethod() {
		// will be ignored because of the assumption in the @Before method
	}

	@Test
	public void testAbortUsingAssumeInTearDownMethod() {
		// will be ignored because of the assumption in the @After method
	}

	@Test
	public void testFailOnSetUp() {
		// will fail because of the assertion in setup-method
	}

	@Test
	public void testFailOnTearDown() {
		// will fail because of the assertion in tearDown-method
	}

	@Test
	public void testErrorOnSetUp() {
		// will throw an exception in setUp-method
	}

	@Test
	public void testErrorOnTearDown() {
		// will throw an exception in tearDown-method
	}

	@Test
	@Ignore("Ignoring with annotation")
	public void testIgnoreUsingAnnotationWithMessage() {
		throw new RuntimeException("This will never happen");
	}

	@Test
	@Ignore
	public void testIgnoreUsingAnnotationWithoutMessage() {
		throw new RuntimeException("This will never happen");
	}

	@Test
	public void testAbortUsingAssumeWithMessage() {
		Assume.assumeFalse("Ignore ussing assumptions", true);
	}

	@Test
	public void testAbortUsingAssumeWithoutMessage() {
		Assume.assumeFalse(true);
	}

	@Test
	public void testError() {
		throw new RuntimeException("KABOOM!");
	}

	@Test(timeout= 100)
	public void testErrorBecauseOfTimeout() throws InterruptedException {
		Thread.sleep(1000);
		fail("This test should have timed out by now");
	}

	@Test
	public void testOk() {
		// all fine, pass
	}


}
