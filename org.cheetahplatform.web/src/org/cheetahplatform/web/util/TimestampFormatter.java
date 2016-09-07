package org.cheetahplatform.web.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimestampFormatter {
	public static void main(String[] args) {
		Date date = new Date(1429111108772l);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
		System.out.println(calendar.get(Calendar.MINUTE));
		System.out.println(calendar.get(Calendar.SECOND));
		System.out.println(calendar.get(Calendar.MILLISECOND));
	}
}
