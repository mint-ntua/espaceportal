package model.basicDataTypes;

import java.util.ArrayList;
public class CidocEvent {
	public static enum EventType {
		CREATED, OTHER 
	}
	
	EventType eventType;
	WithPeriod timespan;
	MultiLiteralOrResource agent;
	MultiLiteralOrResource place;
}
