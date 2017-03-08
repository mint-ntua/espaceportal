package general;

import java.util.List;

import model.Collection;

import org.junit.Test;

import db.DB;

public class MongoScripts {

	@Test
	public void updateCollectionRights() {

		long colCount = DB.getCollectionDAO().count();
		for(int i = 0;i < (colCount/50); i++) {
			List<Collection> cols = DB.getCollectionDAO().getAll(i*50, (i*50)+50);
			for(Collection col: cols) {
				DB.getCollectionDAO().makePermanent(col);
			}
		}
		List<Collection> last = DB.getCollectionDAO().getAll(((int)colCount%50)+2, (int)colCount);
	}
}
