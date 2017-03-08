package actors;

import notifications.Notification;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import db.DB;
import model.usersAndGroups.User;
import model.usersAndGroups.UserGroup;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import utils.NotificationCenter;

/**
 *
 * @author Arne Stabenau The actor that talks with the web app.
 */
public class NotificationActor extends UntypedActor {
	public static final ALogger log = Logger.of(NotificationActor.class);

	private User loggedInUser = null;
	private final ActorRef out;

	public static Props props(ActorRef out) {
		return Props.create(NotificationActor.class, out);
	}

	public NotificationActor(ActorRef out) {
		this.out = out;
	}

	public void preStart() {
		NotificationCenter.addActor(getSelf());
	}

	public void postStop() {
		NotificationCenter.removeActor(getSelf());
	}

	public void onReceive(Object message) throws Exception {
		if (message instanceof JsonNode) {
			// only login / logout messages are expected
			JsonNode json = (JsonNode) message;
			log.debug("Received Message from browser: " + json.toString());
			if (json.get("action").asText().equals("login")) {
				String id = json.get("id").asText();
				try {
					loggedInUser = DB.getUserDAO().get(new ObjectId(id));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			} else if (json.get("action").asText().equals("logout")) {
				String id = json.get("id").asText();
				if (loggedInUser != null && id.equals(loggedInUser.getDbId().toString())) {
					loggedInUser = null;
				} else {
					log.error("User is already logged out");
				}
			}
		}
		if (message instanceof Notification) {
			Notification notification = (Notification) message;
			if (loggedInUser == null) {
				return;
			}
			if (loggedInUser.getDbId().equals(notification.getReceiver())) {
				out.tell(Json.toJson(notification), self());
			} else {
				UserGroup group = DB.getUserGroupDAO().get(notification.getReceiver());
				if (group != null && group.getAdminIds().contains(loggedInUser)) {
					out.tell(Json.toJson(notification), self());
				}
			}
		}
	}
}
