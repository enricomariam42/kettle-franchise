/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

 
/*
 * Created on 2-jul-2008
 *
 */

package be.kjube.plugins.season;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


public class SeasonDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = Season.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SeasonMeta input;
	
	private ModifyListener lsMod ;
	private int middle;
	private int margin;
	
	/**
	 * all fields from the previous steps
	 */
	private RowMetaInterface prevFields = null;
    	
	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private ColumnInfo[]	columns;

	private Button	wIgnoreErrors;

	public SeasonDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(SeasonMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, input);
        
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		changed = input.hasChanged();
		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "SeasonDialog.Shell.Title")); //$NON-NLS-1$

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "SeasonDialog.Stepname.Label")); //$NON-NLS-1$
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// IgnoreErrors line
		Label wlIgnoreErrors = new Label(shell, SWT.RIGHT);
		wlIgnoreErrors.setText(BaseMessages.getString(PKG, "SeasonDialog.IgnoreErrors.Label")); //$NON-NLS-1$
 		props.setLook(wlIgnoreErrors);
		FormData fdlIgnoreErrors = new FormData();
		fdlIgnoreErrors.left = new FormAttachment(0, 0);
		fdlIgnoreErrors.right= new FormAttachment(middle, -margin);
		fdlIgnoreErrors.top  = new FormAttachment(wStepname, margin);
		wlIgnoreErrors.setLayoutData(fdlIgnoreErrors);
		wIgnoreErrors=new Button(shell, SWT.CHECK);
 		props.setLook(wIgnoreErrors);
		FormData fdIgnoreErrors = new FormData();
		fdIgnoreErrors.left = new FormAttachment(middle, 0);
		fdIgnoreErrors.top  = new FormAttachment(wStepname, margin);
		fdIgnoreErrors.right= new FormAttachment(100, 0);
		wIgnoreErrors.setLayoutData(fdIgnoreErrors);


		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);
        
		addFields();
		
		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
		setComboValues();
		
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void addFields()
	{
		columns = new ColumnInfo[] { 
				new ColumnInfo(BaseMessages.getString(PKG, "SeasonDialog.Result.Column"), ColumnInfo.COLUMN_TYPE_TEXT, false),
				new ColumnInfo(BaseMessages.getString(PKG, "SeasonDialog.Century.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false),
				new ColumnInfo(BaseMessages.getString(PKG, "SeasonDialog.Year.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false),
				new ColumnInfo(BaseMessages.getString(PKG, "SeasonDialog.SeasonNumber.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false),
		};
		
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "SeasonDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wIgnoreErrors, margin);
		wlFields.setLayoutData(fdlFields);
		
		wFields=new TableView(transMeta, shell, 
				  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
				  columns, 
				  input.getFields().size(),  
				  lsMod,
				  props
				  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wOK, -2*margin);
		wFields.setLayoutData(fdFields);

		setComboValues();		
	}

	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);

				} catch (KettleException e) {
					String msg = BaseMessages.getString(PKG, "SeasonDialog.DoMapping.UnableToFindInput");
					log.logError(toString(), msg);
				}
				String[] prevStepFieldNames = prevFields.getFieldNames();
				if(prevStepFieldNames!=null){
					Arrays.sort(prevStepFieldNames);
	
					for (int i = 1; i < columns.length; i++) {
						columns[i].setComboValues(prevStepFieldNames);
					}
				}
			}
		};
		new Thread(fieldLoader).start();
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		wIgnoreErrors.setSelection(input.isIgnoringErrors());
		
		Table table = wFields.table;
		if (input.getFields().size()>0) table.removeAll();
		for (int i=0;i<input.getFields().size();i++)
		{
			SeasonField field = input.getFields().get(i);
			
			TableItem ti = new TableItem(table, SWT.NONE);
			int col=1;
			ti.setText(col++, Const.NVL(field.getResultField(), ""));
			ti.setText(col++, Const.NVL(field.getCenturiesField(), ""));
			ti.setText(col++, Const.NVL(field.getYearsField(), ""));
			ti.setText(col++, Const.NVL(field.getSeasonNumberField(), ""));
		}

        wFields.setRowNums();
		wFields.optWidth(true);
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;
		stepname = wStepname.getText(); // return value
		
		input.setIgnoringErrors(wIgnoreErrors.getSelection());
		
		int nrfields = wFields.nrNonEmpty();
		input.allocate();
		
		for (int i=0;i<nrfields;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			int col=1;
			String resultField = ti.getText(col++);
			String centuriesField = ti.getText(col++);
			String yearsField = ti.getText(col++);
			String seasonNumberField = ti.getText(col++);

			input.getFields().add( new SeasonField(resultField, centuriesField, yearsField, seasonNumberField) );
		}
		dispose();
	}
}
