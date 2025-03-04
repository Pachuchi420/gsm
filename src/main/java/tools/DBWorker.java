package tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DBWorker {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());

    public void submitTask(Runnable task) {
        executor.submit(task);
    }

    // Custom thread factory to create daemon threads
    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true); // 🔥 Set thread as daemon so it won't prevent app exit
            return thread;
        }
    }
}