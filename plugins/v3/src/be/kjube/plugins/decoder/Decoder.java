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

package be.kjube.plugins.decoder;

import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.streamlookup.Messages;

import be.kjube.util.Kjube;

/**
 * Sets a field value to a constant if it is null
 * 
 * @author Matt
 * @since 19-11-2009
 */

public class Decoder extends BaseStep implements StepInterface {
	private DecoderMeta	meta;
	private DecoderData	data;
	private int	position;
	private Object	lastDateString;

	public Decoder(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(DecoderMeta)getStepMeta().getStepMetaInterface();
		data=(DecoderData)stepDataInterface;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (DecoderMeta) smi;
		data = (DecoderData) sdi;

		if (first) {
			readSourceData();
		}
		
		Object[] r = getRow(); // get row, set busy!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;

			// What's the format of the output row?
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			data.dataTableIdFieldIndex = getInputRowMeta().indexOfValue(meta.getDataTableIdField());
			if (data.dataTableIdFieldIndex<0) {
				throw new KettleException("Unable to find field '"+meta.getDataTableIdField()+"' in the main data input stream");
			}
			
			data.dataKeyFieldIndex = getInputRowMeta().indexOfValue(meta.getDataKeyField());
			if (data.dataKeyFieldIndex<0) {
				throw new KettleException("Unable to find field '"+meta.getDataKeyField()+"' in the main data input stream: "+getInputRowMeta().toString());
			}

			data.dataDataFieldIndex = getInputRowMeta().indexOfValue(meta.getDataDataField());
			if (data.dataDataFieldIndex<0) {
				throw new KettleException("Unable to find field '"+meta.getDataDataField()+"' in the main data input stream");
			}
			
			data.dataLastUpdateFieldIndex = getInputRowMeta().indexOfValue("DTLASTUPDATE");
			if (data.dataLastUpdateFieldIndex<0) {
				throw new KettleException("Unable to find field '"+"DTLASTUPDATE"+"' in the main data input stream");
			}

			data.dataTimestampFieldIndex = getInputRowMeta().indexOfValue("TIMESTAMP");
			if (data.dataTimestampFieldIndex<0) {
				throw new KettleException("Unable to find field '"+"TIMESTAMP"+"' in the main data input stream");
			}

			data.dataLanguageFieldIndex = getInputRowMeta().indexOfValue("IDLANGUAGE");
			if (data.dataLanguageFieldIndex<0) {
				throw new KettleException("Unable to find field '"+"IDLANGUAGE"+"' in the main data input stream");
			}

			data.dataCompanyFieldIndex = getInputRowMeta().indexOfValue("IDCOMPANY");
			if (data.dataCompanyFieldIndex<0) {
				throw new KettleException("Unable to find field '"+"IDCOMPANY"+"' in the main data input stream");
			}
		} // end if first

		try {
			decodeRow(getInputRowMeta(), r);
			
			// Just pass the data along!
			//
			putRow(getInputRowMeta(), r);

		} catch (Exception e) {
			boolean sendToErrorRow = false;
			String errorMessage = null;

			if (getStepMeta().isDoingErrorHandling()) {
				sendToErrorRow = true;
				errorMessage = e.toString();
			} else {
				logError(Messages.getString("Decoder.Log.ErrorInStep", e.getMessage())); //$NON-NLS-1$
				e.printStackTrace();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow) {
				// Simply add this row to the error row
				putError(data.outputRowMeta, r, 1, errorMessage, null, "DECODER001");
			}
		}
		return true;
	}

	private void decodeRow(RowMetaInterface inputRowMeta, Object[] r) throws KettleException {

		String tableId = Kjube.trim(inputRowMeta.getString(r, data.dataTableIdFieldIndex));
		
		String keyString = inputRowMeta.getString(r, data.dataKeyFieldIndex);
		String code = inputRowMeta.getString(r, data.dataDataFieldIndex);
		String languageId = inputRowMeta.getString(r, data.dataLanguageFieldIndex);
		Long   companyId = inputRowMeta.getInteger(r, data.dataCompanyFieldIndex);
		
		Date dtLastUpdate = inputRowMeta.getDate(r, data.dataLastUpdateFieldIndex);
		Date timestamp = inputRowMeta.getDate(r, data.dataTimestampFieldIndex);
		
		// Now we need to split up this "code" field into the various fields that are defined in the decoderTable object for this table.
		//
		DecoderTable decoderTable = data.decoderTables.get(tableId);
		if (decoderTable==null) {
			throw new KettleException("Unable to find decoding information for table with ID: "+tableId);
		}

		// Get the real target table name...
		//
		String tableName = data.tableNameMap.get(tableId);
		if (tableName==null) {
			tableName = tableId;
		} else {
			logRowlevel("Table '"+tableId+"' mapped to '"+tableName+"'");
		}
		
		// Now assemble the row of data to process...
		//
		RowMetaInterface rowMeta = new RowMeta();
		List<Object> rowData = new ArrayList<Object>();

		position = 0;
		lastDateString = null;
		
		// Add the keys
		//
		for (DecoderField keyDecoderField : decoderTable.getKeys()) {
			parseField(rowMeta, rowData, tableId, keyDecoderField, keyString, keyString);
		}

		// Add the company and language Ids...
		//
		ValueMetaInterface companyIdMeta = new ValueMeta("IDCOMPANY", ValueMetaInterface.TYPE_INTEGER);
		companyIdMeta.setLength(3);
		rowMeta.addValueMeta(companyIdMeta);
		rowData.add(companyId);
		
		ValueMetaInterface languageIdMeta = new ValueMeta("IDLANGUAGE", ValueMetaInterface.TYPE_STRING);
		languageIdMeta.setLength(2);
		rowMeta.addValueMeta(languageIdMeta);
		rowData.add(languageId);
		
		position = 0;
		lastDateString = null;

		// Now handle the standard & appendix elements in one go...
		//
		
		for (DecoderField field : decoderTable.getElements()) {
			parseField(rowMeta, rowData, tableId, field, code, keyString);
		}
		/*
		for (DecoderField field : decoderTable.getAppendix()) {
			parseField(rowMeta, rowData, tableId, field, code, keyString);
		}
		*/
		
		// At the end, add the dtLastUpdate and time stamp fields...
		//
		rowMeta.addValueMeta( new ValueMeta("DTLASTUPDATE", ValueMetaInterface.TYPE_DATE) );
		rowData.add(dtLastUpdate);
		
		rowMeta.addValueMeta( new ValueMeta("TIMESTAMP", ValueMetaInterface.TYPE_DATE) );
		rowData.add(timestamp);
		
		// Handle the record...
		// rowMeta.toString(rowData)
		handleRecord(decoderTable, tableId, tableName, rowMeta, rowData);
	}

	private void handleRecord(DecoderTable decoderTable, String tableId, String tableName, RowMetaInterface rowMeta, List<Object> rowData) throws KettleException {
		if (log.isDetailed()) {
			logDetailed(tableName+" : "+rowMeta.getString(rowData.toArray(new Object[rowData.size()])));
		}

		String schemaTable = data.databaseMeta.getQuotedSchemaTableCombination(meta.getSchema(), tableName);
		
		// We're connected to the target db.
		// 
		// First : look up the record in the database
		// If it doesn't exist: insert
		// If it does exist: update
		//
		// Quick & dirty, nothing fancy.
		//
		TableStatements tableStatements = data.statementsMap.get(tableId);
		if (tableStatements==null) {
			
			tableStatements = new TableStatements(decoderTable);
			data.statementsMap.put(tableId, tableStatements);
		
			if (meta.isModifyingTables()) {
				// Create the tables?
				//
				String sql = "";
				try {
					sql = data.db.getDDL(schemaTable, rowMeta, null, false, null, true);
					if (!Const.isEmpty(sql)) {
	
						data.db.execStatements(sql);
						data.db.commit();
						
						logDetailed("Table "+schemaTable+" created.");
						
						// Create a unique index over the ID fields...
						//
						String indexSql = "CREATE UNIQUE INDEX "+data.databaseMeta.getQuotedSchemaTableCombination(meta.getSchema(), "IDX_"+tableName);
						indexSql+=" ON "+schemaTable+"(";
						boolean first=true;
						int index=0;
						for (int i=0;i<decoderTable.getKeys().size()+2;i++) {
							boolean include = true;
							if (i<decoderTable.getKeys().size() && Const.isEmpty( decoderTable.getKeys().get(i).getFieldName()) ) {
								include=false;
							}
							if (include) {
								if (first) { 
									first=false; 
								} else { 
									indexSql+=", "; 
								}
								ValueMetaInterface valueMeta = rowMeta.getValueMeta(index++);
								indexSql+=" "+data.databaseMeta.quoteField(valueMeta.getName());
							}
						}
						indexSql+=")"+Const.CR;
						indexSql+=";";
						try {
							data.db.execStatements(indexSql);
							logDetailed("Table "+schemaTable+" created.");
						} catch(Exception e) {
							throw new KettleException("Creation of index on table "+schemaTable+" failed: "+indexSql, e);
						}
					}
				} catch(Exception e) {
					throw new KettleException("Create table of "+schemaTable+" failed: "+sql, e);
				}
			}
			
			String lookupSql=null;
			try {
				// Lookup...
				//
				lookupSql = "SELECT 1 FROM "+schemaTable+" WHERE ";
				
				RowMetaInterface lookupRowMeta = new RowMeta();
				boolean first=true;
				int index = 0;
				for (int i=0;i<decoderTable.getKeys().size()+2;i++) { // 2 extra: language and company
					boolean include = true;
					if (i<decoderTable.getKeys().size() && Const.isEmpty( decoderTable.getKeys().get(i).getFieldName()) ) {
						include=false;
					}
					if (include) {
						if (first) { 
							first=false; 
						} else { 
							lookupSql+=" AND "; 
						}
						tableStatements.getLookupIndexes().add(index);
						ValueMetaInterface valueMeta= rowMeta.getValueMeta(index++);
						lookupSql+=data.databaseMeta.quoteField(valueMeta.getName())+" = ?";
						lookupRowMeta.addValueMeta(valueMeta);
					}
				}
				
				PreparedStatement lookupStatement = data.db.prepareSQL(lookupSql);
				tableStatements.setLookupRowMeta(lookupRowMeta);
				tableStatements.setLookupStatement(lookupStatement);
				tableStatements.setLookupSql(lookupSql);
			} catch(Exception e) {
				throw new KettleException("Unable to prepare lookup statement: "+Const.CR+lookupSql, e);
			}
			
			String insertSql = null;
			try {
				// Insert...
				//
				insertSql = data.db.getInsertStatement(meta.getSchema(), tableName, rowMeta);
				PreparedStatement insertStatement = data.db.prepareSQL(insertSql);
				tableStatements.setInsertStatement(insertStatement);
				tableStatements.setInsertRowMeta(rowMeta);
				tableStatements.setInsertSql(insertSql);
			} catch(Exception e) {
				throw new KettleException("Unable to prepare insert statement: "+Const.CR+insertSql, e);
			}
			
			String updateSql = null;
			try {
				// Update...
				//
				RowMetaInterface updateRowMeta = new RowMeta();
				
				updateSql = "UPDATE "+schemaTable+" SET ";
				boolean first=true;
				for (int i=decoderTable.getKeys().size()+2;i<rowMeta.size();i++) {  // 2 extra: language and company
					boolean include = true;
					if (i<decoderTable.getKeys().size() && Const.isEmpty( decoderTable.getKeys().get(i).getFieldName()) ) {
						include=false;
					}
					if (include) {
						if (first) {
							first=false;
						} else {
							updateSql+=", ";
						}
						tableStatements.getUpdateIndexes().add(i);
						ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
						updateRowMeta.addValueMeta(valueMeta);
						updateSql+=data.databaseMeta.quoteField(valueMeta.getName())+" = ?";
					}
				}
				updateSql+=" WHERE ";
				first= true;
				int index=0;
				for (int i=0;i<decoderTable.getKeys().size()+2;i++) {  // 2 extra: language and company
					boolean include = true;
					if (i<decoderTable.getKeys().size() && Const.isEmpty( decoderTable.getKeys().get(i).getFieldName()) ) {
						include=false;
					}
					if (include) {
						if (first) {
							first=false;
						} else {
							updateSql+=" AND ";
						}
						tableStatements.getUpdateIndexes().add(index);
						ValueMetaInterface valueMeta = rowMeta.getValueMeta(index++);
						updateRowMeta.addValueMeta(valueMeta);
						updateSql+=data.databaseMeta.quoteField(valueMeta.getName())+" = ? ";
					}
				}
				PreparedStatement updateStatement = data.db.prepareSQL(updateSql);
				tableStatements.setUpdateRowMeta(updateRowMeta);
				tableStatements.setUpdateStatement(updateStatement);
				tableStatements.setUpdateSql(updateSql);
			} catch(Exception e) {
				throw new KettleException("Unable to prepare update statement: "+Const.CR+updateSql, e);
			}			
		}
		
		// Perform the lookup!
		//
		RowMetaInterface lookupRowMeta = tableStatements.getLookupRowMeta();
		Object[] lookupData = new Object[lookupRowMeta.size()];
		for (int i=0;i<lookupData.length;i++) {
			lookupData[i] = rowData.get(i);
		}
		
		Object[] result;
		try {
			data.db.setValues(tableStatements.getLookupRowMeta(), lookupData, tableStatements.getLookupStatement());
			result = data.db.getLookup(tableStatements.getLookupStatement());
			incrementLinesInput();
		} catch(Exception e) {
			e.printStackTrace();
			throw new KettleException("Unable to check key {"+lookupRowMeta.getString(lookupData)+"} in table '"+schemaTable+"'"+Const.CR+tableStatements.getLookupSql(), e);
		}
		if (result==null || result.length==0) {
			Object[] rowDataInsert = rowData.toArray(new Object[rowData.size()]);
			try {
				// Insert 
				//
				data.db.setValues(tableStatements.getInsertRowMeta(), rowDataInsert, tableStatements.getInsertStatement());
				data.db.insertRow(tableStatements.getInsertStatement());
				incrementLinesOutput();
			} catch(Exception e) {
				throw new KettleException("Error inserting row :"+tableStatements.getInsertRowMeta().getString(rowDataInsert), e);
			}
		} else {
			// Update
			//
			RowMetaInterface updateRowMeta = tableStatements.getUpdateRowMeta();
			Object[] updateRowData = new Object[updateRowMeta.size()];
			for (int i=0;i<tableStatements.getUpdateIndexes().size();i++) {
				int index = tableStatements.getUpdateIndexes().get(i);
				updateRowData[i] = rowData.get(index);
			}
			
			/*
			// update fields first, then the keys...
			//
			int index=0;
			for (int i=decoderTable.getKeys().size()+2;i<rowMeta.size();i++) {
				if (i>=decoderTable.getKeys().size() || !Const.isEmpty( decoderTable.getKeys().get(i).getFieldName()) ) {
					// ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
					updateRowData[index++] = rowData.get(index);
					index++;
				}
			}
			for (int i=0;i<decoderTable.getKeys().size()+2;i++) {
				if (i>=decoderTable.getKeys().size() || !Const.isEmpty( decoderTable.getKeys().get(i).getFieldName()) ) {
					// ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
					updateRowData[index++] = rowData.get(i);
				}
			}
			*/
			try {
				data.db.setValues(updateRowMeta, updateRowData, tableStatements.getUpdateStatement());
				data.db.insertRow(tableStatements.getUpdateStatement());
				incrementLinesUpdated();
			} catch(Exception e) {
				throw new KettleException("Error updating row :"+updateRowMeta.getString(updateRowData), e);
			}
		}
		
	}

	private void parseField(RowMetaInterface rowMeta, List<Object> rowData, String tableId, DecoderField field, String code, String keyString) throws KettleException {
		ValueMetaInterface valueMeta = new ValueMeta(field.getFieldId());
		boolean ignore;
		if (!Const.isEmpty(field.getFieldName())) {
			ignore = false;
			valueMeta.setName(field.getFieldName());
		} else {
			ignore=true;
		}
		Object object = null;
		
		String part;
		if (code!=null) {
			if (position>=code.length()) {
				part = null;
			} else {
				if (position+field.getLength()>=code.length()) {
					part = code.substring(position);
				} else {
					part = code.substring(position, position+field.getLength());
				}
			}
		} else {
			part = null;
		}
		
		if (field.getDataType()!=DataType.T) {
			lastDateString = null;
		}
		
    switch(field.getFieldTrimType()) {
    case ValueMetaInterface.TRIM_TYPE_NONE: break;
    case ValueMetaInterface.TRIM_TYPE_LEFT: part = Kjube.ltrim(part); break;
    case ValueMetaInterface.TRIM_TYPE_RIGHT: part = Kjube.rtrim(part); break;
    case ValueMetaInterface.TRIM_TYPE_BOTH: part = Kjube.trim(part); break;
    default: throw new KettleException("Non-standard trim type specified: "+field.getFieldTrimType());
    }

    /*
    if (!Const.isEmpty(field.getFieldName()) && field.getFieldName().length()>=2) {
			String type = field.getFieldName().substring(0,2);
			if ("DS".equalsIgnoreCase(type)) {
				part = Kjube.trim(part);
			}
		}
		*/
		

		switch(field.getDataType()) {
		case A : // Alphanumeric (String):
			valueMeta.setType(ValueMetaInterface.TYPE_STRING);
			valueMeta.setLength(field.getLength());
			object = part;
			break;
		case D : // Date (yyyy/MM/dd)
			valueMeta.setType(ValueMetaInterface.TYPE_DATE);
				try {
					object = data.convertStringToDate(part);
				} catch (ParseException e) {
					throw new KettleException("Date parse problem for field {"+field+"}, key="+keyString+", part="+part+", position="+position+", length="+field.getLength(), e);
				}
			lastDateString = part;
			break;
		case T : // Time part (HHmmss)
			if (!Const.isEmpty(part) && lastDateString==null) {
				throw new KettleException("Time without a date found: "+part+" for tableId="+tableId+" and fieldId="+keyString);
			}
			valueMeta.setType(ValueMetaInterface.TYPE_DATE);
			try {
				object = Const.isEmpty(part) ? null : data.convertStringToDate(lastDateString+part);
			} catch (ParseException e) {
				throw new KettleException("Time parse problem for field {"+field+"}, key="+keyString+", part="+part+", position="+position+", length="+field.getLength(), e);
			}
			break;
		case N : // Numeric
			valueMeta.setType(ValueMetaInterface.TYPE_NUMBER);
			try {
				part = Kjube.trim(part);
				object = Const.isEmpty(part) ? null : Double.parseDouble(part);
			} catch (NumberFormatException e) {
				throw new KettleException("Number parse problem for field {"+field+"}, key="+keyString+", part="+part+", position="+position+", length="+field.getLength(), e);
			}
			break;
		default :
			throw new KettleException("Unhandled data type encountered: "+field.getDataType().getDescription()+" in tableId="+tableId+", fielId="+keyString);
		}
		
		if (!ignore) {
			rowMeta.addValueMeta(valueMeta);
			rowData.add(object);
		}
		
		position+=field.getLength();
		
	}

	private void readSourceData() throws KettleException {
		
		// Read in the data from the mask stream...
		//

		if (Const.isEmpty(meta.getMaskSourceStep())) {
			throw new KettleException("No mask source step specified");
		}
		
		RowSet maskRowSet = findInputRowSet(meta.getMaskSourceStep());
		if (maskRowSet==null) {
			throw new KettleException("The specified mask source step '"+meta.getMaskSourceStep()+"' could not be found");
		}

		Object[] row = getRowFrom(maskRowSet);
		RowMetaInterface rowMeta = maskRowSet.getRowMeta();
		int count=0;
		while (row!=null) {
			count++;
			// Get the table ID rowMeta.getString(row)
			//
			String tableId = Kjube.trim(rowMeta.getString(row, meta.getMaskTableIdField(), null));
			if (Const.isEmpty(tableId)) {
				throw new KettleException("Unable to find data for field '"+meta.getMaskTableIdField()+"' while reading from step '"+meta.getMaskSourceStep()+"'");
			}
			
			String tableName = Kjube.trim( rowMeta.getString(row, meta.getMaskTableNameField(), null) );
			if (!Const.isEmpty(tableName)) {
				data.tableNameMap.put(tableId, tableName);
			}

			// Grab the other fields.
			//
			String fieldId = rowMeta.getString(row, meta.getMaskFieldIdField(), null);
			if (Const.isEmpty(fieldId)) {
				throw new KettleException("Unable to find data for field '"+meta.getMaskFieldIdField()+"' while reading from step '"+meta.getMaskSourceStep()+"'");
			}
      
      String trimTypeCode = rowMeta.getString(row, meta.getMaskFieldTrimField(), null);
      if (Const.isEmpty(trimTypeCode)) {
        throw new KettleException("Unable to find data for field '"+meta.getMaskFieldTrimField()+"' while reading from step '"+meta.getMaskSourceStep()+"'");
      }
      
      int trimType = ValueMeta.getTrimTypeByCode(trimTypeCode);
			String fieldName = Kjube.trim(rowMeta.getString(row, meta.getMaskFieldNameField(), null));
			String fieldType = rowMeta.getString(row, "TPFIELD", null);
			int fieldNr = Const.toInt( Kjube.trim( rowMeta.getString(row, "SQFIELD", null) ), -1);
			String dataType = rowMeta.getString(row, "IDDATATYPE", null);
			int length = Const.toInt( Kjube.trim( rowMeta.getString(row, "NRLENGTH", null)), -1);
			Date timeStamp = rowMeta.getDate(row, rowMeta.indexOfValue("TIMESTAMP"));

			if (fieldNr<0 || length<0) {
				throw new KettleException("Unexpectedly received an incorrect value for sqField or nrLength.  IDTABLE="+tableId+", IDFIELD="+fieldId);
			}
			
			// Get the map for this table...
			//
			Map<String, DecoderField> fieldMap = data.decoderMap.get(tableId);
			if (fieldMap==null) {
				fieldMap = new HashMap<String, DecoderField>();
				data.decoderMap.put(tableId, fieldMap);
				System.out.println("Stored field ["+tableId+"].["+fieldId+"] in decoder map");
			}
			
			// Store the entry for this field ID:
			//
			DecoderField decoderField = new DecoderField(tableId, fieldId, fieldName, fieldType, trimType, fieldNr, dataType, length, timeStamp);
			if (fieldMap.get(fieldId)!=null) {
				throw new KettleException("IDTABLE="+tableId+" : duplicate IDFIELD detected : "+fieldId);
			}
			fieldMap.put(fieldId, decoderField);
			
			// Get a new row too!
			//
			row = getRowFrom(maskRowSet);
		}
		
		if (data.decoderMap.isEmpty()) {
			throw new KettleException("The decoder map is empty: "+count+" rows were read from step '"+meta.getMaskSourceStep()+"'");
		}
		
		// OK, let's do some pre-processing: create simple lists where we first read the mask data...
		//
		for (String tableId : data.decoderMap.keySet()) {
			Map<String, DecoderField> fieldMap = data.decoderMap.get(tableId);
			String tableName = data.tableNameMap.get(tableId);
			DecoderTable decoderTable = new DecoderTable(tableId, tableName, fieldMap);
			data.decoderTables.put(tableId, decoderTable);
		}
		
		// TODO: clean out data.decoderMap, it's no longer needed.
	}
	
	/**
	 * Determines the batch ID DB connection
	 * @return
	 * @throws KettleException
	 */
	public DatabaseMeta getActualDatabaseMeta(List<DatabaseMeta> databases) {
		
		if (meta.getConnection()==null && !Const.isEmpty(meta.getConnectionName())) {
			return DatabaseMeta.findDatabase(databases, environmentSubstitute(meta.getConnectionName()));
		}
		
		return meta.getConnection();
	}

	
	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
				
		if (super.init(smi, sdi)) {
			
			data.databaseMeta = getActualDatabaseMeta(getTransMeta().getDatabases());
			if (data.databaseMeta==null) {
				logError("Unable to find a target database to work with");
				return false;
			}
			
			data.commitSize = 100; // TODO: move to metadata!!!!!!!!!!!!!!!!!
			
            data.db=new Database(data.databaseMeta);
			data.db.shareVariablesWith(this);
			
			try {
	            if (getTransMeta().isUsingUniqueConnections())
	            {
	                synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
	            }
	            else
	            {
	                data.db.connect(getPartitionID());
	            }
			} catch(KettleException e) {
				logError("Unbale to connect to database '"+data.databaseMeta.getName()+"'", e);
				return false;
			}
            if(log.isBasic()) logBasic("Connected to database ["+data.databaseMeta+"] (commit="+data.commitSize+")");
			data.db.setCommit(data.commitSize);
			
			return true;
		}
		return false;
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

        for (TableStatements tableStatements : data.statementsMap.values()) {
    		try
    		{
	        	// Get a commit counter per prepared statement to keep track of separate tables, etc. 
			    //
				Integer batchCounter = data.commitCounterMap.get(tableStatements.getDecoderTable().getTableId());
			    if (batchCounter==null) {
			    	batchCounter = 0;
			    }
			    
			    data.db.closePreparedStatement(tableStatements.getLookupStatement());
	            data.db.closePreparedStatement(tableStatements.getInsertStatement());
	            data.db.closePreparedStatement(tableStatements.getUpdateStatement());
    		}
    		catch(KettleException e) {
    			logError("Unexpected error closing statements & connection", e);
    		}

        }
        data.db.disconnect();

		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run() {
		BaseStep.runStepThread(this, meta, data);
	}
}
