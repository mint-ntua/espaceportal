package notifications;

import model.usersAndGroups.UserGroup;

import org.bson.types.ObjectId;

import utils.Serializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import db.DB;

public class GroupNotification extends Notification {
	
	// The group that is involved with the action (if group related)
	@JsonSerialize(using = Serializer.ObjectIdSerializer.class)
	private ObjectId group;
	
	public ObjectId getGroup() {
		return group;
	}

	public void setGroup(ObjectId group) {
		this.group = group;
	}

	public String getGroupName() {
		if (this.group == null) {
			return null;
		}
		UserGroup gr = DB.getUserGroupDAO().get(this.group);
		if (gr != null) {
			return gr.getFriendlyName();
		}
		return "DELETED";
	}

}
