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
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.job.Job;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Sets a field value to a constant if it is null
 * 
 * @author Matt
 * @since 19-11-2009
 */

public class Rejects extends BaseStep implements StepInterface {
	private RejectsMeta	meta;
	private RejectsData	data;

	public Rejects(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(RejectsMeta)getStepMeta().getStepMetaInterface();
		data=(RejectsData)stepDataInterface;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (RejectsMeta) smi;
		data = (RejectsData) sdi;

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

			data.keyIndexes = new ArrayList<Integer>();

			for (String keyField : meta.getKeyFields()) {
				String key = environmentSubstitute(keyField);
				if (!Const.isEmpty(key)) {
					int index = getInputRowMeta().indexOfValue(key);
					if (index<0) {
						throw new KettleException(Messages.getString("Rejects.Log.CanNotFindField", keyField));
					}
					data.keyIndexes.add(index);
				}
			}
			
			data.errorCountIndex = getInputRowMeta().indexOfValue(environmentSubstitute(meta.getErrorCountField()));
			data.errorDescriptionsIndex = getInputRowMeta().indexOfValue(environmentSubstitute(meta.getErrorDescriptionsField()));
			data.errorFieldsIndex = getInputRowMeta().indexOfValue(environmentSubstitute(meta.getErrorFieldsField()));
			data.errorCodesIndex = getInputRowMeta().indexOfValue(environmentSubstitute(meta.getErrorCodesField()));
			
			data.valueIndexes = new ArrayList<Integer>();
			
			for (int i=0;i<getInputRowMeta().size();i++) {
				if (!data.keyIndexes.contains(i) && 
						i!=data.errorCountIndex && 
						i!=data.errorDescriptionsIndex &&
						i!=data.errorFieldsIndex &&
						i!=data.errorCodesIndex) {
					data.valueIndexes.add(i);
				}
			}
			
			String[] prevStepNames = getTransMeta().getPrevStepNames(getStepMeta());
			if (prevStepNames==null || prevStepNames.length==0) {
				throw new KettleException(Messages.getString("Rejects.Log.CanNotFindPreviousSteps"));
			}
			data.previousSteps = "";
			for (String prevStepName : prevStepNames) {
				if (data.previousSteps.length()>0) data.previousSteps+=", ";
				data.previousSteps+=prevStepName;
			}

			data.db.prepareInsert(data.outputRowMeta, environmentSubstitute(meta.getRejectsSchemaName()), environmentSubstitute(meta.getRejectsTableName()));

		} // end if first

		// Assemble the data...
		//
		Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		int outputIndex = 0;
			
		// The ID
		//
		StringBuffer id = new StringBuffer();
		for (int index : data.keyIndexes) {
			if (id.length() > 0)
				id.append(", ");
			id.append(getInputRowMeta().getString(r, index));
		}
		outputRowData[outputIndex++] = id.toString();

		// The row serialized as a String
		//
		StringBuffer value = new StringBuffer();
		for (int index : data.valueIndexes) {
			if (value.length() > 0)
				value.append(", ");
			value.append(getInputRowMeta().getString(r, index));
		}
		outputRowData[outputIndex++] = value.toString();

		// The Batch ID
		//
		outputRowData[outputIndex++] = Long.valueOf(getTrans().getBatchId());

		// The transformation name
		//
		outputRowData[outputIndex++] = getTransMeta().getName();

		// The parent job name
		//
		Job parentJob = getTrans().getParentJob();
		outputRowData[outputIndex++] = parentJob!=null ? parentJob.getJobMeta().getName() : null;

		// The rejecting step
		//
		outputRowData[outputIndex++] = data.previousSteps;

		// Error count
		//
		if (data.errorCountIndex>=0) {
			outputRowData[outputIndex++] = getInputRowMeta().getInteger(r, data.errorCountIndex);
		} else {
			outputIndex++;
		}

		// Error Descriptions
		//
		if (data.errorDescriptionsIndex>=0) {
			outputRowData[outputIndex++] = getInputRowMeta().getString(r, data.errorDescriptionsIndex);
		} else {
			outputIndex++;
		} 

		// Error Fields
		//
		if (data.errorFieldsIndex>=0) {
			outputRowData[outputIndex++] = getInputRowMeta().getString(r, data.errorFieldsIndex);
		} else {
			outputIndex++;
		}
		
		// Error codes
		//
		if (data.errorCodesIndex>=0) {
			outputRowData[outputIndex++] = getInputRowMeta().getString(r, data.errorCodesIndex);
		} else {
			outputIndex++;
		}

		// A logging date
		//
		outputRowData[outputIndex++] = new Date();

		// Write the row to the database...
		//
		data.db.setValuesInsert(data.outputRowMeta, outputRowData);
		data.db.insertRow();

		// Pass on the row for debugging purposes
		//
		putRow(data.outputRowMeta, outputRowData); 
		
		return true;
	}

	@Override
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		
		if (super.init(smi, sdi)) {
			
			DatabaseMeta dbMeta = meta.getDatabaseMeta();
			if (dbMeta==null) {
				dbMeta = getTransMeta().findDatabase( environmentSubstitute(meta.getDatabaseName()) );
			}
			if (dbMeta==null) {
				logError("Unable to find a valid database connection");
				return false;
			}
			
			data.db = new Database(dbMeta);
			try {
				data.db.shareVariablesWith(this);
				
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
                
                if(log.isBasic()) logBasic("Connected to database ["+meta.getDatabaseMeta()+"] (commit="+1000+")");
				data.db.setCommit(1000);
			
				return true;
			} catch(Exception e) {
				logError("An error occurred intialising this step", e);
				stopAll();
				setErrors(1);
			}
		}
		return false;
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		
		try {
            data.db.closeInsert();
            data.db.disconnect();

		} catch(Exception e) {
			logError("Error closing database connection, error closing insert statement", e);
		}
		super.dispose(smi, sdi);
	}

	//
	// Run is were the action happens!
	public void run() {
		BaseStep.runStepThread(this, meta, data);
	}
}
