package utils;

import java.util.HashSet;
import java.util.Set;

import notifications.Notification;
import akka.actor.ActorRef;

/**
 * Mostly static, keeping all actors for websockets together, so they can
 * receive messages
 * 
 * @author Arne Stabenau
 *
 */
public class NotificationCenter {

	// Messages are send explicitly to people logged in,
	// Do we want to store them? For people that are not logged in?
	// Primary use for now is testing the websockets.

	public static Set<ActorRef> notificationActors = new HashSet<ActorRef>();

	public static void addActor(ActorRef ref) {
		notificationActors.add(ref);
	}

	public static void removeActor(ActorRef ref) {
		notificationActors.remove(ref);
	}

	// The actor refs wil decide themselves if this is ok for them

	public static void sendNotification(Notification notification) {
		for (ActorRef ref : notificationActors) {
			ref.tell(notification, null);
		}
	}
}
