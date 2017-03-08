package controllers;

import play.mvc.Result;

public class PreFlight extends WithController {

	public static Result checkPreFlight(String path) {
		return ok();
	}

}
