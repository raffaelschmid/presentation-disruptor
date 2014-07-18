package com.trivadis.disruptor.test.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reporter {

	private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
	private static final BigDecimal MILLION = THOUSAND.multiply(THOUSAND);
	private static final BigDecimal BILLION = MILLION.multiply(THOUSAND);

	private static final Logger LOG = LoggerFactory.getLogger(Reporter.class);
	private final BigDecimal start, end, nanos, mikros, sec, millis;

	public Reporter(final Reportable reportable) {
		start = BigDecimal.valueOf(System.nanoTime());
		reportable.call();
		end = BigDecimal.valueOf(System.nanoTime());

		nanos = end.subtract(start);
		mikros = nanos.divide(THOUSAND);
		millis = nanos.divide(MILLION);
		sec = nanos.divide(BILLION);
	}

	public void fazit(final long noOfTransactions) {
		LOG.info("----------------------------------------------------------------");
		LOG.info("REPORT");
		LOG.info("----------------------------------------------------------------");

		LOG.info("time used (nano):          {}", nanos);

		LOG.info("time used (mikro):         {}", mikros);
		LOG.info("time used (milli):         {}", millis);
		LOG.info("time used (second):        {}", sec);
		LOG.info("----------------------------------------------------------------");
		LOG.info("Number of Transactions:    {}", noOfTransactions);
		LOG.info("Avg. Transactions/Second:  {}",
				BigDecimal.valueOf(noOfTransactions).divide(sec, 3, RoundingMode.HALF_UP));
	}

	public void fazit(final String text) {
		LOG.info("----------------------------------------------------------------");
		LOG.info(text + ":         {} (millis)", millis);
	}

}
