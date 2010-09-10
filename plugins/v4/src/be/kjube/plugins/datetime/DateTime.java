package be.kjube.plugins.datetime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
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

public class DateTime extends BaseStep implements StepInterface {
	private static Class<?> PKG = DateTime.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DateTimeMeta	meta;
	private DateTimeData	data;
	private String	currentFieldname;

	public DateTime(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(DateTimeMeta)getStepMeta().getStepMetaInterface();
		data=(DateTimeData)stepDataInterface;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (DateTimeMeta) smi;
		data = (DateTimeData) sdi;

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

			for (DateTimeField field : meta.getFields()) {
				DateTimeIndexes index = new DateTimeIndexes();
				index.centuriesFieldIndex = getFieldIndex(field.getCenturiesField(), data.removeIndexes, false);
				index.yearsFieldIndex = getFieldIndex(field.getYearsField(), data.removeIndexes, false);
				index.monthsFieldIndex = getFieldIndex(field.getMonthsField(), data.removeIndexes, false);
				index.daysFieldIndex = getFieldIndex(field.getDaysField(), data.removeIndexes, false);
				index.hoursFieldIndex = getFieldIndex(field.getHoursField(), data.removeIndexes, true);
				index.minutesFieldIndex = getFieldIndex(field.getMinutesField(), data.removeIndexes, true);
				index.secondsFieldIndex = getFieldIndex(field.getSecondsField(), data.removeIndexes, true);
				data.indexes.add(index);
			}
			
			Collections.sort(data.removeIndexes);
			
		} // end if first

		try {
			Object[] outputRowData = calculateDateTimes(getInputRowMeta(), r);
			putRow(data.outputRowMeta, outputRowData); // copy row to output rowset(s);

		} catch (Exception e) {
			boolean sendToErrorRow = false;
			String errorMessage = null;

			if (getStepMeta().isDoingErrorHandling()) {
				sendToErrorRow = true;
				errorMessage = e.toString();
			} else {
				logError(BaseMessages.getString(PKG, "DateTime.Log.ErrorInStep", e.getMessage())); //$NON-NLS-1$
				e.printStackTrace();
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow) {
				// Simply add this row to the error row
				putError(getInputRowMeta(), r, 1, errorMessage, currentFieldname, "DATETIME001");
			}
		}
		return true;
	}

	private int getFieldIndex(String fieldName, List<Integer> indexes, boolean optional) throws KettleException {
		int index = getInputRowMeta().indexOfValue(fieldName);
		if (index<0 && !optional) {
			throw new KettleException("Unable to find field '"+fieldName+"' in the input fields");
		}
		
		if (index>=0 && !indexes.contains(index)) {
			indexes.add(index);
		}
		
		return index;
	}

	private Object[] calculateDateTimes(RowMetaInterface rowMeta, Object[] r) throws KettleException {
		
		Date[] dates = new Date[data.indexes.size()];
		
		for (int i=0;i<data.indexes.size();i++) {
			DateTimeIndexes index = data.indexes.get(i);
			DateTimeField field = meta.getFields().get(i);
			
			currentFieldname = field.getResultField();
			
			int centuries = getFieldValue(rowMeta, r, index.centuriesFieldIndex, false);
			int years = getFieldValue(rowMeta, r, index.yearsFieldIndex, false);
			int months = getFieldValue(rowMeta, r, index.monthsFieldIndex, false);
			int days = getFieldValue(rowMeta, r, index.daysFieldIndex, false);
			int hours = 0;
			if (index.hoursFieldIndex>=0) {
				hours = getFieldValue(rowMeta, r, index.hoursFieldIndex, true);
			}
			int minutes = 0;
			if (index.minutesFieldIndex>=0) {
				minutes = getFieldValue(rowMeta, r, index.minutesFieldIndex, true);
			}
			int seconds = 0;
			if (index.secondsFieldIndex>=0) {
				seconds = getFieldValue(rowMeta, r, index.secondsFieldIndex, true);
			}
			KettleException exception = null;
			
			// Perform basic validations!
			//
			if (centuries<18 || centuries>22) {
				exception = new KettleException("Invalid centuries value specified: "+centuries+" in field '"+field.getCenturiesField()+"'");
				if (!meta.isIgnoringErrors()) throw exception;
			}
			if (years<0 || years>99) {
				exception = new KettleException("Invalid years value specified: "+years+" in field '"+field.getYearsField()+"'");
				if (!meta.isIgnoringErrors()) throw exception;
			}
			if (months<1 || months>12) {
				exception = new KettleException("Invalid months value specified: "+months+" in field '"+field.getMonthsField()+"'");
				if (!meta.isIgnoringErrors()) throw exception;
			}
			if (days<1 || days>31) {
				exception = new KettleException("Invalid days value specified: "+days+" in field '"+field.getDaysField()+"'");
				if (!meta.isIgnoringErrors()) throw exception;
			}
			if (hours<0 || hours>59) {
				exception = new KettleException("Invalid hours value specified: "+hours+" in field '"+field.getHoursField()+"'");
				if (!meta.isIgnoringErrors()) throw exception;
			}
			if (minutes<0 || minutes>59) {
				exception = new KettleException("Invalid minutes value specified: "+minutes+" in field '"+field.getMinutesField()+"'");
				if (!meta.isIgnoringErrors()) throw exception;
			}
			if (seconds<0 || seconds>59) {
				exception = new KettleException("Invalid seconds value specified: "+seconds+" in field '"+field.getSecondsField()+"'");
				if (!meta.isIgnoringErrors()) throw exception;
			}
			if (exception==null) {
				
				Calendar cal = Calendar.getInstance();
				cal.setLenient(false);
				
				cal.set(Calendar.YEAR, centuries*100+years);
				cal.set(Calendar.MONTH, months-1);
				cal.set(Calendar.DAY_OF_MONTH, days);
				cal.set(Calendar.HOUR_OF_DAY, hours);
				cal.set(Calendar.MINUTE, minutes);
				cal.set(Calendar.SECOND, seconds);
				cal.set(Calendar.MILLISECOND, 0);
				
				dates[i] = cal.getTime();
			} else {
				dates[i] = null;
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
		for (Date date : dates) {
			outputRowData[outputIndex++] = date;
		}
		
		return outputRowData;
	}

	private int getFieldValue(RowMetaInterface rowMeta, Object[] r, int fieldIndex, boolean nullAllowed) throws KettleException {
		Long value = rowMeta.getInteger(r, fieldIndex);
		if (value==null) {
			if (nullAllowed) {
				return 0;
			} else {
				throw new KettleException("Value '"+rowMeta.getValueMeta(fieldIndex)+"' can't be null");
			}
		}
		int intValue = value.intValue();
		if (intValue<0) {
			throw new KettleException("Value '"+rowMeta.getValueMeta(fieldIndex)+"' can't be negative: "+intValue);
		}
		return intValue;
	}

	//
	// Run is were the action happens!
	public void run() {
		BaseStep.runStepThread(this, meta, data);
	}
}
