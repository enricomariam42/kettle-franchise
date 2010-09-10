package be.kjube.plugins.decoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;

public class DecoderTable {
	
	private String tableId;
	
	private String tableName;

	private List<DecoderField> keys;
	
	private List<DecoderField> elements;
	
	private List<DecoderField> appendix;
	
	
	public DecoderTable(String tableId, String tableName, Map<String, DecoderField> decoderFields) throws KettleException {
		
		this.tableId = tableId;
		this.tableName = tableName;
		
		keys = new ArrayList<DecoderField>();
		elements = new ArrayList<DecoderField>();
		appendix = new ArrayList<DecoderField>();

			
		// Extract the various fields...
		//
		for (DecoderField field : decoderFields.values()) {
			switch (field.getFieldType()) {
			case K : 
				keys.add(field); 
				break;
			case E : 
				elements.add(field);
				break;
			case A :
				appendix.add(field);
				break;
			case F : 
				// Unknown key field designation...
				// Ignore for now
				//
				break;
			default:
				// field type is not handled : check your code!
				//
				throw new KettleException("Unhandled decoder type : "+field.getFieldType().getDescription());
			}
		}
		
		// Always add IDCompany and ICLanguage as part of the key
		//
		// keys.add(new DecoderField(tableId, "IDCOMPANY", FieldType.K, keys.size(), DataType.A, 1, new Date()));
		// keys.add(new DecoderField(tableId, "IDLANGUAGE", FieldType.K, keys.size(), DataType.A, 1, new Date()));
		
		Comparator<DecoderField> comparator = new Comparator<DecoderField>() {
			public int compare(DecoderField one, DecoderField two) {
				return new Integer(one.getFieldNr()).compareTo(two.getFieldNr());
			}
		};
		// Now sort the elements and appendix lists on the fieldNr...
		//
		Collections.sort(keys, comparator);
		Collections.sort(elements, comparator);
		Collections.sort(appendix, comparator);	
	}
	
	public String getTableId() {
		return tableId;
	}
	
	/**
	 * @return the elements
	 */
	public List<DecoderField> getElements() {
		return elements;
	}

	/**
	 * @return the appendix
	 */
	public List<DecoderField> getAppendix() {
		return appendix;
	}

	/**
	 * @return the keys
	 */
	public List<DecoderField> getKeys() {
		return keys;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}	
}
