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
package be.kjube.plugins.rejects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import be.kjube.util.Kjube;

@Step(
		id = "Rejects", 
		image = "be/kjube/plugins/images/rejects.png", 
		description = "kJube.Plugins.Rejects.Description", 
		name = "kJube.Plugins.Rejects.Name", 
		categoryDescription = "kJube.Category.Name", 
		i18nPackageName = "be.kjube.plugins"
	)
public class RejectsMeta extends BaseStepMeta implements StepMetaInterface {
	private static Class<?> PKG = Rejects.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DatabaseMeta	databaseMeta;
	private String			databaseName;

	private String			rejectsSchemaName;
	private String			rejectsTableName;
	private List<String>	keyFields;
	
	private String          errorCountField;
	private String          errorDescriptionsField;
	private String          errorFieldsField;
	private String          errorCodesField;

	public RejectsMeta() {
		super(); // allocate BaseStepMeta

		keyFields = new ArrayList<String>();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode, databases);
	}

	public Object clone() {
		RejectsMeta retval = (RejectsMeta) super.clone();

		return retval;
	}
	
	@Override
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
		
		inputRowMeta.clear();
		
		// The ID
		//
		ValueMetaInterface id = new ValueMeta("id", ValueMetaInterface.TYPE_STRING);
		id.setLength(2000);
		inputRowMeta.addValueMeta(id);
		
		// The row serialized as a String
		//
		ValueMetaInterface value = new ValueMeta("value", ValueMetaInterface.TYPE_STRING);
		value.setLength(DatabaseMeta.CLOB_LENGTH);
		inputRowMeta.addValueMeta(value);

		// The Batch ID
		//
		ValueMetaInterface batchId= new ValueMeta("batch_id", ValueMetaInterface.TYPE_INTEGER);
		batchId.setLength(9);
		inputRowMeta.addValueMeta(batchId);
		
		// The transformation name
		//
		ValueMetaInterface transName = new ValueMeta("transname", ValueMetaInterface.TYPE_STRING);
		transName.setLength(255);
		inputRowMeta.addValueMeta(transName);

		// The parent job name
		//
		ValueMetaInterface jobName = new ValueMeta("jobname", ValueMetaInterface.TYPE_STRING);
		jobName.setLength(255);
		inputRowMeta.addValueMeta(jobName);

		// The rejecting step
		//
		ValueMetaInterface stepName = new ValueMeta("stepname", ValueMetaInterface.TYPE_STRING);
		stepName.setLength(255);
		inputRowMeta.addValueMeta(stepName);

		// Error count
		//
		ValueMetaInterface errorCount= new ValueMeta("error_count", ValueMetaInterface.TYPE_INTEGER);
		errorCount.setLength(9);
		inputRowMeta.addValueMeta(errorCount);

		// Error Descriptions
		//
		ValueMetaInterface errorDescriptions = new ValueMeta("error_descriptions", ValueMetaInterface.TYPE_STRING);
		errorDescriptions.setLength(DatabaseMeta.CLOB_LENGTH);
		inputRowMeta.addValueMeta(errorDescriptions);

		// Error Fields
		//
		ValueMetaInterface errorFields = new ValueMeta("error_fields", ValueMetaInterface.TYPE_STRING);
		errorFields.setLength(255);
		inputRowMeta.addValueMeta(errorFields);

		// Error codes
		//
		ValueMetaInterface errorCodes = new ValueMeta("error_codes", ValueMetaInterface.TYPE_STRING);
		errorCodes.setLength(255);
		inputRowMeta.addValueMeta(errorCodes);

		// A logging date
		//
		ValueMetaInterface logDate = new ValueMeta("logdate", ValueMetaInterface.TYPE_DATE);
		inputRowMeta.addValueMeta(logDate);
	}

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException {
		try {
			databaseMeta = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(stepnode, "database"));
			databaseName = XMLHandler.getTagValue(stepnode, "database_name");
			rejectsSchemaName = XMLHandler.getTagValue(stepnode, "rejects_schema_name");
			rejectsTableName = XMLHandler.getTagValue(stepnode, "rejects_table_name");

			errorCountField = XMLHandler.getTagValue(stepnode, "error_count_field");
			errorDescriptionsField = XMLHandler.getTagValue(stepnode, "error_descriptions_field");
			errorFieldsField = XMLHandler.getTagValue(stepnode, "error_fields_field");
			errorCodesField = XMLHandler.getTagValue(stepnode, "error_codes_field");

			Node fields = XMLHandler.getSubNode(stepnode, "keys");
			int nrfields = XMLHandler.countNodes(fields, "key");

			keyFields.clear();

			for (int i = 0; i < nrfields; i++) {
				Node keyNode = XMLHandler.getSubNodeByNr(fields, "key", i);
				keyFields.add(XMLHandler.getNodeValue(keyNode));
			}
		} catch (Exception e) {
			throw new KettleXMLException("It was not possibke to load the step metadata from XML", e);
		}
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append("      ").append(XMLHandler.addTagValue("database", databaseMeta != null ? databaseMeta.getName() : ""));
		retval.append("      ").append(XMLHandler.addTagValue("database_name", databaseName));
		retval.append("      ").append(XMLHandler.addTagValue("rejects_schema_name", rejectsSchemaName));
		retval.append("      ").append(XMLHandler.addTagValue("rejects_table_name", rejectsTableName));

		retval.append("      ").append(XMLHandler.addTagValue("error_count_field", errorCountField));
		retval.append("      ").append(XMLHandler.addTagValue("error_descriptions_field", errorDescriptionsField));
		retval.append("      ").append(XMLHandler.addTagValue("error_fields_field", errorFieldsField));
		retval.append("      ").append(XMLHandler.addTagValue("error_codes_field", errorCodesField));

		retval.append("      ").append(XMLHandler.openTag("keys"));
		for (String keyField : keyFields) {
			retval.append("      ").append(XMLHandler.addTagValue("key", keyField));
		}
		retval.append("      ").append(XMLHandler.closeTag("keys"));

		return retval.toString();
	}

	public void setDefault() {
		databaseMeta = null;
		databaseName = Kjube.DEFAULT_BATCH_LOGGING_CONNECTION;
		rejectsSchemaName = Kjube.DEFAULT_REJECTS_SCHEMA;
		rejectsTableName = Kjube.DEFAULT_REJECTS_TABLE;

		errorCountField = Kjube.DEFAULT_ERROR_COUNT_VARIABLE_NAME;
		errorDescriptionsField = Kjube.DEFAULT_ERROR_DESCRIPTIONS_VARIABLE_NAME;
		errorFieldsField = Kjube.DEFAULT_ERROR_FIELDS_VARIABLE_NAME;
		errorCodesField = Kjube.DEFAULT_ERROR_CODES_VARIABLE_NAME;
			
	}

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {
			databaseMeta = rep.loadDatabaseMetaFromJobEntryAttribute(id_step, "database_name", "database", databases);
			databaseName = rep.getStepAttributeString(id_step, "database_name");
			rejectsSchemaName = rep.getStepAttributeString(id_step, "rejects_schema_name");
			rejectsTableName = rep.getStepAttributeString(id_step, "rejects_table_name");

			errorCountField = rep.getStepAttributeString(id_step, "error_count_field");
			errorDescriptionsField = rep.getStepAttributeString(id_step, "error_descriptions_field");
			errorFieldsField = rep.getStepAttributeString(id_step, "error_fields_field");
			errorCodesField = rep.getStepAttributeString(id_step, "error_codes_field");

			int nrKeys = rep.countNrStepAttributes(id_step, "key");
			keyFields.clear();
			for (int i = 0; i < nrKeys; i++) {
				keyFields.add(rep.getStepAttributeString(id_step, i, "key"));
			}
		} catch (Exception e) {
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "database", databaseMeta != null ? databaseMeta.getName() : "");
			rep.saveStepAttribute(id_transformation, id_step, "database_name", databaseName);
			rep.saveStepAttribute(id_transformation, id_step, "rejects_schema_name", rejectsSchemaName);
			rep.saveStepAttribute(id_transformation, id_step, "rejects_table_name", rejectsTableName);

			rep.saveStepAttribute(id_transformation, id_step, "error_count_field", errorCountField);
			rep.saveStepAttribute(id_transformation, id_step, "error_descriptions_field", errorDescriptionsField);
			rep.saveStepAttribute(id_transformation, id_step, "error_fields_field", errorFieldsField);
			rep.saveStepAttribute(id_transformation, id_step, "error_codes_field", errorCodesField);

			for (int i = 0; i < keyFields.size(); i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "key", keyFields.get(i));
			}
		} catch (Exception e) {
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		// See if we have input streams leading to this step!
		CheckResult cr;

		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "RejectsMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "RejectsMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	@Override
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev) throws KettleStepException {
		
		DatabaseMeta dbMeta = databaseMeta;
		if (dbMeta==null) {
			dbMeta = transMeta.findDatabase( transMeta.environmentSubstitute(databaseName) );
		}
		if (dbMeta!=null) {
			Database db = new Database(null, dbMeta);
			try {
				db.connect();
				
				RowMetaInterface rowMeta = new RowMeta();
				getFields(rowMeta, stepMeta.getName(), null, null, transMeta);
				
				String sql = db.getDDL(dbMeta.getQuotedSchemaTableCombination(rejectsSchemaName, rejectsTableName), rowMeta);
				if (!Const.isEmpty(sql)) {
					return new SQLStatement(stepMeta.getName(), databaseMeta, sql);
				} else {
					return super.getSQLStatements(transMeta, stepMeta, prev);
				}
			} catch(Exception e) {
				throw new KettleStepException("Unable to create SQL statement", e);
			} finally {
				db.disconnect();
			}
		} else {
			return super.getSQLStatements(transMeta, stepMeta, prev);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans) {
		return new Rejects(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData() {
		return new RejectsData();
	}

	public boolean supportsErrorHandling() {
		return true;
	}

	/**
	 * @return the databaseMeta
	 */
	public DatabaseMeta getDatabaseMeta() {
		return databaseMeta;
	}

	/**
	 * @param databaseMeta
	 *            the databaseMeta to set
	 */
	public void setDatabaseMeta(DatabaseMeta databaseMeta) {
		this.databaseMeta = databaseMeta;
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @param databaseName
	 *            the databaseName to set
	 */
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * @return the rejectsSchemaName
	 */
	public String getRejectsSchemaName() {
		return rejectsSchemaName;
	}

	/**
	 * @param rejectsSchemaName
	 *            the rejectsSchemaName to set
	 */
	public void setRejectsSchemaName(String rejectsSchemaName) {
		this.rejectsSchemaName = rejectsSchemaName;
	}

	/**
	 * @return the rejectsTableName
	 */
	public String getRejectsTableName() {
		return rejectsTableName;
	}

	/**
	 * @param rejectsTableName
	 *            the rejectsTableName to set
	 */
	public void setRejectsTableName(String rejectsTableName) {
		this.rejectsTableName = rejectsTableName;
	}

	/**
	 * @return the keyFields
	 */
	public List<String> getKeyFields() {
		return keyFields;
	}

	/**
	 * @param keyFields
	 *            the keyFields to set
	 */
	public void setKeyFields(List<String> keyFields) {
		this.keyFields = keyFields;
	}

	/**
	 * @return the errorCountField
	 */
	public String getErrorCountField() {
		return errorCountField;
	}

	/**
	 * @param errorCountField the errorCountField to set
	 */
	public void setErrorCountField(String errorCountField) {
		this.errorCountField = errorCountField;
	}

	/**
	 * @return the errorDescriptionsField
	 */
	public String getErrorDescriptionsField() {
		return errorDescriptionsField;
	}

	/**
	 * @param errorDescriptionsField the errorDescriptionsField to set
	 */
	public void setErrorDescriptionsField(String errorDescriptionsField) {
		this.errorDescriptionsField = errorDescriptionsField;
	}

	/**
	 * @return the errorFieldsField
	 */
	public String getErrorFieldsField() {
		return errorFieldsField;
	}

	/**
	 * @param errorFieldsField the errorFieldsField to set
	 */
	public void setErrorFieldsField(String errorFieldsField) {
		this.errorFieldsField = errorFieldsField;
	}

	/**
	 * @return the errorCodesField
	 */
	public String getErrorCodesField() {
		return errorCodesField;
	}

	/**
	 * @param errorCodesField the errorCodesField to set
	 */
	public void setErrorCodesField(String errorCodesField) {
		this.errorCodesField = errorCodesField;
	}

}
