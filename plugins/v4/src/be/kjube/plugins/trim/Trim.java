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

package be.kjube.plugins.trim;

import java.util.Arrays;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import be.kjube.plugins.trim.TrimMeta.TrimType;
import be.kjube.util.Kjube;

/**
 * Sets a field value to a constant if it is null
 * 
 * @author Matt
 * @since 19-11-2009
 */

public class Trim extends BaseStep implements StepInterface {
	private static Class<?> PKG = Trim.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private TrimMeta	meta;
	private TrimData	data;

	public Trim(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(TrimMeta)getStepMeta().getStepMetaInterface();
		data=(TrimData)stepDataInterface;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (TrimMeta) smi;
		data = (TrimData) sdi;

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

			data.fieldnrs = new int[0];

			if (!meta.isTrimmingAllFields() || meta.isExcludingFromTrim()) {
				// Consider only selected fields or exclude selected fields from trim
				//
				if (!Const.isEmpty(meta.getFieldName())) {
					data.fieldnrs = new int[meta.getFieldName().length];

					for (int i = 0; i < meta.getFieldName().length; i++) {
						data.fieldnrs[i] = data.outputRowMeta.indexOfValue(meta.getFieldName()[i]);
						if (data.fieldnrs[i] < 0) {
							logError(BaseMessages.getString(PKG, "Trim.Log.CanNotFindField", meta.getFieldName()[i]));
							throw new KettleException(BaseMessages.getString(PKG, "Trim.Log.CanNotFindField", meta.getFieldName()[i]));
						}
					}
				}
			}
			
			if (meta.isExcludingFromTrim()) {
				Arrays.sort(data.fieldnrs);
			}
			
		} // end if first

		try {
			updateFields(getInputRowMeta(), r);
			putRow(data.outputRowMeta, r); // copy row to output rowset(s);

		} catch (Exception e) {
			boolean sendToErrorRow = false;
			String errorMessage = null;

			if (getStepMeta().isDoingErrorHandling()) {
				sendToErrorRow = true;
				errorMessage = e.toString();
			} else {
				logError(BaseMessages.getString(PKG, "Trim.Log.ErrorInStep", e.getMessage())); //$NON-NLS-1$
				e.printStackTrace();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow) {
				// Simply add this row to the error row
				putError(data.outputRowMeta, r, 1, errorMessage, null, "TRIM001");
			}
		}
		return true;
	}

	private void updateFields(RowMetaInterface rowMeta, Object[] r) throws Exception {
		if ((meta.isTrimmingAllFields() && meta.getAllFieldsTrimType() != TrimType.NONE)) {
			// Trim all fields except those specified.
			//
			int excludeIndex = 0;
			for (int i = 0; i < rowMeta.size(); i++) {
				ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
				
				int exclude = -1;
				if (excludeIndex<data.fieldnrs.length) {
					exclude = data.fieldnrs[excludeIndex];
				}

				while (exclude<i && excludeIndex<data.fieldnrs.length-1) {
					exclude = data.fieldnrs[++excludeIndex];
				}
				
				if (i!=exclude && valueMeta.isString()) {
					String string = valueMeta.getString(r[i]);
					r[i] = trimString(string, meta.getAllFieldsTrimType());
				}
			}
		} else {
			// Loop over the fields
			//
			for (int i = 0; i < data.fieldnrs.length; i++) {
				ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fieldnrs[i]);
				TrimType trimType = meta.getFieldTrimType()[i];

				if (valueMeta.isString()) {
					if (trimType != TrimType.NONE) {
						String string = valueMeta.getString(r[data.fieldnrs[i]]);
						r[data.fieldnrs[i]] = trimString(string, meta.getFieldTrimType()[i]);
					}
				} else {
					// A warning message seems over the top since you simply
					// can't trim a non string field
					// The point is also lost since there are no adverse effects
					// of NOT trimming a field that can't be trimmed.
				}
			}
		}
	}

	private String trimString(String string, TrimType trimType) {
		switch (trimType) {
		case LEFT:
			return Kjube.ltrim(string);
		case RIGHT:
			return Kjube.rtrim(string);
		case BOTH:
			return Kjube.trim(string);
		default:
			return string;
		}
	}
	

	//
	// Run is were the action happens!
	public void run() {
		BaseStep.runStepThread(this, meta, data);
	}
}
