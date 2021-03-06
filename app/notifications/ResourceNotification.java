package notifications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.basicDataTypes.Language;
import model.basicDataTypes.WithAccess.Access;
import model.resources.WithResource;
import model.usersAndGroups.User;
import model.usersAndGroups.UserGroup;

import org.bson.types.ObjectId;

import utils.Serializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import db.DB;

public class ResourceNotification extends Notification {

	// The resource related with the action
	@JsonSerialize(using = Serializer.ObjectIdSerializer.class)
	private ObjectId resource;

	public static class ShareInfo {
		//the userOrGroup the resource is shared with
		@JsonSerialize(using = Serializer.ObjectIdSerializer.class)
		private ObjectId userOrGroup;
		private Access previousAccess;
		private Access newAccess;
		//effectiveIds of the owner of the resource - the one who does the sharing
		private List<ObjectId> ownerEffectiveIds;

		public Access getNewAccess() {
			return newAccess;
		}
		public void setNewAccess(Access newAccess) {
			this.newAccess = newAccess;
		}
		public Access getPreviousAccess() {
			return previousAccess;
		}
		public void setPreviousAccess(Access previousAccess) {
			this.previousAccess = previousAccess;
		}
		public List<ObjectId> getOwnerEffectiveIds() {
			return ownerEffectiveIds;
		}
		public void setOwnerEffectiveIds(List<ObjectId> ownerEffectiveIds) {
			this.ownerEffectiveIds = ownerEffectiveIds;
		}
		public ObjectId getUserOrGroup() {
			return userOrGroup;
		}
		public void setUserOrGroup(ObjectId userOrGroup) {
			this.userOrGroup = userOrGroup;
		}

		public Boolean sharedWithGroup() {
			if (userOrGroup != null) {
				if (DB.getUserDAO().existsEntity(userOrGroup))
					return false;
				else if (DB.getUserGroupDAO().existsEntity(userOrGroup))
					return true;
			}
			return null;
		}

		public String getUserOrGroupName() {
			String username;
			if (userOrGroup == null)
				return "DELETED";
			User user = DB.getUserDAO().getById(userOrGroup, new ArrayList<String>(Arrays.asList("username")));
			if (user != null)
				username = user.getUsername();
			else {
				UserGroup userGroup =  DB.getUserGroupDAO().getById(userOrGroup, new ArrayList<String>(Arrays.asList("username")));
				if (userGroup == null)
					return "DELETED";
				username = userGroup.getUsername();
			}
			return username;
		}
	}

	private ShareInfo shareInfo;

	public ObjectId getResource() {
		return resource;
	}

	public void setResource(ObjectId resource) {
		this.resource = resource;
	}

	public String getResourceName() {
		if (this.resource == null) {
			return null;
		}
		WithResource withResource = DB.getCollectionObjectDAO().getById(resource, new ArrayList<String>() {{ add("descriptiveData.label"); }});
		if (withResource == null)
			withResource = DB.getRecordResourceDAO().getById(resource, new ArrayList<String>() {{ add("descriptiveData.label"); }});
		if (withResource != null)
			return withResource.getDescriptiveData().getLabel().get(Language.DEFAULT).get(0);
		else
			return "DELETED";
	}

	public ShareInfo getShareInfo() {
		return shareInfo;
	}

	public void setShareInfo(ShareInfo shareInfo) {
		this.shareInfo = shareInfo;
	}



}
