package model.basicDataTypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import play.Logger;
import play.Logger.ALogger;

/**
 * Capture accurate and inaccurate dates in a visualisable way. Enable search
 * for year. This is a point in time. If you mean a timespan, use different
 * class.
 */
public class WithDate {
	public static final ALogger log = Logger.of( WithDate.class );
	
	Date isoDate;
	// facet
	// year should be filled in whenever possible
	// 100 bc is translated to -100
	int year;

	// controlled expression of an epoch "stone age", "renaissance", "16th
	// century"
	LiteralOrResource epoch;

	// if the year is not accurate, give the inaccuracy here( 0- accurate)
	int approximation;

	// ontology based time
	String uri;
	// ResourceType uriType;

	// mandatory, other fields are extracted from that
	String free;

	public WithDate() {
		super();
	}

	public Date getIsoDate() {
		return isoDate;
	}

	public void setIsoDate(Date d) {
		if (d != null) {
			this.isoDate = d;
			Calendar instance = Calendar.getInstance();
			instance.setTime(d);
			year = instance.get(Calendar.YEAR);
		}
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public LiteralOrResource getEpoch() {
		return epoch;
	}

	public void setEpoch(LiteralOrResource epoch) {
		this.epoch = epoch;
	}

	public int getApproximation() {
		return approximation;
	}

	public void setApproximation(int approximation) {
		this.approximation = approximation;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	//
	// public ResourceType getUriType() {
	// return uriType;
	// }
	//
	// public void setUriType(ResourceType uriType) {
	// this.uriType = uriType;
	// }

	public String getFree() {
		return free;
	}

	public void setFree(String free) {
		this.free = free;
	}

	public WithDate(String free) {
		super();
		setDate(free);
	}

	public void setDate(String free) {
		this.free = free;
		// code to init the other Date representations
		if (sources.core.Utils.isNumericInteger(free)) {
			this.year = Integer.parseInt(free);
		} else if (free.matches("\\d+\\s+(bc|BC)")) {
			this.year = Integer.parseInt(free.split("\\s")[0]);
		} else if (free.matches("\\d\\d\\d\\d-\\d\\d\\d\\d")) {
			this.year = Integer.parseInt(free.split("-")[0]);
		} else if (free.matches("\\d\\d-\\d\\d-\\d\\d\\d\\d")) {
			try {
				setIsoDate((new SimpleDateFormat("dd-mm-yyyy")).parse(free));
			} catch (ParseException e) {
				log.warn("Parse Exception: " + free);
			}
		} else if (free.matches("\\d\\d\\d\\d-\\d\\d-\\d\\d")) {
			try {
				setIsoDate((new SimpleDateFormat("yyyy-mm-dd")).parse(free));
			} catch (ParseException e) {
				log.warn("Parse Exception: " + free);
			}
		} else if (sources.core.Utils.isValidURL(free)) {
			this.uri = free;
			// this.uriType = ResourceType.uri;
		} else {
			log.warn("unrecognized date: " + free);
		}
	}

}
