package be.kjube.plugins.season;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;

public class SeasonField {
	
	private String resultField;

	private String	centuriesField;
	private String	yearsField;
	private String	seasonNumberField;

	
	/**
	 * @param resultField
	 * @param centuriesField
	 * @param yearsField
	 * @param seasonNumberField
	 */
	public SeasonField(String resultField, String centuriesField, String yearsField, String seasonNumberField) {
		this.resultField = resultField;
		this.centuriesField = centuriesField;
		this.yearsField = yearsField;
		this.seasonNumberField = seasonNumberField;
	}
	
	public List<String> getFieldnames() {
		List<String> names = new ArrayList<String>();
		if (!Const.isEmpty(resultField)) names.add(resultField);
		if (!Const.isEmpty(centuriesField)) names.add(centuriesField);
		if (!Const.isEmpty(yearsField)) names.add(yearsField);
		if (!Const.isEmpty(seasonNumberField)) names.add(seasonNumberField);
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

	/**
	 * @return the seasonNumberField
	 */
	public String getSeasonNumberField() {
		return seasonNumberField;
	}

	/**
	 * @param seasonNumberField the seasonNumberField to set
	 */
	public void setSeasonNumberField(String seasonNumberField) {
		this.seasonNumberField = seasonNumberField;
	}

}
