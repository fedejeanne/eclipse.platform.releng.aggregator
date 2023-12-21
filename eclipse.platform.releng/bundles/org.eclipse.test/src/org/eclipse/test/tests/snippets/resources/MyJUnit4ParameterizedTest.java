package org.eclipse.test.tests.snippets.resources;

import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class MyJUnit4ParameterizedTest {

//	@BeforeClass
//	public static void beforeAll() {
//		throw new RuntimeException("Don't initialize this class");
//	}

	@AfterClass
	public static void afterAll() {
		throw new RuntimeException("Don't finalize this class");
	}



	@Parameters(name="String: {0}")
	public static Collection<String> getParameters() {
		return List.of("a", "b");
	}

	@Parameter
	public String parameterString;

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
	public void testIgnoreUsingAssumeWithMessage() {
		Assume.assumeFalse("Ignore ussing assumptions", true);
	}

	@Test
	public void testIgnoreUsingAssumeWithoutMessage() {
		Assume.assumeFalse(true);
	}

	@Test
	public void testError() {
		throw new RuntimeException("KABOOM!");
	}

	@Test(timeout= 100)
	public void testTimeout() throws InterruptedException {
		Thread.sleep(1000);
		fail("This test should have timed out by now");
	}

	@Test
	public void testOk() {
		// all fine, pass
	}
}
