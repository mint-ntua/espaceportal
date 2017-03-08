package general;

import java.util.Date;

import model.ApiKey;

import org.junit.Test;

import db.DB;

public class Temp {


	@Test
	public void test1() {
		// create an ApiKey
		ApiKey k = new ApiKey();
		// should cover localhost
		//k.setIpPattern("((0:){7}.*)|(127.0.0.1)");
		// set it to expired
		k.setExpires(new Date(new Date().getTime()));
		k.addCall(0, ".*");

		// store it
		DB.getApiKeyDAO().makePermanent(k);
	}
}
