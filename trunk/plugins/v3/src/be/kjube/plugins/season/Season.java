package be.kjube.plugins.season;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
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

public class Season extends BaseStep implements StepInterface {
	private SeasonMeta	meta;
	private SeasonData	data;
	private String	currentFieldname;

	public Season(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(SeasonMeta)getStepMeta().getStepMetaInterface();
		data=(SeasonData)stepDataInterface;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (SeasonMeta) smi;
		data = (SeasonData) sdi;

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
			
			data.removeIndexes = new ArrayList<Integer>();

			for (SeasonField field : meta.getFields()) {
				SeasonIndexes index = new SeasonIndexes();
				index.centuriesFieldIndex = getFieldIndex(field.getCenturiesField(), data.removeIndexes);
				index.yearsFieldIndex = getFieldIndex(field.getYearsField(), data.removeIndexes);
				index.seasonNumberFieldIndex = getFieldIndex(field.getSeasonNumberField(), data.removeIndexes);
				data.indexes.add(index);
			}
			
			Collections.sort(data.removeIndexes);
			
		} // end if first

		try {
			Object[] outputRowData = calculateSeasonIDs(getInputRowMeta(), r);
			putRow(data.outputRowMeta, outputRowData); // copy row to output rowset(s);

		} catch (Exception e) {
			boolean sendToErrorRow = false;
			String errorMessage = null;

			if (getStepMeta().isDoingErrorHandling()) {
				sendToErrorRow = true;
				errorMessage = e.toString();
			} else {
				logError(Messages.getString("Season.Log.ErrorInStep", e.getMessage())); //$NON-NLS-1$
				e.printStackTrace();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow) {
				// Simply add this row to the error row
				putError(getInputRowMeta(), r, 1, errorMessage, currentFieldname, "SEASON001");
			}
		}
		return true;
	}

	private int getFieldIndex(String fieldName, List<Integer> indexes) throws KettleException {
		int index = getInputRowMeta().indexOfValue(fieldName);
		if (index<0) {
			throw new KettleException("Unable to find field '"+fieldName+"' in the input fields");
		}
		
		if (index>=0 && !indexes.contains(index)) {
			indexes.add(index);
		}
		
		return index;
	}

	private Object[] calculateSeasonIDs(RowMetaInterface rowMeta, Object[] r) throws KettleException {
		
		Long[] seasonIds = new Long[data.indexes.size()];
		
		for (int i=0;i<data.indexes.size();i++) {
			SeasonIndexes index = data.indexes.get(i);
			SeasonField field = meta.getFields().get(i);
			
			currentFieldname = field.getResultField();
			
			long centuries = getFieldValue(rowMeta, r, index.centuriesFieldIndex);
			long years = getFieldValue(rowMeta, r, index.yearsFieldIndex);
			long seasonNumber = getFieldValue(rowMeta, r, index.seasonNumberFieldIndex);

			KettleException exception = null;
			
			// Perform basic validations!
			//
			if (centuries<18 || centuries>22) {
				exception = new KettleException("Invalid centuries value specified: "+centuries+" in field '"+field.getCenturiesField()+"'");
			}
			if (years<0 || years>99) {
				exception = new KettleException("Invalid years value specified: "+years+" in field '"+field.getYearsField()+"'");
			}
			if (seasonNumber<0 || seasonNumber>9) {
				exception = new KettleException("Invalid season number value specified: "+seasonNumber+" in field "+field.getSeasonNumberField()+"'");
			}

			if (meta.isIgnoringErrors()) {
				if (exception!=null) {
					seasonIds[i] = new Long(0);
				} else {
					seasonIds[i] = calcSeasonId(centuries, years, seasonNumber);
				}
			} else {
				if (exception!=null) {
					throw exception;
				} else {
					seasonIds[i] = calcSeasonId(centuries, years, seasonNumber);
				}
			}
		}
		
		// Create a new empty row...
		//
		Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		
		// Make sure to copy only those fields we would like to keep
		// Ignore the fields we already consumed
		//
		int outputIndex=0;
		int removeIndex=0;
		for (int i=0;i<rowMeta.size();i++) {
			// See if we don't have to remove this index...
			//
			int remove = -1;
			if (removeIndex<data.removeIndexes.size()) {
				remove = data.removeIndexes.get(removeIndex);
			}
			while (remove<i && removeIndex<data.removeIndexes.size()-1) {
				remove = data.removeIndexes.get(++removeIndex);
			}
			if (remove!=i) {
				outputRowData[outputIndex++] = r[i];
			}
		}
		
		// Add the new fields at the very end...
		//
		for (Long seasonId : seasonIds) {
			outputRowData[outputIndex++] = seasonId;
		}
		
		return outputRowData;
	}

	private Long calcSeasonId(long centuries, long years, long seasonNumber) {
		return Long.valueOf(centuries*1000+years*10+seasonNumber);
	}

	private long getFieldValue(RowMetaInterface rowMeta, Object[] r, int minutesFieldIndex) throws KettleException {
		Long value = rowMeta.getInteger(r, minutesFieldIndex);
		if (value==null) {
			throw new KettleException("Value '"+rowMeta.getValueMeta(minutesFieldIndex)+"' can't be null");
		}
		long longValue = value.longValue();
		if (longValue<0) {
			throw new KettleException("Value '"+rowMeta.getValueMeta(minutesFieldIndex)+"' can't be negative: "+longValue);
		}
		return longValue;
	}

	//
	// Run is were the action happens!
	public void run() {
		BaseStep.runStepThread(this, meta, data);
	}
}
