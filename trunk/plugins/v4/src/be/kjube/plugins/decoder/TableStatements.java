package be.kjube.plugins.decoder;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;

public class TableStatements {

	private DecoderTable		decoderTable;
	private String				schemaTable;

	private PreparedStatement	lookupStatement;
	private PreparedStatement	insertStatement;
	private PreparedStatement	updateStatement;

	private RowMetaInterface	lookupRowMeta;
	private RowMetaInterface	insertRowMeta;
	private RowMetaInterface	updateRowMeta;
	
	private String lookupSql;
	private String insertSql;
	private String updateSql;
	
	private List<Integer> lookupIndexes;
	private List<Integer> insertIndexes;
	private List<Integer> updateIndexes;

	public TableStatements(DecoderTable decoderTable) {
		this.decoderTable = decoderTable;
		lookupIndexes = new ArrayList<Integer>();
		insertIndexes = new ArrayList<Integer>();
		updateIndexes = new ArrayList<Integer>();
	}

	/**
	 * @return the decoderTable
	 */
	public DecoderTable getDecoderTable() {
		return decoderTable;
	}

	/**
	 * @param decoderTable
	 *            the decoderTable to set
	 */
	public void setDecoderTable(DecoderTable decoderTable) {
		this.decoderTable = decoderTable;
	}

	/**
	 * @return the schemaTable
	 */
	public String getSchemaTable() {
		return schemaTable;
	}

	/**
	 * @param schemaTable
	 *            the schemaTable to set
	 */
	public void setSchemaTable(String schemaTable) {
		this.schemaTable = schemaTable;
	}

	/**
	 * @return the lookupStatement
	 */
	public PreparedStatement getLookupStatement() {
		return lookupStatement;
	}

	/**
	 * @param lookupStatement
	 *            the lookupStatement to set
	 */
	public void setLookupStatement(PreparedStatement lookupStatement) {
		this.lookupStatement = lookupStatement;
	}

	/**
	 * @return the insertStatement
	 */
	public PreparedStatement getInsertStatement() {
		return insertStatement;
	}

	/**
	 * @param insertStatement
	 *            the insertStatement to set
	 */
	public void setInsertStatement(PreparedStatement insertStatement) {
		this.insertStatement = insertStatement;
	}

	/**
	 * @return the updateStatement
	 */
	public PreparedStatement getUpdateStatement() {
		return updateStatement;
	}

	/**
	 * @param updateStatement
	 *            the updateStatement to set
	 */
	public void setUpdateStatement(PreparedStatement updateStatement) {
		this.updateStatement = updateStatement;
	}

	/**
	 * @return the lookupRowMeta
	 */
	public RowMetaInterface getLookupRowMeta() {
		return lookupRowMeta;
	}

	/**
	 * @param lookupRowMeta
	 *            the lookupRowMeta to set
	 */
	public void setLookupRowMeta(RowMetaInterface lookupRowMeta) {
		this.lookupRowMeta = lookupRowMeta;
	}

	/**
	 * @return the insertRowMeta
	 */
	public RowMetaInterface getInsertRowMeta() {
		return insertRowMeta;
	}

	/**
	 * @param insertRowMeta
	 *            the insertRowMeta to set
	 */
	public void setInsertRowMeta(RowMetaInterface insertRowMeta) {
		this.insertRowMeta = insertRowMeta;
	}

	/**
	 * @return the updateRowMeta
	 */
	public RowMetaInterface getUpdateRowMeta() {
		return updateRowMeta;
	}

	/**
	 * @param updateRowMeta
	 *            the updateRowMeta to set
	 */
	public void setUpdateRowMeta(RowMetaInterface updateRowMeta) {
		this.updateRowMeta = updateRowMeta;
	}

	/**
	 * @return the lookupSql
	 */
	public String getLookupSql() {
		return lookupSql;
	}

	/**
	 * @param lookupSql the lookupSql to set
	 */
	public void setLookupSql(String lookupSql) {
		this.lookupSql = lookupSql;
	}

	/**
	 * @return the insertSql
	 */
	public String getInsertSql() {
		return insertSql;
	}

	/**
	 * @param insertSql the insertSql to set
	 */
	public void setInsertSql(String insertSql) {
		this.insertSql = insertSql;
	}

	/**
	 * @return the updateSql
	 */
	public String getUpdateSql() {
		return updateSql;
	}

	/**
	 * @param updateSql the updateSql to set
	 */
	public void setUpdateSql(String updateSql) {
		this.updateSql = updateSql;
	}

	/**
	 * @return the lookupIndexes
	 */
	public List<Integer> getLookupIndexes() {
		return lookupIndexes;
	}

	/**
	 * @param lookupIndexes the lookupIndexes to set
	 */
	public void setLookupIndexes(List<Integer> lookupIndexes) {
		this.lookupIndexes = lookupIndexes;
	}

	/**
	 * @return the insertIndexes
	 */
	public List<Integer> getInsertIndexes() {
		return insertIndexes;
	}

	/**
	 * @param insertIndexes the insertIndexes to set
	 */
	public void setInsertIndexes(List<Integer> insertIndexes) {
		this.insertIndexes = insertIndexes;
	}

	/**
	 * @return the updateIndexes
	 */
	public List<Integer> getUpdateIndexes() {
		return updateIndexes;
	}

	/**
	 * @param updateIndexes the updateIndexes to set
	 */
	public void setUpdateIndexes(List<Integer> updateIndexes) {
		this.updateIndexes = updateIndexes;
	}	

}
