package general.daoTests;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import model.Media;
import model.basicDataTypes.WithAccess;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;


import db.DB;

public class MediaDAOTest {

	@Test
	public void testCRUD() throws Exception {
		//create
		Media thumb = new Media();
		byte[] rawbytes = null;
		try {
			URL url = new URL("http://www.ntua.gr/schools/ece.jpg");
			File file = new File("test_java.txt");
			FileUtils.copyURLToFile(url, file);
			FileInputStream fileStream = new FileInputStream(
					file);

			rawbytes = IOUtils.toByteArray(fileStream);
		} catch(Exception e) {
			System.out.println(e);
			System.exit(-1);
		}

		thumb.setData(rawbytes);
		thumb.setType(Media.BaseType.IMAGE);
		thumb.setMimeType("image/jpeg");
		thumb.setHeight(599);
		thumb.setWidth(755);

		thumb.getRights().setIsPublic( true );
		thumb.getRights().addToAcl(new ObjectId() ,WithAccess.Access.OWN);
		thumb.getRights().addToAcl(new ObjectId() ,WithAccess.Access.WRITE);
		
		DB.getMediaDAO().makePermanent(thumb);
		//test succesful storage
		assertThat(thumb.getDbId()).isNotNull()
			.overridingErrorMessage("Media object didn't not save correctly in DB!");

		//retrieve from db
		Media a = DB.getMediaDAO().findById(thumb.getDbId());
		assertThat(a).isNotNull()
		.overridingErrorMessage("Test media not found after store.");

		//check is gone
		// DB.getMediaDAO().makeTransient(a);
		Media b = DB.getMediaDAO().findById(thumb.getDbId());
		assertThat( b )
		.overridingErrorMessage("User not deleted!")
		.isNull();

	}


	public Media testMediaStorage() throws Exception {

		Media image = null;
			//Create a Media Object
			image = new Media();


			URL url = new URL("http://www.ntua.gr/ntua-01.jpg");
			File file = new File("test_java.txt");
			FileUtils.copyURLToFile(url, file);
			FileInputStream fileStream = new FileInputStream(
					file);

			byte[] rawbytes = IOUtils.toByteArray(fileStream);


			image.setData(rawbytes);
			image.setType(Media.BaseType.IMAGE);
			image.setMimeType("image/jpeg");
			image.setHeight(599);
			image.setWidth(755);

			DB.getMediaDAO().makePermanent(image);

		assertThat( image.getDbId()).isNotNull();
		return image;

	}

}
