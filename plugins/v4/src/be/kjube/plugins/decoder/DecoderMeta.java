package be.kjube.plugins.decoder;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.w3c.dom.Node;

@Step(
		id="Decoder",
		image="be/kjube/plugins/images/decoder.png",
		description ="kJube.Plugins.Decoder.Description",
		name ="kJube.Plugins.Decoder.Name",
		categoryDescription="kJube.Category.Name",
		i18nPackageName="be.kjube.plugins" 
	)
public class DecoderMeta extends BaseStepMeta implements StepMetaInterface {
	private static Class<?> PKG = Decoder.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String	CODE_CONNECTION						= "connection";
	private static final String	CODE_CONNECTION_NAME				= "connection_name";
	private static final String	CODE_CONNECTION_ID 					= "connection_id";
	private static final String	CODE_SCHEMA                         = "schema";
	
	private static final String	CODE_DATA_TABLE_ID_FIELD			= "data_table_id_field";
	private static final String	CODE_DATA_KEY_FIELD					= "data_key_field";
	private static final String	CODE_DATA_DATA_FIELD				= "data_data_field";
	
	private static final String	CODE_MASK_SOURCE_STEP				= "mask_source_step";
	private static final String	CODE_MASK_TABLE_ID_FIELD			= "mask_table_id_field";
	private static final String	CODE_MASK_TABLE_NAME_FIELD			= "mask_table_name_field";
	private static final String	CODE_MASK_FIELD_ID_FIELD			= "mask_field_id_field";
	private static final String	CODE_MASK_FIELD_NAME_FIELD			= "mask_field_name_field";
  private static final String CODE_MASK_FIELD_TRIM_FIELD      = "mask_field_trim_field";

	private static final String	CODE_MODIFY_TABLES					= "modify_tables";

	private DatabaseMeta connection;
	private String connectionName;
	private String schema;
	
	private String dataTableIdField;
	private String dataKeyField;
	private String dataDataField;
	
	private String maskSourceStep;
	private String maskTableIdField;
	private String maskTableNameField;
	private String maskFieldIdField;
	private String maskFieldNameField;
  private String maskFieldTrimField;
	
	private boolean modifyingTables;
		
	public DecoderMeta() {
		super(); // allocate BaseStepMeta
	}
	
	public void setDefault() {
		
		dataTableIdField = "IDTABLE";
		maskTableIdField = "IDTABLE";
		maskTableNameField = "TABLE_NAME";
		maskTableIdField = "IDFIELD";
		maskTableNameField = "FIELD_NAME";
		
		dataKeyField = "IDCODE";
		dataDataField = "DSCODE";
		
	}

	public String getXML() {

		StringBuffer retval = new StringBuffer();

		retval.append("      ").append(XMLHandler.addTagValue(CODE_CONNECTION,     connection==null ? null : connection.getName()));
		retval.append("      ").append(XMLHandler.addTagValue(CODE_CONNECTION_NAME, connectionName)); // Variables!
		retval.append("      ").append(XMLHandler.addTagValue(CODE_SCHEMA, schema)); 

		retval.append("      " + XMLHandler.addTagValue(CODE_DATA_TABLE_ID_FIELD, dataTableIdField));
		retval.append("      " + XMLHandler.addTagValue(CODE_DATA_KEY_FIELD, dataKeyField));
		retval.append("      " + XMLHandler.addTagValue(CODE_DATA_DATA_FIELD, dataDataField));

		retval.append("      " + XMLHandler.addTagValue(CODE_MASK_SOURCE_STEP, maskSourceStep));
		retval.append("      " + XMLHandler.addTagValue(CODE_MASK_TABLE_ID_FIELD, maskTableIdField));
		retval.append("      " + XMLHandler.addTagValue(CODE_MASK_TABLE_NAME_FIELD, maskTableNameField));
		retval.append("      " + XMLHandler.addTagValue(CODE_MASK_FIELD_ID_FIELD, maskFieldIdField));
    retval.append("      " + XMLHandler.addTagValue(CODE_MASK_FIELD_NAME_FIELD, maskFieldNameField));
    retval.append("      " + XMLHandler.addTagValue(CODE_MASK_FIELD_TRIM_FIELD, maskFieldTrimField));

		retval.append("      " + XMLHandler.addTagValue(CODE_MODIFY_TABLES, modifyingTables));

		return retval.toString();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		  try
		    {
		      connection = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(stepnode, CODE_CONNECTION));
		      connectionName = XMLHandler.getTagValue(stepnode, CODE_CONNECTION_NAME);
		      schema = XMLHandler.getTagValue(stepnode, CODE_SCHEMA);
		      
			  dataTableIdField = XMLHandler.getTagValue(stepnode, CODE_DATA_TABLE_ID_FIELD);
			  dataKeyField = XMLHandler.getTagValue(stepnode, CODE_DATA_KEY_FIELD);
			  dataDataField = XMLHandler.getTagValue(stepnode, CODE_DATA_DATA_FIELD);

			  maskSourceStep = XMLHandler.getTagValue(stepnode, CODE_MASK_SOURCE_STEP);
			  maskTableIdField = XMLHandler.getTagValue(stepnode, CODE_MASK_TABLE_ID_FIELD);
			  maskTableNameField = XMLHandler.getTagValue(stepnode, CODE_MASK_TABLE_NAME_FIELD);
			  maskFieldIdField = XMLHandler.getTagValue(stepnode, CODE_MASK_FIELD_ID_FIELD);
			  maskFieldNameField = XMLHandler.getTagValue(stepnode, CODE_MASK_FIELD_NAME_FIELD);
        maskFieldTrimField = XMLHandler.getTagValue(stepnode, CODE_MASK_FIELD_TRIM_FIELD);

			  modifyingTables = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, CODE_MODIFY_TABLES) );
		    }
	      catch (Exception e)
	      {
	          throw new KettleXMLException("It was not possibke to load the step metadata from XML", e);
	      }

	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
	        try
	        {
	        	connection = rep.loadDatabaseMetaFromStepAttribute(id_step, CODE_CONNECTION_ID, databases);
		        connectionName = rep.getStepAttributeString(id_step, CODE_CONNECTION_NAME);
		        schema = rep.getStepAttributeString(id_step, CODE_SCHEMA);

	        	dataTableIdField = rep.getStepAttributeString(id_step, CODE_DATA_TABLE_ID_FIELD);
	        	dataKeyField = rep.getStepAttributeString(id_step, CODE_DATA_KEY_FIELD);
	        	dataDataField = rep.getStepAttributeString(id_step, CODE_DATA_DATA_FIELD);

	        	maskSourceStep = rep.getStepAttributeString(id_step, CODE_MASK_SOURCE_STEP);
	        	maskTableIdField = rep.getStepAttributeString(id_step, CODE_MASK_TABLE_ID_FIELD);
	        	maskTableNameField = rep.getStepAttributeString(id_step, CODE_MASK_TABLE_NAME_FIELD);
	        	maskFieldIdField = rep.getStepAttributeString(id_step, CODE_MASK_FIELD_ID_FIELD);
	        	maskFieldNameField = rep.getStepAttributeString(id_step, CODE_MASK_FIELD_NAME_FIELD);
            maskFieldTrimField = rep.getStepAttributeString(id_step, CODE_MASK_FIELD_TRIM_FIELD);
	        	
	        	modifyingTables = rep.getStepAttributeBoolean(id_step, CODE_MODIFY_TABLES);
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unexpected error reading step information from the repository", e);
	        }
	    }
	
		public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
	        try
	        {
	        	rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, CODE_CONNECTION_ID, connection);
				rep.saveStepAttribute(id_transformation, id_step, CODE_CONNECTION_NAME,   connectionName);
				rep.saveStepAttribute(id_transformation, id_step, CODE_SCHEMA,   schema);

	        	rep.saveStepAttribute(id_transformation, id_step, CODE_DATA_TABLE_ID_FIELD, dataTableIdField);
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_DATA_KEY_FIELD, dataKeyField);
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_DATA_DATA_FIELD, dataDataField);
	        	
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_MASK_SOURCE_STEP, maskSourceStep);
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_MASK_TABLE_ID_FIELD, maskTableIdField);
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_MASK_TABLE_NAME_FIELD, maskTableNameField);
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_MASK_FIELD_ID_FIELD, maskFieldIdField);
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_MASK_FIELD_NAME_FIELD, maskFieldNameField);
            rep.saveStepAttribute(id_transformation, id_step, CODE_MASK_FIELD_TRIM_FIELD, maskFieldTrimField);
	        	
	        	rep.saveStepAttribute(id_transformation, id_step, CODE_MODIFY_TABLES, modifyingTables);
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
	        }
	    }

	@Override
	public boolean supportsErrorHandling() {
		return true;
	}
	
	@Override
    public boolean excludeFromRowLayoutVerification()
    {
        return true;
    }

	public void check(List<CheckResultInterface> arg0, TransMeta arg1, StepMeta arg2, RowMetaInterface arg3, String[] arg4, String[] arg5, RowMetaInterface arg6) {
		// TODO
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Decoder(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new DecoderData();
	}


	/**
	 * @return the dataTableIdField
	 */
	public String getDataTableIdField() {
		return dataTableIdField;
	}

	/**
	 * @param dataTableIdField the dataTableIdField to set
	 */
	public void setDataTableIdField(String dataTableIdField) {
		this.dataTableIdField = dataTableIdField;
	}

	/**
	 * @return the maskTableIdField
	 */
	public String getMaskTableIdField() {
		return maskTableIdField;
	}

	/**
	 * @param maskTableIdField the maskTableIdField to set
	 */
	public void setMaskTableIdField(String maskTableIdField) {
		this.maskTableIdField = maskTableIdField;
	}

	/**
	 * @return the maskSourceStep
	 */
	public String getMaskSourceStep() {
		return maskSourceStep;
	}

	/**
	 * @param maskSourceStep the maskSourceStep to set
	 */
	public void setMaskSourceStep(String maskSourceStep) {
		this.maskSourceStep = maskSourceStep;
	}

	/**
	 * @return the dataKeyField
	 */
	public String getDataKeyField() {
		return dataKeyField;
	}

	/**
	 * @param dataKeyField the dataKeyField to set
	 */
	public void setDataKeyField(String dataKeyField) {
		this.dataKeyField = dataKeyField;
	}

	/**
	 * @return the dataDataField
	 */
	public String getDataDataField() {
		return dataDataField;
	}

	/**
	 * @param dataDataField the dataDataField to set
	 */
	public void setDataDataField(String dataDataField) {
		this.dataDataField = dataDataField;
	}

	/**
	 * @return the connection
	 */
	public DatabaseMeta getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(DatabaseMeta connection) {
		this.connection = connection;
	}

	/**
	 * @return the connectionName
	 */
	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * @param connectionName the connectionName to set
	 */
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * @param schema the schema to set
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * @return the maskTableNameField
	 */
	public String getMaskTableNameField() {
		return maskTableNameField;
	}

	/**
	 * @param maskTableNameField the maskTableNameField to set
	 */
	public void setMaskTableNameField(String maskTableNameField) {
		this.maskTableNameField = maskTableNameField;
	}

	/**
	 * @return the maskFieldIdField
	 */
	public String getMaskFieldIdField() {
		return maskFieldIdField;
	}

	/**
	 * @param maskFieldIdField the maskFieldIdField to set
	 */
	public void setMaskFieldIdField(String maskFieldIdField) {
		this.maskFieldIdField = maskFieldIdField;
	}

	/**
	 * @return the maskFieldNameField
	 */
	public String getMaskFieldNameField() {
		return maskFieldNameField;
	}

	/**
	 * @param maskFieldNameField the maskFieldNameField to set
	 */
	public void setMaskFieldNameField(String maskFieldNameField) {
		this.maskFieldNameField = maskFieldNameField;
	}

	/**
	 * @return the modifyingTables
	 */
	public boolean isModifyingTables() {
		return modifyingTables;
	}

	/**
	 * @param modifyingTables the modifyingTables to set
	 */
	public void setModifyingTables(boolean modifyingTables) {
		this.modifyingTables = modifyingTables;
	}
	
	@Override
	public StepIOMetaInterface getStepIOMeta() {
		if (ioMeta==null) {
			ioMeta = new StepIOMeta(true, true, false, false, false, false);
			
			ioMeta.addStream(new Stream(StreamType.INFO, null, BaseMessages.getString(PKG, "DecoderMeta.InfoStream."), StreamIcon.INFO, null));
		}
		
		return ioMeta;
	}

	@Override
	public void resetStepIoMeta() {
		// Do nothing
	}

  /**
   * @return the maskFieldTrimField
   */
  public String getMaskFieldTrimField() {
    return maskFieldTrimField;
  }

  /**
   * @param maskFieldTrimField the maskFieldTrimField to set
   */
  public void setMaskFieldTrimField(String maskFieldTrimField) {
    this.maskFieldTrimField = maskFieldTrimField;
  }

}
