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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

@Step(
		name="TrimStrings",
		image="be/kjube/plugins/images/trim.png",
		tooltip="kJube.Plugins.TrimStrings.Description",
		description="kJube.Plugins.TrimStrings.Name",
		category=StepCategory.CATEGORY_USER_DEFINED,
		categoryDescription="kJube.Category.Name",
		i18nPackageName="be.kjube.plugins" 
	)
public class TrimMeta extends BaseStepMeta implements StepMetaInterface
{
	public enum TrimType {
		
		NONE(Messages.getString("TrimMeta.TrimType.None")), // $NON-NLS-1$ 
		LEFT(Messages.getString("TrimMeta.TrimType.Left")), // $NON-NLS-1$ 
		RIGHT(Messages.getString("TrimMeta.TrimType.Right")), // $NON-NLS-1$ 
		BOTH(Messages.getString("TrimMeta.TrimType.Both")); // $NON-NLS-1$
		
		private String description;
		
		public String getDescription() {
			return description;
		}
		
		private TrimType(String description) {
			this.description=description;
		}

		public static TrimType getTrimTypeByDescription(String text) {
			for (TrimType trimType : values()) {
				if (trimType.description.equalsIgnoreCase(text)) {
					return trimType;
				}
			}
			return null;
		}
	};
	
	/**
	 * Are we trimming all fields? (Default = true)
	 */
	private boolean trimmingAllFields;
	
	/**
	 * The trim type (Default = Right)
	 */
	private TrimType allFieldsTrimType;
	
    /** which fields to display? */
    private String  fieldName[];
    
    /** The trim type to apply per field */
    private TrimType[] fieldTrimType;
    
    private boolean excludingFromTrim;
        
	public TrimMeta()
	{
		super(); // allocate BaseStepMeta
	}


	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode, databases);
    }

	public Object clone()
	{	
        TrimMeta retval = (TrimMeta) super.clone();
        
		return retval;
    }
	
    public void allocate(int nrfields)
    {
        fieldName = new String[nrfields]; 
        fieldTrimType = new TrimType[nrfields]; 
    }
    
    @Override
    public void getFields(RowMetaInterface inputRowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
    	
    	if (!trimmingAllFields) {
	    	for (int i=0;i<fieldName.length;i++) {
	    		String name = fieldName[i];
	    		TrimType trimType = fieldTrimType[i];
	    		
	    		if (trimType!=TrimType.NONE) {
		    		ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta(name);
		    		if (valueMeta!=null && valueMeta.isString()) {
		    			valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL); // always convert Strings to normal storage type
		    		}
		    		valueMeta.setOrigin(origin);
	    		}
	    	}
    	} else {
    		if (allFieldsTrimType!=TrimType.NONE) {
    			for (ValueMetaInterface valueMeta : inputRowMeta.getValueMetaList()) {
    				if (valueMeta.isString()) {
		    			valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL); // always convert Strings to normal storage type
		    			valueMeta.setOrigin(origin);
    				}
    			}
    		}
    	}
    }
   
 
    public boolean isTrimmingAllFields() {
		return trimmingAllFields;
	}
    
    public void setTrimmingAllFields(boolean trimmingAllFields) {
		this.trimmingAllFields = trimmingAllFields;
	}
    
    public TrimType getAllFieldsTrimType() {
		return allFieldsTrimType;
	}
    
    public void setAllFieldsTrimType(TrimType allFieldsTrimType) {
		this.allFieldsTrimType = allFieldsTrimType;
	}
    
    public String[] getFieldName() {
		return fieldName;
	}
    
    public void setFieldName(String[] fieldName) {
		this.fieldName = fieldName;
	}
    
    public TrimType[] getFieldTrimType() {
		return fieldTrimType;
	}
    
    public void setFieldTrimType(TrimType[] fieldTrimType) {
		this.fieldTrimType = fieldTrimType;
	}
    
    
    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
    {
	  try
	    {
		  trimmingAllFields = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "trim_all"));
		  String trimType = XMLHandler.getTagValue(stepnode, "all_fields_trim_type");
		  allFieldsTrimType = Const.isEmpty(trimType) ? TrimType.NONE : TrimType.valueOf( trimType );
		  excludingFromTrim = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "exclude_fields"));

		  Node fields = XMLHandler.getSubNode(stepnode, "fields");
          int nrfields = XMLHandler.countNodes(fields, "field");
          
          allocate(nrfields);
          
          for (int i = 0; i < nrfields; i++)
          {
              Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
              fieldName[i] = XMLHandler.getTagValue(fnode, "name");
              String type = XMLHandler.getTagValue(fnode, "trim_type");
              fieldTrimType[i] = Const.isEmpty(type) ? TrimType.NONE : TrimType.valueOf( type );
          }  
	    }
      catch (Exception e)
      {
          throw new KettleXMLException("It was not possibke to load the Trim metadata from XML", e);
      }
	}
   public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        
        retval.append("      " + XMLHandler.addTagValue("trim_all", trimmingAllFields));
        retval.append("      " + XMLHandler.addTagValue("all_fields_trim_type", allFieldsTrimType.toString()));
        retval.append("      " + XMLHandler.addTagValue("exclude_fields", excludingFromTrim));
        
        if (fieldName!=null) {
	        retval.append("    <fields>" + Const.CR);
	        for (int i = 0; i < fieldName.length; i++)
	        {
	            retval.append("      <field>" + Const.CR);
	            retval.append("        " + XMLHandler.addTagValue("name", fieldName[i]));
	            retval.append("        " + XMLHandler.addTagValue("trim_type", fieldTrimType[i].toString()));
	            retval.append("        </field>" + Const.CR);
	        }
	        retval.append("      </fields>" + Const.CR);
        }

        return retval.toString();
    }

    public void setDefault()
	{
		trimmingAllFields = true;
		excludingFromTrim = true;
		allFieldsTrimType = TrimType.RIGHT;
		fieldName = new String[0];
		fieldTrimType = new TrimType[0];
	}

    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
	        try
	        {
	        	trimmingAllFields = rep.getStepAttributeBoolean(id_step, "trim_all");
	        	String trimType = rep.getStepAttributeString(id_step, "all_fields_trim_type");
	        	allFieldsTrimType = Const.isEmpty(trimType) ? TrimType.NONE : TrimType.valueOf( trimType );
	        	excludingFromTrim = rep.getStepAttributeBoolean(id_step, "exclude_fields");
	        	
	            int nrfields = rep.countNrStepAttributes(id_step, "field_name");
	            allocate(nrfields);
	            
	            for (int i = 0; i < nrfields; i++)
	            {
	                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
	                String type = rep.getStepAttributeString(id_step, i, "field_trim_type");
	                fieldTrimType[i] = Const.isEmpty(type) ? TrimType.NONE : TrimType.valueOf( type );
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unexpected error reading step information from the repository", e);
	        }
	    }
	
	    public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	    {
	        try
	        {
	        	rep.saveStepAttribute(id_transformation, id_step, "trim_all", trimmingAllFields);
	        	rep.saveStepAttribute(id_transformation, id_step, "all_fields_trim_type", allFieldsTrimType.toString());
	        	rep.saveStepAttribute(id_transformation, id_step, "exclude_fields", excludingFromTrim);
	        	
	            if (fieldName!=null) {
		            for (int i = 0; i < fieldName.length; i++)
		            {
		                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
		                rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type", fieldTrimType[i].toString());
		            }
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
	        }
	    }
	
	   public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	   {
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("IfNullMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("IfNullMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			String error_message = "";
	        boolean error_found = false;

            // Starting from selected fields in ...
            for (int i = 0; i < fieldName.length; i++)
            {
                int idx = prev.indexOfValue(fieldName[i]);
                if (idx < 0)
                {
                    error_message += "\t\t" + fieldName[i] + Const.CR;
                    error_found = true;
                }
            }
            if (error_found)
            {
                error_message = Messages.getString("IfNullMeta.CheckResult.FieldsFound", error_message);

                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length > 0)
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("IfNullMeta.CheckResult.AllFieldsFound"), stepinfo);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("IfNullMeta.CheckResult.NoFieldsEntered"), stepinfo);
                    remarks.add(cr);
                }
            }

		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("IfNullMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("IfNullMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Trim(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new TrimData();
	}
	
    public boolean supportsErrorHandling()
    {
        return true;
    }



	/**
	 * @return the excludingFromTrim
	 */
	public boolean isExcludingFromTrim() {
		return excludingFromTrim;
	}



	/**
	 * @param excludingFromTrim the excludingFromTrim to set
	 */
	public void setExcludingFromTrim(boolean excludingFromTrim) {
		this.excludingFromTrim = excludingFromTrim;
	}

}
