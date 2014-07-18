package com.trivadis.contention;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import com.trivadis.disruptor.test.support.Reportable;
import com.trivadis.disruptor.test.support.Reporter;

public class ContentionTest {
	private static final long COUNT = 500 * 1000 * 1000;
	private static final long COUNT_TWO = 500 * 1000 * 1000 / 2;

	private long var = 0;
	private volatile long varVolatile = 0;
	private final AtomicLong varAtomicLong = new AtomicLong(0);

	@Test
	public void test_increment_single_threaded() {
		final Reporter reporter = new Reporter(new Reportable() {
			@Override
			public void call() {
				for (long i = 0; i < COUNT; i++) {
					var++;
				}
			}
		});
		assertEquals(COUNT, var);
		reporter.fazit("threads: one | long");
	}

	@Test
	public void test_increment_single_threaded_volatile() throws Exception {
		final Reporter reporter = new Reporter(new Reportable() {
			@Override
			public void call() {
				for (long i = 0; i < COUNT; i++) {
					varVolatile++;
				}
			}
		});
		assertEquals(COUNT, varVolatile);
		reporter.fazit("threads: one | volatile");
	}

	@Test
	public void test_increment_single_threaded_atomic() throws Exception {
		final Reporter reporter = new Reporter(new Reportable() {
			@Override
			public void call() {
				for (long i = 0; i < COUNT; i++) {
					varAtomicLong.incrementAndGet();
				}
			}
		});
		assertEquals(COUNT, varAtomicLong.get());
		reporter.fazit("threads: one | atomic");
	}

	private final ReentrantLock lock = new ReentrantLock();

	@Test
	public void test_increment_single_threaded_lock() {
		final Reporter reporter = new Reporter(new Reportable() {
			@Override
			public void call() {
				for (long i = 0; i < COUNT; i++) {
					try {
						lock.lock();
						var++;
					} finally {
						lock.unlock();
					}
				}
			}
		});
		assertEquals(COUNT, var);
		reporter.fazit("threads: one | lock");
	}

	private final ExecutorService executor = Executors.newFixedThreadPool(2);

	@Test
	public void test_increment_two_threads_atomic() throws Exception {
		final Reporter report = new Reporter(new Reportable() {

			@Override
			public void call() {
				final Runnable runnable = new Runnable() {
					@Override
					public void run() {
						for (long i = 0; i < COUNT_TWO; i++) {
							varAtomicLong.incrementAndGet();
						}
					}
				};
				final Future<?> result01 = executor.submit(runnable);
				final Future<?> result02 = executor.submit(runnable);
				try {
					result01.get();
					result02.get();
					assertEquals(COUNT, varAtomicLong.get());
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		report.fazit("threads: two | atomic");
	}

	@Test
	public void test_increment_two_threads_lock() throws Exception {
		final Reporter report = new Reporter(new Reportable() {

			@Override
			public void call() {
				final Runnable runnable = new Runnable() {
					@Override
					public void run() {
						for (long i = 0; i < COUNT_TWO; i++) {
							try {
								lock.lock();
								var++;
							} finally {
								lock.unlock();
							}
						}
					}
				};
				final Future<?> result01 = executor.submit(runnable);
				final Future<?> result02 = executor.submit(runnable);
				try {
					result01.get();
					result02.get();
					assertEquals(COUNT, var);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		report.fazit("threads two: | lock");
	}
}
