package aurora.plugin.script.engine;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.mozilla.javascript.Context;

public class TestMain {
	static AtomicInteger count = new AtomicInteger(0);
	static int NUM = 20;
	static LinkedList<Long> recent = new LinkedList<Long>();
	static int maxRecent = 500;

	static Random r = new Random();

	public static void main(String[] args) throws Exception {
		final long t = System.currentTimeMillis();
		for (int i = 0; i < NUM; i++) {
			new Thread() {
				public void run() {
					for (int j = 0; j < 1000; j++) {
						Context cx = Context.enter();
						// cx.setOptimizationLevel(r.nextInt(3));
						long t = 0;
						if (r.nextDouble() < 0.1) {
							t = System.nanoTime();
							add(t);
						} else
							t = get();
						CompiledScriptCache.getInstance().getScript(
								"var i=" + t, cx);
						Context.exit();
					}
					if (count.incrementAndGet() == NUM) {
						// for (int i = 0; i < recent.size(); i++)
						// System.out.println(recent.get(i));
						System.out.println(System.currentTimeMillis() - t);
					}
				}
			}.start();
		}

	}

	static synchronized void add(long i) {
		recent.add(i);
		while (recent.size() > maxRecent)
			recent.removeFirst();
	}

	static synchronized long get() {
		if (recent.isEmpty())
			return -1;
		int i = r.nextInt(recent.size());
		return recent.get(i);
	}
}
