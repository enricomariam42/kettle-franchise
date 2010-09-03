package be.kjube.generators.staging2ods;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.insertupdate.InsertUpdateMeta;
import org.pentaho.di.trans.steps.janino.JaninoMeta;
import org.pentaho.di.trans.steps.janino.JaninoMetaFunction;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;

import be.kjube.plugins.rejects.RejectsMeta;
import be.kjube.util.Kjube;

public class OdsGenerator {
	
	private String	tableName;
	private String	keyFields;
	private DatabaseMeta	sourceDb;
	private DatabaseMeta	targetDb;

	public OdsGenerator(DatabaseMeta sourceDb, DatabaseMeta targetDb, String tableName, String keyFields) {
		this.sourceDb = sourceDb;
		this.targetDb = targetDb;
		this.tableName = tableName;
		this.keyFields = keyFields;
	}
	
	public void generate() throws Exception {
		String sourceTable = tableName;
		String targetTable = "T"+tableName.substring(1);
		String filename = targetTable+".ktr";
		String[] keys = keyFields.split(",");
		
		LogWriter.getInstance().logDetailed(toString(), "Generating transformation ["+filename+"] to copy data from table ["+sourceTable+"] to ["+targetTable+"]");
		
		TransMeta transMeta = new TransMeta();
		transMeta.setName(targetTable);
		transMeta.setFilename(filename);
		
		// Source step: read from the source table...
		//
		TableInputMeta tableInputMeta = new TableInputMeta();
		tableInputMeta.setDatabaseMeta(sourceDb);
		tableInputMeta.setSQL("SELECT * FROM "+sourceTable);
		StepMeta sourceStep = new StepMeta("Read from "+sourceTable, tableInputMeta);
		sourceStep.setLocation(100, 100);
		sourceStep.setDraw(true);
		
		JaninoMeta janinoMeta = new JaninoMeta();
		janinoMeta.allocate(1);
		janinoMeta.getFormula()[0] = new JaninoMetaFunction("TIMESTAMP", "new java.util.Date()", ValueMetaInterface.TYPE_DATE, -1, -1, "TIMESTAMP");
		
		StepMeta timeStep = new StepMeta("Replace timestamp", janinoMeta);
		timeStep.setLocation(300, 100);
		timeStep.setDraw(true);

		TransHopMeta source2time = new TransHopMeta(sourceStep, timeStep);

	
		// Target step: upsert into the target table...
		//
		RowMetaInterface targetFields = getTargetFields(targetTable);
		InsertUpdateMeta insertUpdateMeta = new InsertUpdateMeta();
		insertUpdateMeta.setDatabaseMeta(targetDb);
		insertUpdateMeta.setTableName(targetTable);
		insertUpdateMeta.allocate(keys.length, targetFields.size());
		insertUpdateMeta.setCommitSize(1000);
		for (int i=0;i<keys.length;i++) {
			insertUpdateMeta.getKeyLookup()[i] = keys[i];
			insertUpdateMeta.getKeyCondition()[i] = "=";
			insertUpdateMeta.getKeyStream()[i] = keys[i];
		}

		for (int i=0;i<targetFields.size();i++) {
			ValueMetaInterface valueMeta = targetFields.getValueMeta(i);
			insertUpdateMeta.getUpdate()[i] = Const.indexOfString(valueMeta.getName(), keys)<0;
			insertUpdateMeta.getUpdateLookup()[i] = valueMeta.getName();
			insertUpdateMeta.getUpdateStream()[i] = valueMeta.getName();
		}
		StepMeta targetStep= new StepMeta("Update table "+targetTable, insertUpdateMeta);
		targetStep.setLocation(500, 100);
		targetStep.setDraw(true);
				
		TransHopMeta time2target = new TransHopMeta(timeStep, targetStep);
		
		// Now add the Rejects step...
		//
		RejectsMeta rejectsMeta = new RejectsMeta();
		rejectsMeta.setDefault();
		for (String key : keys) {
			rejectsMeta.getKeyFields().add(key);
		}
		
		StepMeta rejectsStep = new StepMeta("Rejects of "+targetTable, rejectsMeta);
		rejectsStep.setLocation(500, 300);
		rejectsStep.setDraw(true);
		
		StepErrorMeta stepErrorMeta = new StepErrorMeta(transMeta, targetStep);
		stepErrorMeta.setTargetStep(rejectsStep);
		stepErrorMeta.setEnabled(true);
		stepErrorMeta.setNrErrorsValuename( Kjube.DEFAULT_ERROR_COUNT_VARIABLE_NAME );
		stepErrorMeta.setErrorDescriptionsValuename( Kjube.DEFAULT_ERROR_DESCRIPTIONS_VARIABLE_NAME );
		stepErrorMeta.setErrorFieldsValuename( Kjube.DEFAULT_ERROR_FIELDS_VARIABLE_NAME );
		stepErrorMeta.setErrorCodesValuename( Kjube.DEFAULT_ERROR_CODES_VARIABLE_NAME );
		targetStep.setStepErrorMeta(stepErrorMeta);
		
		TransHopMeta errorHop = new TransHopMeta(targetStep, rejectsStep);
		
		// Add all this information to the transformation
		//
		transMeta.addDatabase(sourceDb);
		transMeta.addDatabase(targetDb);
		transMeta.addStep(sourceStep);
		transMeta.addStep(timeStep);
		transMeta.addStep(targetStep);
		transMeta.addStep(rejectsStep);
		transMeta.addTransHop(source2time);
		transMeta.addTransHop(time2target);
		transMeta.addTransHop(errorHop);
		
		// Save to file!
		//
		String xml = transMeta.getXML();
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(filename)));
		dos.write(xml.getBytes("UTF-8"));
		dos.close();

	}

	
	private RowMetaInterface getTargetFields(String targetTable) throws KettleException {
		Database db = new Database(targetDb); 
		try {
			db.connect();
			return db.getTableFields(targetTable);
		} catch(Exception e) {
			throw new KettleException(e);
		} finally {
			if (db!=null) {
				db.disconnect();
			}
		}
	}


	public static final DatabaseMeta SOURCE_DB = new DatabaseMeta("MySQL-Fendi", "MySQL", "JDBC", "localhost", "fendi", "3306", "matt", "abcd"); 
	public static final DatabaseMeta TARGET_DB = new DatabaseMeta("MySQL-Test", "MySQL", "JDBC", "localhost", "test", "3306", "matt", "abcd"); 
	

	public static void main(String[] args) throws Exception {
		OdsGenerator generator = new OdsGenerator(SOURCE_DB, TARGET_DB, "SAMASK", "IDTABLE,IDFIELD");
		generator.generate();
	}
}
