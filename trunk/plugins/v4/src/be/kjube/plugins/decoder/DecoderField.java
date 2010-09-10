package be.kjube.plugins.decoder;

import java.util.Date;

public class DecoderField {

	private String tableId;

	private String fieldId;
	private String fieldName;
	
	private FieldType fieldType;
	private int fieldNr;
	private DataType dataType;
	private int length;
	
	private Date timeStamp;

  private int fieldTrimType;

	
	/**
	 * @param tableId
	 * @param fieldId
	 * @param fieldType
	 * @param fieldNr
	 * @param dataType
	 * @param length
	 * @param timeStamp
	 */
	public DecoderField(String tableId, String fieldId, String fieldName, FieldType fieldType, int fieldNr, DataType dataType, int length, Date timeStamp) {
		this.tableId = tableId;
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldNr = fieldNr;
		this.dataType = dataType;
		this.length = length;
		this.timeStamp = timeStamp;
	}
	
	/**
	 * @param tableId
	 * @param fieldId
	 * @param fieldType
	 * @param fieldNr
	 * @param dataType
	 * @param length
	 * @param timeStamp
	 */
	public DecoderField(String tableId, String fieldId, String fieldName, String fieldType, int fieldTrimType, int fieldNr, String dataType, int length, Date timeStamp) {
		this.tableId = tableId;
		this.fieldId = fieldId;
		this.fieldName = fieldName;
		this.fieldType = FieldType.valueOf(fieldType);
		this.fieldTrimType = fieldTrimType;
		this.fieldNr = fieldNr;
		this.dataType = DataType.valueOf(dataType);
		this.length = length;
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return fieldType.getDescription()+"-"+fieldNr+" "+tableId+"."+fieldId+"("+dataType.getDescription()+")";
	}

	/**
	 * @return the tableId
	 */
	public String getTableId() {
		return tableId;
	}

	/**
	 * @param tableId the tableId to set
	 */
	public void setTableId(String tableId) {
		this.tableId = tableId;
	}

	/**
	 * @return the fieldId
	 */
	public String getFieldId() {
		return fieldId;
	}

	/**
	 * @param fieldId the fieldId to set
	 */
	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	/**
	 * @return the fieldType
	 */
	public FieldType getFieldType() {
		return fieldType;
	}

	/**
	 * @param fieldType the fieldType to set
	 */
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	/**
	 * @return the fieldNr
	 */
	public int getFieldNr() {
		return fieldNr;
	}

	/**
	 * @param fieldNr the fieldNr to set
	 */
	public void setFieldNr(int fieldNr) {
		this.fieldNr = fieldNr;
	}

	/**
	 * @return the dataType
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return the timeStamp
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

  /**
   * @return the fieldTrimType
   */
  public int getFieldTrimType() {
    return fieldTrimType;
  }

  /**
   * @param fieldTrimType the fieldTrimType to set
   */
  public void setFieldTrimType(int fieldTrimType) {
    this.fieldTrimType = fieldTrimType;
  }

	
	
}
