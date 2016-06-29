package org.cheetahplatform.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listens to the servlet context and starts a worker thread when the web app is started.
 *
 * @author Jakob
 */
public class CheetahServletContextListener implements ServletContextListener {
	private static final int NUMBER_OF_WORKERS = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
	private static List<CheetahWorker> workers = null;

	public static List<CheetahWorker> getWorkers() {
		return workers;
	}

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
	}
}
