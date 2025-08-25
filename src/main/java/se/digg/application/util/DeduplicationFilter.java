/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Deduplication filter for excessive logging
 */
public class DeduplicationFilter extends TurboFilter
{
	private static final Marker deduplicateMarker = MarkerFactory.getMarker("DEDUPLICATE");
	private static final int CACHE_SIZE = 8;
	private static final int DUPLICATE_LOG_COUNT = 1000;

	@RequiredArgsConstructor
	@EqualsAndHashCode(exclude = {"count"})
	private static class LogException
	{
		private final String message;
		private final StackTraceElement[] stackTraceElements;
		private volatile int count;
		private volatile AtomicInteger c = new AtomicInteger(0);
	}

	private final Deque<LogException> cache = new ConcurrentLinkedDeque<>();

	@Override
	public void stop()
	{
		cache.clear();
		super.stop();
	}

	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects, Throwable throwable)
	{
		if (marker != deduplicateMarker || logger.isDebugEnabled() || throwable == null)
		{
			return FilterReply.NEUTRAL;
		}

		LogException logException = new LogException(s, throwable.getStackTrace());
		for (LogException e : cache)
		{
			if (logException.equals(e))
			{
				if (e.c.incrementAndGet() % DUPLICATE_LOG_COUNT == 0)
				{
					logger.warn("following log message logged " + DUPLICATE_LOG_COUNT + " times!");
					return FilterReply.NEUTRAL;
				}
				return FilterReply.DENY;
			}
		}

		synchronized (cache)
		{
			if (cache.size() >= CACHE_SIZE)
			{
				cache.pop();
			}
			cache.push(logException);
		}

		return FilterReply.NEUTRAL;
	}
}