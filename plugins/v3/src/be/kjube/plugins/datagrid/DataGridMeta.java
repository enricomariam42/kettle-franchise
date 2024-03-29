 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package be.kjube.plugins.datagrid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

@Step(
		name = "DataGrid", 
		image = "be/kjube/plugins/images/data-grid.png", 
		tooltip = "kJube.Plugins.DataGrid.Description", 
		description = "kJube.Plugins.DataGrid.Name", 
		category = StepCategory.CATEGORY_USER_DEFINED, 
		categoryDescription = "kJube.Category.Name", 
		i18nPackageName = "be.kjube.plugins"
	)
public class DataGridMeta extends BaseStepMeta implements StepMetaInterface
{	
	private  String currency[];
	private  String decimal[];
	private  String group[];
	
	private  String fieldName[];
	private  String fieldType[];
	private  String fieldFormat[];

	private  int fieldLength[];
	private  int fieldPrecision[];
	
	private  List<List<String>> dataLines;

	public DataGridMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @return Returns the currency.
     */
    public String[] getCurrency()
    {
        return currency;
    }
    
    /**
     * @param currency The currency to set.
     */
    public void setCurrency(String[] currency)
    {
        this.currency = currency;
    }
    
    /**
     * @return Returns the decimal.
     */
    public String[] getDecimal()
    {
        return decimal;
    }
    
    /**
     * @param decimal The decimal to set.
     */
    public void setDecimal(String[] decimal)
    {
        this.decimal = decimal;
    }
    
    /**
     * @return Returns the fieldFormat.
     */
    public String[] getFieldFormat()
    {
        return fieldFormat;
    }
    
    /**
     * @param fieldFormat The fieldFormat to set.
     */
    public void setFieldFormat(String[] fieldFormat)
    {
        this.fieldFormat = fieldFormat;
    }
    
    /**
     * @return Returns the fieldLength.
     */
    public int[] getFieldLength()
    {
        return fieldLength;
    }
    
    /**
     * @param fieldLength The fieldLength to set.
     */
    public void setFieldLength(int[] fieldLength)
    {
        this.fieldLength = fieldLength;
    }
    
    /**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }
    
    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
    
    /**
     * @return Returns the fieldPrecision.
     */
    public int[] getFieldPrecision()
    {
        return fieldPrecision;
    }
    
    /**
     * @param fieldPrecision The fieldPrecision to set.
     */
    public void setFieldPrecision(int[] fieldPrecision)
    {
        this.fieldPrecision = fieldPrecision;
    }
    
    /**
     * @return Returns the fieldType.
     */
    public String[] getFieldType()
    {
        return fieldType;
    }
    
    /**
     * @param fieldType The fieldType to set.
     */
    public void setFieldType(String[] fieldType)
    {
        this.fieldType = fieldType;
    }
    
    /**
     * @return Returns the group.
     */
    public String[] getGroup()
    {
        return group;
    }
    
    /**
     * @param group The group to set.
     */
    public void setGroup(String[] group)
    {
        this.group = group;
    }
    
    public List<List<String>> getDataLines() {
		return dataLines;
	}
    
    public void setDataLines(List<List<String>> dataLines) {
		this.dataLines = dataLines;
	}
     
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
		fieldName      = new String[nrfields];
		fieldType      = new String[nrfields];
		fieldFormat    = new String[nrfields];
		fieldLength    = new int[nrfields];
		fieldPrecision = new int[nrfields];
		currency       = new String[nrfields];
		decimal        = new String[nrfields];
		group          = new String[nrfields];
	}
	
	public Object clone()
	{
		DataGridMeta retval = (DataGridMeta)super.clone();

		int nrfields=fieldName.length;

		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName[i]   = fieldName[i];
			retval.fieldType[i]   = fieldType[i];
			retval.fieldFormat[i] = fieldFormat[i];
			retval.currency[i]    = currency[i];
			retval.decimal[i]     = decimal[i];
			retval.group[i]       = group[i];
			fieldLength[i]        = fieldLength[i]; 
			fieldPrecision[i]     = fieldPrecision[i]; 
		}
		
		retval.setDataLines(new ArrayList<List<String>>());
		for (List<String> line : dataLines) {
			List<String> newLine = new ArrayList<String>();
			newLine.addAll(line);
			retval.getDataLines().add(newLine);
		}
		
		return retval;
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			Node fields = XMLHandler.getSubNode(stepnode, "fields");
			int  nrfields=XMLHandler.countNodes(fields, "field");
	
			allocate(nrfields);
			
			String slength, sprecision;
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				fieldName[i]   = XMLHandler.getTagValue(fnode, "name");
				fieldType[i]   = XMLHandler.getTagValue(fnode, "type");
				fieldFormat[i] = XMLHandler.getTagValue(fnode, "format");
				currency[i]    = XMLHandler.getTagValue(fnode, "currency");
				decimal[i]     = XMLHandler.getTagValue(fnode, "decimal");
				group[i]       = XMLHandler.getTagValue(fnode, "group");
				slength        = XMLHandler.getTagValue(fnode, "length");
				sprecision     = XMLHandler.getTagValue(fnode, "precision");
				
				fieldLength[i]    = Const.toInt(slength, -1);
				fieldPrecision[i] = Const.toInt(sprecision, -1);
			}
			
			Node datanode = XMLHandler.getSubNode(stepnode, "data");
			// NodeList childNodes = datanode.getChildNodes();
			dataLines = new ArrayList<List<String>>();
			
			Node lineNode = datanode.getFirstChild();
			while (lineNode!=null) {
				if ("line".equals(lineNode.getNodeName())) {
					List<String> line = new ArrayList<String>();
					Node itemNode = lineNode.getFirstChild();
					while (itemNode!=null) {
						if ("item".equals(itemNode.getNodeName())) {
							line.add(XMLHandler.getNodeValue(itemNode));
						}
						itemNode = itemNode.getNextSibling();
					}
					/*
					for (int f=0;f<nrfields;f++) {
						Node itemNode = XMLHandler.getSubNodeByNr(lineNode, "item", f);
						String item = XMLHandler.getNodeValue(itemNode);
						line.add(item);
					}
					*/
					dataLines.add(line);
					
				}
				
				lineNode = lineNode.getNextSibling();
			}
        }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void setDefault()
	{
		int i, nrfields=0;
	
		allocate(nrfields);

        DecimalFormat decimalFormat = new DecimalFormat();

		for (i=0;i<nrfields;i++)
		{
			fieldName[i]      = "field"+i;				
			fieldType[i]      = "Number";
			fieldFormat[i]    = "\u00A40,000,000.00;\u00A4-0,000,000.00";
			fieldLength[i]    = 9;
			fieldPrecision[i] = 2;
			currency[i]       = decimalFormat.getDecimalFormatSymbols().getCurrencySymbol();
			decimal[i]        = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getDecimalSeparator() } );
			group[i]          = new String(new char[] { decimalFormat.getDecimalFormatSymbols().getGroupingSeparator() } );
		}

		dataLines = new ArrayList<List<String>>();
	}
	
	public void getFields(RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		for (int i=0;i<fieldName.length;i++) {
			if (!Const.isEmpty(fieldName[i])) {
				int type=ValueMeta.getType(fieldType[i]);
				if (type==ValueMetaInterface.TYPE_NONE) type=ValueMetaInterface.TYPE_STRING;
				ValueMetaInterface v=new ValueMeta(fieldName[i], type);
				v.setLength(fieldLength[i]);
                v.setPrecision(fieldPrecision[i]);
				v.setOrigin(name);
				v.setConversionMask(fieldFormat[i]);
				v.setCurrencySymbol(currency[i]);
				v.setGroupingSymbol(group[i]);
				v.setDecimalSymbol(decimal[i]);
				
				rowMeta.addValueMeta(v);
			}
		}
	}
		
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append("    <fields>").append(Const.CR);
		for (int i=0;i<fieldName.length;i++)
		{
			if (fieldName[i]!=null && fieldName[i].length()!=0)
			{
				retval.append("      <field>").append(Const.CR);
				retval.append("        ").append(XMLHandler.addTagValue("name",      fieldName[i]));
				retval.append("        ").append(XMLHandler.addTagValue("type",      fieldType[i]));
				retval.append("        ").append(XMLHandler.addTagValue("format",    fieldFormat[i]));
				retval.append("        ").append(XMLHandler.addTagValue("currency",  currency[i]));
				retval.append("        ").append(XMLHandler.addTagValue("decimal",   decimal[i]));
				retval.append("        ").append(XMLHandler.addTagValue("group",     group[i]));
				retval.append("        ").append(XMLHandler.addTagValue("length",    fieldLength[i]));
				retval.append("        ").append(XMLHandler.addTagValue("precision", fieldPrecision[i]));
				retval.append("      </field>").append(Const.CR);
			}
		}
		retval.append("    </fields>").append(Const.CR);				
		
		retval.append("    <data>").append(Const.CR);
		for (List<String> line : dataLines) {
			retval.append("      <line> ");
			for (String item : line) {
				retval.append(XMLHandler.addTagValue("item", item, false));
			}
			retval.append(" </line>").append(Const.CR);
		}
		retval.append("    </data>").append(Const.CR);

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, "field_name");
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i]      =       rep.getStepAttributeString (id_step, i, "field_name");
				fieldType[i]      =       rep.getStepAttributeString (id_step, i, "field_type");
	
				fieldFormat[i]    =       rep.getStepAttributeString (id_step, i, "field_format");
				currency[i]       =       rep.getStepAttributeString (id_step, i, "field_currency");
				decimal[i]        =       rep.getStepAttributeString (id_step, i, "field_decimal");
				group[i]          =       rep.getStepAttributeString (id_step, i, "field_group");
				fieldLength[i]    =  (int)rep.getStepAttributeInteger(id_step, i, "field_length");
				fieldPrecision[i] =  (int)rep.getStepAttributeInteger(id_step, i, "field_precision");
			}
			
			int nrLines = (int) rep.getStepAttributeInteger(id_step, "nr_lines");
			dataLines = new ArrayList<List<String>>();
			for (int i=0;i<nrLines;i++) {
				List<String> line = new ArrayList<String>();
				
				for (int f=0;f<nrfields;f++) {
					String item = rep.getStepAttributeString(id_step, i, "item_"+f);
					line.add(item);
				}
				
				dataLines.add(line);
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			for (int i=0;i<fieldName.length;i++)
			{
				if (fieldName[i]!=null && fieldName[i].length()!=0)
				{
					rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      fieldType[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    fieldFormat[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  currency[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   decimal[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     group[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    fieldLength[i]);
					rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]);
				}
			}

			rep.saveStepAttribute(id_transformation, id_step, "nr_lines", dataLines.size());

			for (int i = 0; i < dataLines.size(); i++) {
				List<String> line = dataLines.get(i);
				for (int f = 0; f < line.size(); f++) {
					String item = line.get(f);
					rep.saveStepAttribute(id_transformation, id_step, i, "item_" + f, item);
				}
			}

		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new DataGrid(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new DataGridData();
	}

	public void check(List<CheckResultInterface> results, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface rowMeta, String[] input, String[] output, RowMetaInterface info) {
		
	}
}