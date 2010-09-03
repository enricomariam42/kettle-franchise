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
package be.kjube.plugins.decoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 19-11-2009
 *
 */
public class DecoderData extends BaseStepData implements StepDataInterface
{

	/**
	 * 
	 */
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	
	public int     dataTableIdFieldIndex;
	
	public Map<String, Map<String, DecoderField>> decoderMap;
	public Map<String, String> tableNameMap;
	
	public int	dataKeyFieldIndex;
	public int	dataDataFieldIndex;
	public int	dataLanguageFieldIndex;
	public int  dataLastUpdateFieldIndex;
	public int  dataTimestampFieldIndex;
	public Map<String, DecoderTable>	decoderTables;
	
	public SimpleDateFormat str2dat;
	public SimpleDateFormat str2tim;
	public DatabaseMeta	databaseMeta;
	public Database db;
	
	public Map<String, TableStatements> statementsMap;
	public Map<String, Integer> commitCounterMap;
	
	public int	commitSize;
	public int	dataCompanyFieldIndex;

    
	public DecoderData()
	{
		super();
		
		decoderMap = new HashMap<String, Map<String,DecoderField>>();
		tableNameMap = new HashMap<String, String>();
		decoderTables = new HashMap<String, DecoderTable>();
		commitCounterMap = new HashMap<String, Integer>();
		statementsMap = new HashMap<String, TableStatements>();
		
		str2dat = new SimpleDateFormat("yyyyMMdd"); 
		str2tim = new SimpleDateFormat("yyyyMMddHHmmss"); 
	}
	
	public Date convertStringToDate(String string) throws ParseException {
		if (Const.isEmpty(string)) {
			return null;
		}
		return str2dat.parse(string);
	}
	
	public Date convertStringToTime(String string) throws ParseException {
		if (Const.isEmpty(string)) {
			return null;
		}
		return str2tim.parse(string);
	}

}
