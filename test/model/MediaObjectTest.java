package model;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import model.basicDataTypes.LiteralOrResource;
import model.EmbeddedMediaObject.WithMediaRights;
import model.EmbeddedMediaObject.WithMediaType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.google.common.net.MediaType;

import db.DB;

public class MediaObjectTest {

	@Test
	public void genericTest() {

		MediaObject mo = new MediaObject();
		byte[] rawbytes = null;
		URL url = null;
		try {
			url = new URL("http://www.ntua.gr/schools/ece.jpg");
			File file = new File("test_java.txt");
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

			if (readers.hasNext()) {

				// pick the first available ImageReader
				ImageReader reader = readers.next();

				// attach source to the reader
				reader.setInput(iis, true);

				// read metadata of first image
				IIOMetadata metadata = reader.getImageMetadata(0);

				String[] names = metadata.getMetadataFormatNames();
				int length = names.length;
				for (int i = 0; i < length; i++) {
					System.out.println("Format name: " + names[i]);
				}
			}

			FileUtils.copyURLToFile(url, file);
			FileInputStream fileStream = new FileInputStream(file);

			rawbytes = IOUtils.toByteArray(fileStream);
		} catch (Exception e) {
			System.out.println(e);
			System.exit(-1);
		}

		mo.setMediaBytes(rawbytes);
		mo.setMimeType(MediaType.ANY_IMAGE_TYPE);
		mo.setHeight(875);
		mo.setWidth(1230);
		//LiteralOrResource lor = LiteralOrResource.build(url.toString());
		//mo.setOriginalRights(lor);
		mo.setWithRights(WithMediaRights.Creative);
		mo.setType(WithMediaType.IMAGE);
		mo.setUrl(url.toString());

		try {
			DB.getMediaObjectDAO().makePermanent(mo);
			System.out.println("Media succesfully saved!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MediaObject nmo = DB.getMediaObjectDAO().findById(mo.getDbId());
		if (nmo != null)
			System.out.println("Media succesfuly retieved!");

		DB.getMediaObjectDAO().deleteById(nmo.getDbId());
		System.out.println("Succesfully deleted!");

	}
}
