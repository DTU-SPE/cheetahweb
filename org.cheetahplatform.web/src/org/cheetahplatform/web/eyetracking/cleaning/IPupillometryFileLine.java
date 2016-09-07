package org.cheetahplatform.web.eyetracking.cleaning;

public interface IPupillometryFileLine {

	String get(int columnNumber);

	String get(PupillometryFileColumn column);

	String getString(String separator);

	int size();
}