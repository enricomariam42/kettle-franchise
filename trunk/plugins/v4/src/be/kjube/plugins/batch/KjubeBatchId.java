/* 
 * Copyright (c) 2009 kJube.  All rights reserved. 
 */ 


package be.kjube.plugins.batch;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;

import be.kjube.util.Kjube;

/**
 * The kJube batch ID manager
 *
 * (c) 2008-2009 kJube
 * @since 23-07-2009
 *
 */

@org.pentaho.di.core.annotations.JobEntry(
		id="KjubeBatchId",
		categoryDescription="kJube.Category.Name",
		i18nPackageName="be.kjube.plugins",
		image="be/kjube/plugins/images/batch.png", 
		name="kJube.Plugins.KjubeBatchId.Name",
		description="kJube.Plugins.KjubeBatchId.Description"
	)
public class KjubeBatchId extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = KjubeBatchId.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public enum OperationType {
		
		START_OF_JOB("start", "Start of job run"),
		JOB_SUCCESS("success", "Job success"),
		JOB_FAILURE("failure", "Job failure"),
		;
		
		private String code;
		private String description;
		
		OperationType(String code, String description) {
			this.code=code;
			this.description = description;
		}
		
		public String getCode() {
			return code;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String toString() {
			return description+"("+code+")";
		}
	}
	
	

	public static final String[] PERIODICITY_OPTIONS = { "5 minutes", "10 minutes", "15 minutes", "30 minutes", "1 hour", "2 hours", "3 hours", "6 hours", "12 hours", "1 day", "2 days", "3 days", "1 week", "1 month", "2 months", "quarter", "year", };
	public static final String DEFAULT_PERIODICITY_OPTION = "1 day";

	private DatabaseMeta batchIdConnection;
	private String batchIdConnectionName;
	private String batchIdSchema;
	private String batchIdTable;
	
	private DatabaseMeta batchLoggingConnection;
	private String batchLoggingConnectionName;
	private String batchLoggingSchema;
	private String batchLoggingTable;
	
	private String periodicity;
	private OperationType operationType;
	private String batchIdVariableName;
		
	public KjubeBatchId(String name)
	{
		super(name, "");

		setID(-1L);
	    
	    setBatchIdConnectionName(Kjube.DEFAULT_BATCH_ID_CONNECTION);
	    setBatchIdSchema(Kjube.DEFAULT_BATCH_ID_SCHEMA);
	    setBatchIdTable(Kjube.DEFAULT_BATCH_ID_TABLE);
	    setBatchLoggingConnectionName(Kjube.DEFAULT_BATCH_LOGGING_CONNECTION);
	    setBatchLoggingSchema(Kjube.DEFAULT_BATCH_LOGGING_SCHEMA);
	    setBatchLoggingTable(Kjube.DEFAULT_BATCH_LOGGING_TABLE);
	    
	    setOperationType(OperationType.START_OF_JOB);
	    setPeriodicity(DEFAULT_PERIODICITY_OPTION);
	    setBatchIdVariableName(Kjube.DEFAULT_BATCH_ID_VARIABLE_NAME);
	}

	public KjubeBatchId()
	{
		this("");
	}

    public Object clone()
    {
        KjubeBatchId je = (KjubeBatchId) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(128);
		
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("batch_id_connection",     batchIdConnection==null ? null : batchIdConnection.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("batch_id_connection_name", batchIdConnectionName)); // Variables!
		retval.append("      ").append(XMLHandler.addTagValue("batch_id_schema",         batchIdSchema));
		retval.append("      ").append(XMLHandler.addTagValue("batch_id_table",          batchIdTable));

		retval.append("      ").append(XMLHandler.addTagValue("batch_logging_connection", batchLoggingConnection==null ? null : batchLoggingConnection.getName()));
		retval.append("      ").append(XMLHandler.addTagValue("batch_logging_connection_name", batchLoggingConnectionName)); // Variables!
		retval.append("      ").append(XMLHandler.addTagValue("batch_logging_schema",     batchLoggingSchema));
		retval.append("      ").append(XMLHandler.addTagValue("batch_logging_table",      batchLoggingTable));

		retval.append("      ").append(XMLHandler.addTagValue("periodicity",  periodicity));
		retval.append("      ").append(XMLHandler.addTagValue("operation_type",  operationType==null ? null : operationType.getCode()));
		retval.append("      ").append(XMLHandler.addTagValue("batch_id_variable",  batchIdVariableName));

		return retval.toString();
	}
	
	  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
	    try
	    {
	        super.loadXML(entrynode, databases, slaveServers);
	      	
	        batchIdConnection = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(entrynode, "batch_id_connection"));
	        batchIdConnectionName = XMLHandler.getTagValue(entrynode, "batch_id_connection_name");
	        batchIdSchema = XMLHandler.getTagValue(entrynode, "batch_id_schema");
	        batchIdTable  = XMLHandler.getTagValue(entrynode, "batch_id_table");

	        batchLoggingConnection = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(entrynode, "batch_logging_connection"));
	        batchLoggingConnectionName = XMLHandler.getTagValue(entrynode, "batch_logging_connection_name");
	        batchLoggingSchema = XMLHandler.getTagValue(entrynode, "batch_logging_schema");
	        batchLoggingTable  = XMLHandler.getTagValue(entrynode, "batch_logging_table");

	        periodicity = XMLHandler.getTagValue(entrynode, "periodicity");
	        operationType = findOperationTypeByCode( XMLHandler.getTagValue(entrynode, "operation_type") );
	        batchIdVariableName = XMLHandler.getTagValue(entrynode, "batch_id_variable");
	    }
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry from XML node", xe);
		}
	}

	public static OperationType findOperationTypeByCode(String code) {
		for (OperationType operationType : OperationType.values()) {
			if (operationType.getCode().equalsIgnoreCase(code)) {
				return operationType;
			}
		}
		return null;
	}

	public static OperationType findOperationTypeByDescription(String description) {
		for (OperationType operationType : OperationType.values()) {
			if (operationType.getDescription().equalsIgnoreCase(description)) {
				return operationType;
			}
		}
		return null;
	}
	
	@Override
	public void loadRep(Repository rep, ObjectId idJobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
		try
	    {
	        super.loadRep(rep, idJobentry, databases, slaveServers);
	        
	        batchIdConnection = rep.loadDatabaseMetaFromJobEntryAttribute(idJobentry, "batch_id_connection_name", "batch_id_connection_id", databases);
	        
	        batchIdConnectionName = rep.getJobEntryAttributeString(idJobentry, "batch_id_connection_name");
	        batchIdSchema     = rep.getJobEntryAttributeString(idJobentry, "batch_id_schema");
	        batchIdTable      = rep.getJobEntryAttributeString(idJobentry, "batch_id_table");

	        batchIdConnection = rep.loadDatabaseMetaFromJobEntryAttribute(idJobentry, "batch_logging_connection_id", "batch_logging_connection_name", databases);
	        batchLoggingConnectionName = rep.getJobEntryAttributeString(idJobentry, "batch_logging_connection_name");
	        batchLoggingSchema     = rep.getJobEntryAttributeString(idJobentry, "batch_logging_schema");
	        batchLoggingTable      = rep.getJobEntryAttributeString(idJobentry, "batch_logging_table");

	        periodicity = rep.getJobEntryAttributeString(idJobentry, "periodicity");
	        operationType = findOperationTypeByCode( rep.getJobEntryAttributeString(idJobentry, "operation_type") );
	        batchIdVariableName = rep.getJobEntryAttributeString(idJobentry, "batch_id_variable");
	    }
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry from the repository for id_jobentry="+idJobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaJobEntryAttribute(id_job, getObjectId(), "batch_id_connection_name", "batch_id_connection_id", batchIdConnection);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "batch_id_connection_name",   batchIdConnectionName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "batch_id_schema",   batchIdSchema);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "batch_id_table",    batchIdTable);

			rep.saveDatabaseMetaJobEntryAttribute(id_job, getObjectId(), "batch_logging_connection_name", "batch_logging_connection_id", batchLoggingConnection);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "batch_logging_connection_name",   batchLoggingConnectionName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "batch_logging_schema",   batchLoggingSchema);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "batch_logging_table",    batchLoggingTable);
			
			rep.saveJobEntryAttribute(id_job, getObjectId(), "periodicity",    periodicity);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "operation_type",    operationType == null ? null : operationType.getCode());
			rep.saveJobEntryAttribute(id_job, getObjectId(), "batch_id_variable",    periodicity);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'ftp' to the repository for id_job="+id_job, dbe);
		}
	}
	
	/**
	 * Determines the batch ID DB connection
	 * @return
	 * @throws KettleException
	 */
	public DatabaseMeta getActualBatchIdDatabaseMeta(List<DatabaseMeta> databases) {
		
		if (batchIdConnection==null && !Const.isEmpty(batchIdConnectionName)) {
			return DatabaseMeta.findDatabase(databases, environmentSubstitute(batchIdConnectionName));
		}
		
		return batchIdConnection;
	}

	/**
	 * Determines the batch logging DB connection
	 * @return
	 * @throws KettleException
	 */
	public DatabaseMeta getActualBatchLoggingDatabaseMeta(List<DatabaseMeta> databases) {
		
		if (batchLoggingConnection==null && !Const.isEmpty(batchLoggingConnectionName)) {
			return DatabaseMeta.findDatabase(databases, environmentSubstitute(batchLoggingConnectionName));
		}
		
		return batchIdConnection;
	}

	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;

		try {
			
			// Verify a few things first...
			// 
			if (getActualBatchIdDatabaseMeta(parentJob.getJobMeta().getDatabases())==null) {
				throw new KettleException("No valid batch id connection was specified!");
			}
			if (Const.isEmpty(batchIdTable)) {
				throw new KettleException("No batch id table was specified!");
			}
			if (getActualBatchLoggingDatabaseMeta(parentJob.getJobMeta().getDatabases())==null) {
				throw new KettleException("No valid batch logging connection was specified!");
			}
			if (Const.isEmpty(batchLoggingTable)) {
				throw new KettleException("No batch logging table was specified!");
			}
			if (operationType==null) {
				throw new KettleException("No operation type was specified!");
			}

			// What operation type are we talking about?
			//
			switch(operationType) {
			case START_OF_JOB : handleJobStart(); break;
			case JOB_SUCCESS : handleJobSuccess(); break;
			case JOB_FAILURE : handleJobFailure(); break;
			default: 
				throw new KettleException("Unknown operation type ["+operationType+"] encountered!");
			}
			
			// All went well..
			//
			result.setNrErrors(0);
			result.setResult(true);
			
		} catch(Exception e) {
			log.logError(BaseMessages.getString(PKG, "KjubeConfigurator.Exception.UnexpectedError"), e);
			result.setNrErrors(1);
			result.setResult(false);
		}
		
		return result;
	}
	

    private void handleJobStart() throws KettleException {
    	try {
    		// First determine the batch ID for this 
    		
    	} catch(Exception e) {
    		throw new KettleException("Unable to handle the job start operation", e);
    	}
	}

	private void handleJobSuccess() throws KettleException {
    	try {
    		
    	} catch(Exception e) {
    		throw new KettleException("Unable to handle the job success operation", e);
    	}
	}

	private void handleJobFailure() throws KettleException {
    	try {
    		
    	} catch(Exception e) {
    		throw new KettleException("Unable to handle the job end operation", e);
    	}
	}
	
	
	

	public boolean evaluates()
	{
		return true;
	}
    
    public boolean isUnconditional()
    {
        return false;
    }

	/**
	 * @return the batchIdConnection
	 */
	public DatabaseMeta getBatchIdConnection() {
		return batchIdConnection;
	}

	/**
	 * @param batchIdConnection the batchIdConnection to set
	 */
	public void setBatchIdConnection(DatabaseMeta batchIdConnection) {
		this.batchIdConnection = batchIdConnection;
	}

	/**
	 * @return the batchIdSchema
	 */
	public String getBatchIdSchema() {
		return batchIdSchema;
	}

	/**
	 * @param batchIdSchema the batchIdSchema to set
	 */
	public void setBatchIdSchema(String batchIdSchema) {
		this.batchIdSchema = batchIdSchema;
	}

	/**
	 * @return the batchIdTable
	 */
	public String getBatchIdTable() {
		return batchIdTable;
	}

	/**
	 * @param batchIdTable the batchIdTable to set
	 */
	public void setBatchIdTable(String batchIdTable) {
		this.batchIdTable = batchIdTable;
	}

	/**
	 * @return the batchLoggingConnection
	 */
	public DatabaseMeta getBatchLoggingConnection() {
		return batchLoggingConnection;
	}

	/**
	 * @param batchLoggingConnection the batchLoggingConnection to set
	 */
	public void setBatchLoggingConnection(DatabaseMeta batchLoggingConnection) {
		this.batchLoggingConnection = batchLoggingConnection;
	}

	/**
	 * @return the batchLoggingSchema
	 */
	public String getBatchLoggingSchema() {
		return batchLoggingSchema;
	}

	/**
	 * @param batchLoggingSchema the batchLoggingSchema to set
	 */
	public void setBatchLoggingSchema(String batchLoggingSchema) {
		this.batchLoggingSchema = batchLoggingSchema;
	}

	/**
	 * @return the batchLoggingTable
	 */
	public String getBatchLoggingTable() {
		return batchLoggingTable;
	}

	/**
	 * @param batchLoggingTable the batchLoggingTable to set
	 */
	public void setBatchLoggingTable(String batchLoggingTable) {
		this.batchLoggingTable = batchLoggingTable;
	}

	/**
	 * @return the periodicity
	 */
	public String getPeriodicity() {
		return periodicity;
	}

	/**
	 * @param periodicity the periodicity to set
	 */
	public void setPeriodicity(String periodicity) {
		this.periodicity = periodicity;
	}

	/**
	 * @return the operationType
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	/**
	 * @param operationType the operationType to set
	 */
	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	/**
	 * @return the batchIdVariableName
	 */
	public String getBatchIdVariableName() {
		return batchIdVariableName;
	}

	/**
	 * @param batchIdVariableName the batchIdVariableName to set
	 */
	public void setBatchIdVariableName(String batchIdVariableName) {
		this.batchIdVariableName = batchIdVariableName;
	}

	/**
	 * @return the batchIdConnectionName
	 */
	public String getBatchIdConnectionName() {
		return batchIdConnectionName;
	}

	/**
	 * @param batchIdConnectionName the batchIdConnectionName to set
	 */
	public void setBatchIdConnectionName(String batchIdConnectionName) {
		this.batchIdConnectionName = batchIdConnectionName;
	}

	/**
	 * @return the batchLoggingConnectionName
	 */
	public String getBatchLoggingConnectionName() {
		return batchLoggingConnectionName;
	}

	/**
	 * @param batchLoggingConnectionName the batchLoggingConnectionName to set
	 */
	public void setBatchLoggingConnectionName(String batchLoggingConnectionName) {
		this.batchLoggingConnectionName = batchLoggingConnectionName;
	}
    
}