package org.cheetahplatform.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cheetahplatform.web.util.UserfileCleanup;

/**
 * Listens to the servlet context and starts a worker thread when the web app is started.
 *
 * @author Jakob
 */
public class CheetahServletContextListener implements ServletContextListener {
	// private static final int NUMBER_OF_WORKERS = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
	private static final int NUMBER_OF_WORKERS = 1;// TODO Bei commit löschen...
	private static List<CheetahWorker> workers;

	public static List<CheetahWorker> getWorkers() {
		return workers;
	}

	private TimerTask cleanupTimer;

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			for (CheetahWorker worker : workers) {
				worker.doShutdown();
				worker.interrupt();
			}
		} catch (Exception ex) {
			// ignore
		}

		cleanupTimer.cancel();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (workers == null) {
			workers = new ArrayList<>();
		}

		while (workers.size() < NUMBER_OF_WORKERS) {
			CheetahWorker worker = new CheetahWorker();
			worker.start();
			workers.add(worker);
		}

		cleanupTimer = new TimerTask() {
			@Override
			public void run() {
				new UserfileCleanup().cleanUp();
			}
		};
		long interval = 7 * 24 * 60 * 60 * 1000;
		new Timer(true).schedule(cleanupTimer, 0, interval);
	}
}
