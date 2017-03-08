package model.basicDataTypes;

import java.util.Date;


/**
 * 
 * The WithTime might already cover the timespan you mean, but if you need more fields, its meant to be the 
 * start of the timespan.
 */
public class WithPeriod extends WithDate  {
	Date isoEndDate;
	int endYear;
}
