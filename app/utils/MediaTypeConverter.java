package utils;

import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import com.google.common.net.MediaType;

public class MediaTypeConverter extends TypeConverter implements SimpleValueConverter {


		public MediaTypeConverter() {
			super(MediaType.class);
		}

		@Override
		public Object encode(Object value, MappedField optionalExtraInfo) {
			if(value==null) {
				return null;
			}

			return ((MediaType)value).toString();
		}

		@Override
		public Object decode(Class targetClass, Object fromDBObject,
				MappedField optionalExtraInfo) {
			if (fromDBObject == null) {
	            return null;
	        }
	        return MediaType.parse((String)fromDBObject);
		}
}
