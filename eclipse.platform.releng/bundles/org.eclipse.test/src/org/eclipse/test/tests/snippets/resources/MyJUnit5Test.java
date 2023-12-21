package org.eclipse.test.tests.snippets.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

public class MyJUnit5Test {

	@Test
	@EnabledIfSystemProperty(named = "java.vm.vendor", matches = "Oracle.*")
	public void onlyIfVendorNameStartsWithOracle() {
	    //...
	}

//	@TestFactory
//	Collection<DynamicTest> getDynamicallyCreatedTests() {
//		return List.of(
//				dynamicTest("testDynamicOne", () -> {}),
//				dynamicTest("testDynamicSkippedUsingAssumptions", () -> {
//					Assumptions.assumeFalse(true);
//				})
//				);
//	}
//
//	@TestFactory
//	@Disabled("This test factory is disabled")
//	Collection<DynamicTest> factoryWorkersWentOnStrike() {
//		return List.of(
//				dynamicTest("testStrikeOne", () -> {}),
//				dynamicTest("testStrikeTwo", () -> {})
//				);
//	}
//
//
//	@Test
//	@Disabled("Disabling with annotation")
//	void testIgnoreUsingAnnotationWithMessage() {
//		throw new RuntimeException("This will never happen");
//	}
//
//	@Test
//	@Disabled
//	void testIgnoreUsingAnnotationWithoutMessage() {
//		throw new RuntimeException("This will never happen");
//	}
//
//	@Test
//	void testIgnoreUsingAssumeWithMessage() {
//		Assumptions.assumeFalse(true, "Ignore ussing assumptions");
//	}
//
//	@Test
//	void testIgnoreUsingAssumeWithoutMessage() {
//		Assumptions.assumeFalse(true);
//	}
//
//	@Test
//	void testError() {
//		throw new RuntimeException("KABOOM!");
//	}
//
//	@Test
//	@Timeout(value= 100, unit= TimeUnit.MILLISECONDS)
//	void testTimeout() throws InterruptedException {
//		Thread.sleep(1000);
//		fail("This test should have timed out by now");
//	}
//
//	@Test
//	void testOk() {
//		// all fine, pass
//	}
}
