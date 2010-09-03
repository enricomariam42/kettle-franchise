/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package be.kjube.plugins.datetime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

@Step(
		name = "DateTime", 
		image = "be/kjube/plugins/images/datetime.png", 
		tooltip = "kJube.Plugins.DateTime.Description", 
		description = "kJube.Plugins.DateTime.Name", 
		category = StepCategory.CATEGORY_USER_DEFINED, 
		categoryDescription = "kJube.Category.Name", 
		i18nPackageName = "be.kjube.plugins"
	)
public class DateTimeMeta extends BaseStepMeta implements StepMetaInterface {
	public enum ColumnType {

		CENTURIES(Messages.getString("DateTime.ColumnType.Centuries")), // $NON-NLS-1$
		YEARS(Messages.getString("DateTime.ColumnType.Years")), // $NON-NLS-1$
		MONTHS(Messages.getString("DateTime.ColumnType.Months")), // $NON-NLS-1$
		DAYS(Messages.getString("DateTime.ColumnType.Days")), // $NON-NLS-1$
		HOURS(Messages.getString("DateTime.ColumnType.Hours")), // $NON-NLS-1$
		MINUTES(Messages.getString("DateTime.ColumnType.Minutes")), // $NON-NLS-1$
		SECONDS(Messages.getString("DateTime.ColumnType.Seconds")), // $NON-NLS-1$
		;

		private String	description;

		public String getDescription() {
			return description;
		}

		private ColumnType(String description) {
			this.description = description;
		}

		public static ColumnType getColumnTypeByDescription(String text) {
			for (ColumnType trimType : values()) {
				if (trimType.description.equalsIgnoreCase(text)) {
					return trimType;
				}
			}
			return null;
		}

		public static String[] getColumnTypeDescriptions() {
			String[] descriptions = new String[values().length];
			for (int i = 0; i < values().length; i++) {
				descriptions[i] = values()[i].getDescription();
			}
			return descriptions;
		}
	};

	private List<DateTimeField>	fields;
	
	private boolean ignoringErrors;

	public DateTimeMeta() {
		super(); // allocate BaseStepMeta
	}

	public Object clone() {
		DateTimeMeta retval = (DateTimeMeta) super.clone();
		retval.fields = new ArrayList<DateTimeField>();
		retval.fields.addAll(fields);
		return retval;
	}

	public void allocate() {
		fields = new ArrayList<DateTimeField>();
	}

	@Override
	public void getFields(RowMetaInterface inputRowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

		for (DateTimeField field : fields) {
			// Remove the used fields...
			//
			for (String fieldName : field.getFieldnames()) {
				int index = inputRowMeta.indexOfValue(fieldName);
				if (index >= 0) {
					inputRowMeta.removeValueMeta(index);
				}
			}
			// Add a new Date field..
			//
			ValueMetaInterface dateField = new ValueMeta(field.getResultField(), ValueMetaInterface.TYPE_DATE);
			if (Const.isEmpty(field.getHoursField()) && Const.isEmpty(field.getMinutesField()) && Const.isEmpty(field.getSecondsField())) {
				dateField.setConversionMask("yyyy/MM/dd");
			} else {
				dateField.setConversionMask("yyyy/MM/dd HH:mm:ss");
			}
			inputRowMeta.addValueMeta(dateField);
		}
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append("    ").append(XMLHandler.addTagValue("ignore_errors", ignoringErrors));

		retval.append("    <fields>" + Const.CR);
		for (int i = 0; i < fields.size(); i++) {
			DateTimeField field = fields.get(i);
			retval.append("      <field>" + Const.CR);
			retval.append("        " + XMLHandler.addTagValue("result", field.getResultField()));
			retval.append("        " + XMLHandler.addTagValue("centuries", field.getCenturiesField()));
			retval.append("        " + XMLHandler.addTagValue("years", field.getYearsField()));
			retval.append("        " + XMLHandler.addTagValue("months", field.getMonthsField()));
			retval.append("        " + XMLHandler.addTagValue("days", field.getDaysField()));
			retval.append("        " + XMLHandler.addTagValue("hours", field.getHoursField()));
			retval.append("        " + XMLHandler.addTagValue("minutes", field.getMinutesField()));
			retval.append("        " + XMLHandler.addTagValue("seconds", field.getSecondsField()));
			retval.append("        </field>" + Const.CR);
		}
		retval.append("      </fields>" + Const.CR);

		return retval.toString();
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		try {
			
			ignoringErrors = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "ignore_errors"));
			
			Node fieldsNode = XMLHandler.getSubNode(stepnode, "fields");
			int nrfields = XMLHandler.countNodes(fieldsNode, "field");

			allocate();

			for (int i = 0; i < nrfields; i++) {
				Node fnode = XMLHandler.getSubNodeByNr(fieldsNode, "field", i);
				String resultField = XMLHandler.getTagValue(fnode, "result");
				String centuriesField = XMLHandler.getTagValue(fnode, "centuries");
				String yearsField = XMLHandler.getTagValue(fnode, "years");
				String monthsField = XMLHandler.getTagValue(fnode, "months");
				String daysField = XMLHandler.getTagValue(fnode, "days");
				String hoursField = XMLHandler.getTagValue(fnode, "hours");
				String minutesField = XMLHandler.getTagValue(fnode, "minutes");
				String secondsField = XMLHandler.getTagValue(fnode, "seconds");
				fields.add( new DateTimeField(resultField, centuriesField, yearsField, monthsField, daysField, hoursField, minutesField, secondsField) );
			}
		} catch (Exception e) {
			throw new KettleXMLException("It was not possibke to load the step metadata from XML", e);
		}
	}



	public void setDefault() {
		fields = new ArrayList<DateTimeField>();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {
			
			ignoringErrors = rep.getStepAttributeBoolean(id_step, "ignore_errors");
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_result");
			allocate();

			for (int i = 0; i < nrfields; i++) {
				String resultField = rep.getStepAttributeString(id_step, i, "field_result");
				String centuriesField = rep.getStepAttributeString(id_step, i, "field_centuries");
				String yearsField = rep.getStepAttributeString(id_step, i, "field_years");
				String monthsField = rep.getStepAttributeString(id_step, i, "field_months");
				String daysField = rep.getStepAttributeString(id_step, i, "field_days");
				String hoursField = rep.getStepAttributeString(id_step, i, "field_hours");
				String minutesField = rep.getStepAttributeString(id_step, i, "field_minutes");
				String secondsField = rep.getStepAttributeString(id_step, i, "field_seconds");
				fields.add( new DateTimeField(resultField, centuriesField, yearsField, monthsField, daysField, hoursField, minutesField, secondsField) );
			}
		} catch (Exception e) {
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException {
		try {
			
			rep.saveStepAttribute(id_transformation, id_step, "ignore_errors", ignoringErrors);
			
			for (int i = 0; i < fields.size(); i++) {
				DateTimeField field = fields.get(i);
				
				rep.saveStepAttribute(id_transformation, id_step, i, "field_result", field.getResultField());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_centuries", field.getCenturiesField());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_years", field.getYearsField());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_months", field.getMonthsField());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_days", field.getDaysField());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_hours", field.getHoursField());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_minutes", field.getMinutesField());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_seconds", field.getSecondsField());
			}
		} catch (Exception e) {
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {

	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans) {
		return new DateTime(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData() {
		return new DateTimeData();
	}

	public boolean supportsErrorHandling() {
		return !ignoringErrors;
	}

	/**
	 * @return the fields
	 */
	public List<DateTimeField> getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(List<DateTimeField> fields) {
		this.fields = fields;
	}

	/**
	 * @return the ignoringErrors
	 */
	public boolean isIgnoringErrors() {
		return ignoringErrors;
	}

	/**
	 * @param ignoringErrors the ignoringErrors to set
	 */
	public void setIgnoringErrors(boolean ignoringErrors) {
		this.ignoringErrors = ignoringErrors;
	}

}
