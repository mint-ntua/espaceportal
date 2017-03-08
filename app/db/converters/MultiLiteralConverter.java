package db.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import com.mongodb.BasicDBList;

import model.basicDataTypes.MultiLiteral;

@Converters(MultiLiteralConverter.class)
public class MultiLiteralConverter extends TypeConverter{

	public MultiLiteralConverter() {
		super(MultiLiteral.class);
	}

	@Override
	public Object decode(Class targetClass, Object fromDBObject,
			MappedField optionalExtraInfo) {
		if (fromDBObject == null) 
            return null;
		else {
			MultiLiteral outMap = new MultiLiteral();
			for (Entry<String, BasicDBList> e: ((Map<String, BasicDBList>) fromDBObject).entrySet()) {
				ArrayList<String> outList = new ArrayList<String>();
				for (Object s: e.getValue()) {
					outList.add((String) s);
				}
				outMap.put(e.getKey(), outList);
			}
			return outMap;
		}
	}

}
