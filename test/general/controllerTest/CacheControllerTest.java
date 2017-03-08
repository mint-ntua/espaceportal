package general.controllerTest;

// all test should use those
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.GET;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;
import static play.test.Helpers.status;

import org.junit.Test;

import play.mvc.Http.Status;
import play.mvc.Result;

public class CacheControllerTest {

	public static long HOUR = 3600000;

	@Test
	public void testGetThumbnail() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				String url = "http://cdn.wonderfulengineering.com/wp-content/uploads/2014/07/Beautiful-Wallpapers-14.jpg";
				Result result = route(
						fakeRequest(GET, "/cache/byUrl?url=" + url), HOUR);
				assertThat(status(result)).isEqualTo(Status.OK);
			}
		});
	}
}