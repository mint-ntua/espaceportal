package db.converters;

import model.basicDataTypes.WithAccess.Access;

import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

public class AccessEnumConverter extends TypeConverter {

	public AccessEnumConverter() {
		super(Access.class);
	}

	@Override
	public Object encode(Object value, MappedField optionalExtraInfo) {
		if (value==null) 
			return null;
		else {
			return ((Access) value).ordinal();
		}
	}

	@Override
	public Object decode(Class targetClass, Object fromDBObject,
			MappedField optionalExtraInfo) {
		if (fromDBObject == null) 
            return null;
		else {
			return Access.values()[(int)fromDBObject];
		}
	}
}
