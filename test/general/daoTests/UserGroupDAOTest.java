package general.daoTests;

import general.TestUtils;

import java.util.List;

import junit.framework.TestCase;
import model.usersAndGroups.Organization;
import model.usersAndGroups.Project;
import model.usersAndGroups.UserGroup;

import org.bson.types.ObjectId;
import org.junit.Test;

import controllers.GroupManager.GroupType;
import db.DB;

/**
 * The class <code>UserGroupDAOTest</code> contains tests for the class {@link <code>UserGroupDAO</code>}
 *
 * @pattern JUnit Test Case
 *
 * @generatedBy CodePro at 9/25/15 1:08 PM
 *
 * @author mariaral
 *
 * @version $Revision$
 */
public class UserGroupDAOTest extends TestCase {

	/**
	 * Construct new test instance
	 *
	 * @param name
	 *            the test name
	 */
	public UserGroupDAOTest(String name) {
		super(name);
	}

	@Test
	public static ObjectId createTestUserGroup(ObjectId admin) {
		UserGroup group = new UserGroup();
		group.setUsername("testUserGroup" + Math.random());
		group.getAdminIds().add(admin);
		group.getUsers().add(admin);
		DB.getUserGroupDAO().makePermanent(group);
		return group.getDbId();
	}
	
	@Test
	public static ObjectId createTestOrganization(ObjectId admin) {
		Organization group = new Organization();
		group.setUsername("testOrganization" + Math.random());
		group.getAdminIds().add(admin);
		group.getUsers().add(admin);
		DB.getUserGroupDAO().makePermanent(group);
		return group.getDbId();
	}

	public static ObjectId createTestProject(ObjectId admin) {
		Project group = new Project();
		group.setUsername("testUserGroup" + Math.random());
		group.getAdminIds().add(admin);
		group.getUsers().add(admin);
		DB.getUserGroupDAO().makePermanent(group);
		return group.getDbId();
	}

	public static ObjectId createChildGroup(ObjectId parentId) {
		UserGroup group = new UserGroup();
		group.setUsername("testUserGroup" + Math.random());
		group.getParentGroups().add(parentId);
		DB.getUserGroupDAO().makePermanent(group);
		return group.getDbId();
	}
	/**
	 * Run the List<UserGroup> findByUserIdAll(ObjectId, GroupType) method test
	 */
	public void testFindByUserIdAll() {
		ObjectId userId = UserDAOTest.createTestUser();
		ObjectId groupId = createTestUserGroup(userId);
		ObjectId organizationId = createTestOrganization(userId);
		ObjectId projectId = createTestProject(userId);
		List<UserGroup> result = DB.getUserGroupDAO().findByUserIdAll(userId,
				GroupType.All);
		assertEquals(3, result.size());
		result = DB.getUserGroupDAO().findByUserIdAll(userId,
				GroupType.UserGroup);
		assertEquals(1, result.size());
		assertEquals(groupId, result.get(0).getDbId());
		result = DB.getUserGroupDAO().findByUserIdAll(userId,
				GroupType.Organization);
		assertEquals(1, result.size());
		assertEquals(organizationId, result.get(0).getDbId());
		result = DB.getUserGroupDAO().findByUserIdAll(userId,
				GroupType.Project);
		assertEquals(1, result.size());
		assertEquals(projectId, result.get(0).getDbId());
	}
	
	public void testFindByParent() {
		ObjectId userId = UserDAOTest.createTestUser();
		ObjectId parentId = createTestUserGroup(userId);
		ObjectId childId = createChildGroup(parentId);
		List<UserGroup> result = DB.getUserGroupDAO().findByParent(parentId, GroupType.All);
		assertEquals(1, result.size());
		assertEquals(childId, result.get(0).getDbId());
	}

}