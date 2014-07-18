package com.trivadis.disruptor.trade;

import static com.trivadis.disruptor.trade.TradeType.BID;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SingleThreadedClaimStrategy;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.trivadis.disruptor.trade.journaling.JournalingHandler;
import com.trivadis.disruptor.trade.replication.ReplicationHandler;

public final class StockExchangeTest {
	private static final int NUM_EVENT_PROCESSORS = 3;
	private static final int BUFFER_SIZE = 1024 * 8;
	private static final long ITERATIONS = 1000L * 1000L * 100L;
	private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_EVENT_PROCESSORS);

	private final long expectedResult;
	{
		long temp = 0L;

		for (long i = 0; i < ITERATIONS; i++) {
			final boolean replication = 0 == (i % 3L);
			final boolean journaling = 0 == (i % 5L);

			if (replication && journaling) {
				++temp;
			}
		}

		expectedResult = temp;
	}

	private final RingBuffer<TradeEvent> buffer = new RingBuffer<TradeEvent>(TradeEvent.EVENT_FACTORY,
			new SingleThreadedClaimStrategy(BUFFER_SIZE), new YieldingWaitStrategy());
	private final SequenceBarrier ringBarrier = buffer.newBarrier();

	private final ReplicationHandler replicationHandler = new ReplicationHandler();
	private final JournalingHandler journalingHandler = new JournalingHandler();
	private final BusinessLogicHandler blHandler = new BusinessLogicHandler();

	private final BatchEventProcessor<TradeEvent> replication = new BatchEventProcessor<TradeEvent>(buffer,
			ringBarrier, replicationHandler);
	private final BatchEventProcessor<TradeEvent> journaling = new BatchEventProcessor<TradeEvent>(buffer, ringBarrier,
			journalingHandler);

	private final SequenceBarrier journalingReplicationBarrier = buffer.newBarrier(replication.getSequence(),
			journaling.getSequence());
	private final BatchEventProcessor<TradeEvent> businessLogic = new BatchEventProcessor<TradeEvent>(buffer,
			journalingReplicationBarrier, blHandler);
	{
		buffer.setGatingSequences(businessLogic.getSequence());
	}

	@Test
	public void test_exchange() throws Exception {
		final CountDownLatch latch = new CountDownLatch(1);
		blHandler.reset(latch, ITERATIONS - 1);

		EXECUTOR.submit(replication);
		EXECUTOR.submit(journaling);
		EXECUTOR.submit(businessLogic);

		final long amount = 50;
		for (long productId = 0; productId < ITERATIONS; productId++) {
			final long sequence = buffer.next();
			buffer.get(sequence).trade(BID, productId, amount);
			buffer.publish(sequence);
		}

		latch.await();

		replication.halt();
		journaling.halt();
		businessLogic.halt();

		Assert.assertEquals(expectedResult, blHandler.getCounter());
	}
}
