package com.trivadis.disruptor.test.support;

import org.junit.Assert;

public abstract class AbstractPerfTestQueueVsDisruptor {
	public static final int RUNS = 3;

	protected void testImplementations() throws Exception {
		final int availableProcessors = Runtime.getRuntime().availableProcessors();
		if (getRequiredProcessorCount() > availableProcessors) {
			System.out
					.print("*** Warning ***: your system has insufficient processors to execute the test efficiently. ");
			System.out.println("Processors required = " + getRequiredProcessorCount() + " available = "
					+ availableProcessors);
		}

		final long queueOps[] = new long[RUNS];
		final long disruptorOps[] = new long[RUNS];

		// run queue tests
		if ("true".equalsIgnoreCase(System.getProperty("runQueueTests", "true"))) {
			for (int i = 0; i < RUNS; i++) {
				System.gc();
				queueOps[i] = runQueuePass();
				System.out.println("Completed BlockingQueue run " + i);
			}

		}
		// run disruptor tests
		if ("true".equalsIgnoreCase(System.getProperty("runDisruptorTests", "true"))) {
			for (int i = 0; i < RUNS; i++) {
				System.gc();
				disruptorOps[i] = runDisruptorPass();
				System.out.println("Completed Disruptor run " + i);
			}
		}
		printResults(getClass().getSimpleName(), disruptorOps, queueOps);

		for (int i = 0; i < RUNS; i++) {
			Assert.assertTrue("Performance degraded", disruptorOps[i] > queueOps[i]);
		}
	}

	public static void printResults(final String className, final long[] disruptorOps, final long[] queueOps) {
		for (int i = 0; i < RUNS; i++) {
			System.out.format("%s run %d: BlockingQueue=%,d Disruptor=%,d ops/sec\n", className, Integer.valueOf(i),
					Long.valueOf(queueOps[i]), Long.valueOf(disruptorOps[i]));
		}
	}

	protected abstract int getRequiredProcessorCount();

	protected abstract long runQueuePass() throws Exception;

	protected abstract long runDisruptorPass() throws Exception;

	protected abstract void shouldCompareDisruptorVsQueues() throws Exception;
}
