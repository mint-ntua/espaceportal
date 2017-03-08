package model.annotations;

import org.bson.types.ObjectId;

import utils.Serializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class ContextData<T1 extends ContextData.ContextDataBody> {

	public static enum ContextDataType {
		ExhibitionData, None
	}

	protected ContextDataType contextDataType;
	protected ContextDataTarget target;
	protected T1 body;

	public static class ContextDataTarget {
		@JsonSerialize(using = Serializer.ObjectIdSerializer.class)
		ObjectId recordId;

		public ObjectId getRecordId() {
			return recordId;
		}

		public void setRecordId(ObjectId recordId) {
			this.recordId = recordId;
		}
	}

	public static class ContextDataBody {
	}

	public ContextData() {
	}

	public ContextData(ObjectId recordId) {
		this.target = new ContextDataTarget();
		this.getTarget().setRecordId(recordId);
		this.contextDataType = ContextDataType.None;
	}

	public ContextData(ObjectId recordId, ContextDataType contextDataType) {
		this.target = new ContextDataTarget();
		this.getTarget().setRecordId(recordId);
		this.contextDataType = contextDataType;
	}

	public ContextData(ObjectId recordId, ContextDataType contextDataType,
			T1 body) {
		this.target = new ContextDataTarget();
		this.getTarget().setRecordId(recordId);
		this.contextDataType = contextDataType;
		this.body = body;
	}

	public T1 getBody() {
		return body;
	}

	public void setBody(T1 body) {
		this.body = body;
	}

	public ContextDataType getContextDataType() {
		return contextDataType;
	}

	public void setContextDataType(ContextDataType contextDataType) {
		this.contextDataType = contextDataType;
	}

	public ContextDataTarget getTarget() {
		return target;
	}

	public void setTarget(ContextDataTarget target) {
		this.target = target;
	}

}
