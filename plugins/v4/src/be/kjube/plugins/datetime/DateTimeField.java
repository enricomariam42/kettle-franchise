package be.kjube.plugins.datetime;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;

public class DateTimeField {
	
	private String resultField;

	private String	centuriesField;
	private String	yearsField;
	private String	monthsField;
	private String	daysField;
	private String	hoursField;
	private String	minutesField;
	private String	secondsField;

	
	/**
	 * @param resultField
	 * @param centuriesField
	 * @param yearsField
	 * @param monthsField
	 * @param daysField
	 * @param hoursField
	 * @param minutesField
	 * @param secondsField
	 */
	public DateTimeField(String resultField, String centuriesField, String yearsField, String monthsField, String daysField, String hoursField, String minutesField, String secondsField) {
		this.resultField = resultField;
		this.centuriesField = centuriesField;
		this.yearsField = yearsField;
		this.monthsField = monthsField;
		this.daysField = daysField;
		this.hoursField = hoursField;
		this.minutesField = minutesField;
		this.secondsField = secondsField;
	}
	
	public List<String> getFieldnames() {
		List<String> names = new ArrayList<String>();
		if (!Const.isEmpty(resultField)) names.add(resultField);
		if (!Const.isEmpty(centuriesField)) names.add(centuriesField);
		if (!Const.isEmpty(yearsField)) names.add(yearsField);
		if (!Const.isEmpty(monthsField)) names.add(monthsField);
		if (!Const.isEmpty(daysField)) names.add(daysField);
		if (!Const.isEmpty(hoursField)) names.add(hoursField);
		if (!Const.isEmpty(minutesField)) names.add(minutesField);
		if (!Const.isEmpty(secondsField)) names.add(secondsField);
		return names;
	}

	/**
	 * @return the centuriesField
	 */
	public String getCenturiesField() {
		return centuriesField;
	}

	/**
	 * @param centuriesField
	 *            the centuriesField to set
	 */
	public void setCenturiesField(String centuriesField) {
		this.centuriesField = centuriesField;
	}

	/**
	 * @return the yearsField
	 */
	public String getYearsField() {
		return yearsField;
	}

	/**
	 * @param yearsField
	 *            the yearsField to set
	 */
	public void setYearsField(String yearsField) {
		this.yearsField = yearsField;
	}

	/**
	 * @return the monthsField
	 */
	public String getMonthsField() {
		return monthsField;
	}

	/**
	 * @param monthsField
	 *            the monthsField to set
	 */
	public void setMonthsField(String monthsField) {
		this.monthsField = monthsField;
	}

	/**
	 * @return the daysField
	 */
	public String getDaysField() {
		return daysField;
	}

	/**
	 * @param daysField
	 *            the daysField to set
	 */
	public void setDaysField(String daysField) {
		this.daysField = daysField;
	}

	/**
	 * @return the hoursField
	 */
	public String getHoursField() {
		return hoursField;
	}

	/**
	 * @param hoursField
	 *            the hoursField to set
	 */
	public void setHoursField(String hoursField) {
		this.hoursField = hoursField;
	}

	/**
	 * @return the minutesField
	 */
	public String getMinutesField() {
		return minutesField;
	}

	/**
	 * @param minutesField
	 *            the minutesField to set
	 */
	public void setMinutesField(String minutesField) {
		this.minutesField = minutesField;
	}

	/**
	 * @return the secondsField
	 */
	public String getSecondsField() {
		return secondsField;
	}

	/**
	 * @param secondsField
	 *            the secondsField to set
	 */
	public void setSecondsField(String secondsField) {
		this.secondsField = secondsField;
	}

	/**
	 * @return the resultField
	 */
	public String getResultField() {
		return resultField;
	}

	/**
	 * @param resultField the resultField to set
	 */
	public void setResultField(String resultField) {
		this.resultField = resultField;
	}

}
