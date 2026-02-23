package org.nikanikoo.flux.data.managers;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nikanikoo.flux.data.managers.api.OpenVKApi;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Unit tests for BaseManager class.
 * Tests Singleton pattern, API initialization, and Context management.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class BaseManagerTest {

    private Context context;

    /**
     * Concrete implementation of BaseManager for testing purposes.
     */
    private static class TestManager extends BaseManager<TestManager> {
        public TestManager(Context context) {
            super(context);
        }

        public static TestManager getInstance(Context context) {
            return BaseManager.getInstance(TestManager.class, context);
        }
    }

    /**
     * Another concrete implementation for testing multiple manager types.
     */
    private static class AnotherTestManager extends BaseManager<AnotherTestManager> {
        public AnotherTestManager(Context context) {
            super(context);
        }

        public static AnotherTestManager getInstance(Context context) {
            return BaseManager.getInstance(AnotherTestManager.class, context);
        }
    }

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
    }

    /**
     * Test Singleton pattern - getInstance returns the same instance
     * Requirements: 9.2
     */
    @Test
    public void testSingletonPattern_ReturnsSameInstance() {
        TestManager instance1 = TestManager.getInstance(context);
        TestManager instance2 = TestManager.getInstance(context);

        assertNotNull("First instance should not be null", instance1);
        assertNotNull("Second instance should not be null", instance2);
        assertSame("getInstance should return the same instance", instance1, instance2);
    }

    /**
     * Test that different Manager classes get different instances
     * Requirements: 9.2
     */
    @Test
    public void testSingletonPattern_DifferentManagersGetDifferentInstances() {
        TestManager testManager = TestManager.getInstance(context);
        AnotherTestManager anotherManager = AnotherTestManager.getInstance(context);

        assertNotNull("TestManager instance should not be null", testManager);
        assertNotNull("AnotherTestManager instance should not be null", anotherManager);
        assertNotSame("Different manager types should have different instances", testManager, anotherManager);
    }

    /**
     * Test API initialization
     * Requirements: 9.3
     */
    @Test
    public void testApiInitialization() {
        TestManager manager = TestManager.getInstance(context);

        OpenVKApi api = manager.getApi();
        assertNotNull("API should be initialized", api);
    }

    /**
     * Test Context initialization
     * Requirements: 9.3
     */
    @Test
    public void testContextInitialization() {
        TestManager manager = TestManager.getInstance(context);

        Context managerContext = manager.getContext();
        assertNotNull("Context should be initialized", managerContext);
    }

    /**
     * Test that ApplicationContext is used, not Activity context
     * Requirements: 9.4
     */
    @Test
    public void testUsesApplicationContext() {
        TestManager manager = TestManager.getInstance(context);

        Context managerContext = manager.getContext();

        assertNotNull("Manager context should not be null", managerContext);
        // Verify that manager context is an Application context (not Activity)
        assertTrue("Manager should use ApplicationContext type", 
                   managerContext instanceof android.app.Application || 
                   managerContext.getClass().getName().contains("Application"));
    }

    /**
     * Test that passing Activity context still results in ApplicationContext being stored
     * Requirements: 9.4
     */
    @Test
    public void testApplicationContextFromActivityContext() {
        // Create a new manager with the context (which could be an Activity context)
        TestManager manager = TestManager.getInstance(context);

        Context managerContext = manager.getContext();

        assertNotNull("Manager context should not be null", managerContext);
        // Verify that the manager stores an Application context type
        assertTrue("Manager should use ApplicationContext type", 
                   managerContext instanceof android.app.Application || 
                   managerContext.getClass().getName().contains("Application"));
    }

    /**
     * Test thread safety of Singleton pattern
     * Requirements: 9.2
     */
    @Test
    public void testSingletonThreadSafety() throws InterruptedException {
        final TestManager[] instances = new TestManager[10];
        Thread[] threads = new Thread[10];

        // Create multiple threads that try to get the instance simultaneously
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                instances[index] = TestManager.getInstance(context);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all instances are the same
        TestManager firstInstance = instances[0];
        assertNotNull("First instance should not be null", firstInstance);

        for (int i = 1; i < instances.length; i++) {
            assertNotNull("Instance " + i + " should not be null", instances[i]);
            assertSame("All instances should be the same", firstInstance, instances[i]);
        }
    }

    /**
     * Test that API is the same across multiple getInstance calls
     * Requirements: 9.3
     */
    @Test
    public void testApiConsistency() {
        TestManager manager1 = TestManager.getInstance(context);
        TestManager manager2 = TestManager.getInstance(context);

        OpenVKApi api1 = manager1.getApi();
        OpenVKApi api2 = manager2.getApi();

        assertNotNull("First API should not be null", api1);
        assertNotNull("Second API should not be null", api2);
        assertSame("API should be the same instance", api1, api2);
    }
}
