package com.trivadis.disruptor.test.support;
public final class PerfTestUtil
{
    public static long accumulatedAddition(final long iterations)
    {
        long temp = 0L;
        for (long i = 0L; i < iterations; i++)
        {
            temp += i;
        }

        return temp;
    }
}
