package general.controllerTest;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;
import static play.test.Helpers.status;

import org.junit.Test;

import play.libs.Json;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestControllerAuthorization {
	public static long HOUR = 3600000;

	@Test
	public void testAuthorization() {

		final ObjectNode json = Json.newObject();
		json.put("description", "TEst controller");
		json.put("title", "The test title");
		json.put("public", "true");
		json.put("ownerMail", "heres42@mongo.gr");
		running( fakeApplication(), new Runnable() {
			@Override
			public void run() {
				Result result = route(fakeRequest("POST", "/collection/add")
						.withJsonBody(json)
						.withSession("user", "blabla"), HOUR);

			    assertThat(status(result)).isEqualTo(OK);
			}
		});

	    }
	@Test
	public void testNoAuthorization() {
		running( fakeApplication(), new Runnable() {
			@Override
			public void run() {
				Result result = route(fakeRequest("POST", "/collection/add"), HOUR );

				System.out.println();
				assertThat(status(result)).isEqualTo(BAD_REQUEST);
			}
		});
	}
}
