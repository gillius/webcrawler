package org.gillius.webcrawler.resourceloader

import groovy.transform.CompileStatic

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An ExecutorService implementation that immediately runs any task given to it, as a way to turn normally
 * asynchronous calls into synchronous ones. Submitted tasks are executed immediately by the thread that is submitting
 * them.
 */
@CompileStatic
class ImmediateExecutorService extends AbstractExecutorService {
	AtomicBoolean shutdown = new AtomicBoolean(false)

	@Override
	void shutdown() {
		shutdown.set(true)
	}

	@Override
	List<Runnable> shutdownNow() {
		return Collections.emptyList()
	}

	@Override
	boolean isShutdown() {
		return shutdown.get()
	}

	@Override
	boolean isTerminated() {
		return shutdown.get()
	}

	@Override
	boolean awaitTermination(long timeout, TimeUnit unit) {
		return shutdown.get()
	}

	@Override
	void execute(Runnable command) {
		command.run()
	}
}
