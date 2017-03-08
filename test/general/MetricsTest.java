package general;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.*;

public class MetricsTest {

	static final MetricRegistry metrics = new MetricRegistry();
	private static final Histogram responseSizes = metrics.histogram("test");
	public static void main(String[] args) {
		startReport();
		metrics.name(MetricsTest.class, "example", "metrics");
		metrics.name(MetricsTest.class, "example", "metrics-2");
		Meter requests = metrics.meter("requests");
		responseSizes.update(500);
		requests.mark();
		wait5Seconds();
		responseSizes.update(500);

		requests.mark();
		wait5Seconds();
	}

	static void startReport() {
		ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
				.filter(MetricFilter.ALL)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS).build();
		reporter.start(1, TimeUnit.SECONDS);
	}

	static void wait5Seconds() {
		try {
			Thread.sleep(1 * 1000);
		} catch (InterruptedException e) {
		}
	}

}
