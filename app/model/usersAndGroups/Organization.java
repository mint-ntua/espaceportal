package model.usersAndGroups;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.List;

@Entity("UserGroup")
public class Organization extends UserGroup {

	@Embedded
	private Page page;
	
	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}
}
