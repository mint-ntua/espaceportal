package general.controllerTest;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;
import static play.test.Helpers.status;
import model.usersAndGroups.User;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import play.api.libs.json.Json;
import play.mvc.Result;
import db.DB;

public class SearchControllerTest {



	@Test
	public void testSearch() {

		/*
		 * Owner of the CollectionObject
		 *
		 */
		User u = DB.getUserDAO().getByUsername("qwerty");
		if(u == null) {
			System.out.println("No user found");
			return;
		}


		running( fakeApplication(), new Runnable() {
			@Override
			public void run() {
				Result result = route(fakeRequest("POST", "/api/advancedsearch")
						.withSession("user", u.getDbId().toHexString())
						.withJsonBody(Json.parse("{\"searchTerm\":\"grass\",\"page\":1,\"pageSize\":20,"
								+ "\"source\":[\"WITHin\"],\"filters\":[]}")));

			    JsonParser parser = new JsonParser();
			    Gson gson = new GsonBuilder().setPrettyPrinting().create();
			    JsonElement el = parser.parse(contentAsString(result));
			    System.out.println(gson.toJson(el));

			    if(status(result) == 200)
				    assertThat(status(result)).isEqualTo(OK);
			    else {
			    	System.out.println(status(result));
			    	Assert.fail();
			    }

			}
		});
	}
}
