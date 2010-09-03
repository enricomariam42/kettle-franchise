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

package be.kjube.plugins.rejects;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.LabelComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import be.kjube.util.Kjube;


public class RejectsDialog extends BaseStepDialog implements StepDialogInterface
{
	private RejectsMeta input;
	
	private ModifyListener lsMod ;
	private int middle;
	private int margin;
	
	/**
	 * all fields from the previous steps
	 */
	private RowMetaInterface prevFields = null;

    private LabelComboVar wRejectsConnection;
    private LabelTextVar  wRejectsSchema;
    private LabelTextVar  wRejectsTable;

    private LabelComboVar wErrorCountField;
    private LabelComboVar wErrorDescriptionsField;
    private LabelComboVar wErrorFieldsField;
    private LabelComboVar wErrorCodesField;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private Control	lastControl;

	private ColumnInfo[]	keyColumns;
	
	public RejectsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(RejectsMeta)in;
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
		shell.setText(Messages.getString("RejectsDialog.Shell.Title")); //$NON-NLS-1$

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("RejectsDialog.Stepname.Label")); //$NON-NLS-1$
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
		lastControl = wStepname;
		
        wRejectsConnection = new LabelComboVar(transMeta, shell, Messages.getString("RejectsDialog.RejectsConnection.Label"), Messages.getString("RejectsDialog.RejectsConnection.Tooltip", Kjube.DEFAULT_BATCH_ID_CONNECTION));
        props.setLook(wRejectsConnection);
        wRejectsConnection.addModifyListener(lsMod);
        FormData fdRejectsConnection = new FormData();
        fdRejectsConnection.left = new FormAttachment(0, 0);
        fdRejectsConnection.top = new FormAttachment(lastControl, margin);
        fdRejectsConnection.right = new FormAttachment(100, 0);
        wRejectsConnection.setLayoutData(fdRejectsConnection);
        lastControl = wRejectsConnection;
        
        // the batch id schema
	    //
        wRejectsSchema = new LabelTextVar(transMeta, shell, Messages.getString("RejectsDialog.RejectsSchema.Label"), Messages.getString("RejectsDialog.RejectsSchema.Tooltip", Kjube.DEFAULT_BATCH_ID_SCHEMA));
        props.setLook(wRejectsSchema);
        wRejectsSchema.addModifyListener(lsMod);
        FormData fdRejectsSchema = new FormData();
        fdRejectsSchema.left = new FormAttachment(0, 0);
        fdRejectsSchema.top = new FormAttachment(lastControl, margin);
        fdRejectsSchema.right = new FormAttachment(100, 0);
        wRejectsSchema.setLayoutData(fdRejectsSchema);
        lastControl = wRejectsSchema;
        
        // the batch id table        
        //
        wRejectsTable = new LabelTextVar(transMeta, shell, Messages.getString("RejectsDialog.RejectsTable.Label"), Messages.getString("RejectsDialog.RejectsTable.Tooltip", Kjube.DEFAULT_BATCH_ID_TABLE));
        props.setLook(wRejectsTable);
        wRejectsTable.addModifyListener(lsMod);
        FormData fdRejectsTable = new FormData();
        fdRejectsTable.left = new FormAttachment(0, 0);
        fdRejectsTable.top = new FormAttachment(lastControl, margin);
        fdRejectsTable.right = new FormAttachment(100, 0);
        wRejectsTable.setLayoutData(fdRejectsTable);
        lastControl = wRejectsTable;
        
        wErrorCountField = new LabelComboVar(transMeta, shell, Messages.getString("RejectsDialog.ErrorCountField.Label"), Messages.getString("RejectsDialog.ErrorCountField.Tooltip", Kjube.DEFAULT_BATCH_ID_CONNECTION));
        props.setLook(wErrorCountField);
        wErrorCountField.addModifyListener(lsMod);
        FormData fdErrorCountField = new FormData();
        fdErrorCountField.left = new FormAttachment(0, 0);
        fdErrorCountField.top = new FormAttachment(lastControl, margin);
        fdErrorCountField.right = new FormAttachment(100, 0);
        wErrorCountField.setLayoutData(fdErrorCountField);
        lastControl = wErrorCountField;

        wErrorDescriptionsField = new LabelComboVar(transMeta, shell, Messages.getString("RejectsDialog.ErrorDescriptionsField.Label"), Messages.getString("RejectsDialog.ErrorDescriptionsField.Tooltip", Kjube.DEFAULT_BATCH_ID_CONNECTION));
        props.setLook(wErrorDescriptionsField);
        wErrorDescriptionsField.addModifyListener(lsMod);
        FormData fdErrorDescriptionsField = new FormData();
        fdErrorDescriptionsField.left = new FormAttachment(0, 0);
        fdErrorDescriptionsField.top = new FormAttachment(lastControl, margin);
        fdErrorDescriptionsField.right = new FormAttachment(100, 0);
        wErrorDescriptionsField.setLayoutData(fdErrorDescriptionsField);
        lastControl = wErrorDescriptionsField;

        wErrorFieldsField = new LabelComboVar(transMeta, shell, Messages.getString("RejectsDialog.ErrorFieldsField.Label"), Messages.getString("RejectsDialog.ErrorFieldsField.Tooltip", Kjube.DEFAULT_BATCH_ID_CONNECTION));
        props.setLook(wErrorFieldsField);
        wErrorFieldsField.addModifyListener(lsMod);
        FormData fdErrorFieldsField = new FormData();
        fdErrorFieldsField.left = new FormAttachment(0, 0);
        fdErrorFieldsField.top = new FormAttachment(lastControl, margin);
        fdErrorFieldsField.right = new FormAttachment(100, 0);
        wErrorFieldsField.setLayoutData(fdErrorFieldsField);
        lastControl = wErrorFieldsField;

        wErrorCodesField = new LabelComboVar(transMeta, shell, Messages.getString("RejectsDialog.ErrorCodesField.Label"), Messages.getString("RejectsDialog.ErrorCodesField.Tooltip", Kjube.DEFAULT_BATCH_ID_CONNECTION));
        props.setLook(wErrorCodesField);
        wErrorCodesField.addModifyListener(lsMod);
        FormData fdErrorCodesField = new FormData();
        fdErrorCodesField.left = new FormAttachment(0, 0);
        fdErrorCodesField.top = new FormAttachment(lastControl, margin);
        fdErrorCodesField.right = new FormAttachment(100, 0);
        wErrorCodesField.setLayoutData(fdErrorCodesField);
        lastControl = wErrorCodesField;

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCreate=new Button(shell, SWT.PUSH);
		wCreate.setText(Messages.getString("System.Button.SQL"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCreate, wCancel }, margin, null);
        
		addFields();
		
		// Add listeners
		//
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsCreate   = new Listener() { public void handleEvent(Event e) { sql(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wCreate.addListener(SWT.Selection, lsCreate);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
        wRejectsSchema.addSelectionListener(lsDef);
		wRejectsTable.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
;
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);

				} catch (KettleException e) {
					String msg = Messages.getString("TrimDialog.DoMapping.UnableToFindInput");
					log.logError(toString(), msg);
				}
				final String[] prevStepFieldNames = prevFields.getFieldNames();
				if(prevStepFieldNames!=null){
					Arrays.sort(prevStepFieldNames);

					shell.getDisplay().asyncExec(new Runnable() {
						
						public void run() {
							wErrorCountField.setItems(prevStepFieldNames);
							wErrorDescriptionsField.setItems(prevStepFieldNames);
							wErrorFieldsField.setItems(prevStepFieldNames);
							wErrorCodesField.setItems(prevStepFieldNames);

							keyColumns[0].setComboValues(prevStepFieldNames);
						}
					});
				}
			}
		};
		new Thread(fieldLoader).start();
	}

	
	private void addFields()
	{
		keyColumns=new ColumnInfo[] {
			new ColumnInfo(Messages.getString("RejectsDialog.KeyField.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {}, false),
		};
		
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(Messages.getString("RejectsDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(lastControl, margin);
		wlFields.setLayoutData(fdlFields);
		
		wFields=new TableView(transMeta, shell, 
				  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
				  keyColumns, 
				  input.getKeyFields().isEmpty() ? 0 : input.getKeyFields().size(),  
				  lsMod,
				  props
				  );

		fdFields=new FormData();
		fdFields.left  = new FormAttachment(0, 0);
		fdFields.top   = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom= new FormAttachment(wOK, -2*margin);
		wFields.setLayoutData(fdFields);
	}
	
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		for (DatabaseMeta dbMeta : transMeta.getDatabases()) {
			wRejectsConnection.add(dbMeta.getName());
		}
		
		if (!Const.isEmpty(input.getDatabaseName())) {
			wRejectsConnection.setText(input.getDatabaseName());
		} else {
			wRejectsConnection.setText(input.getDatabaseMeta()!=null ? input.getDatabaseMeta().getName() : "");
		}
		
		wRejectsSchema.setText( Const.NVL(input.getRejectsSchemaName(), "") );
		wRejectsTable.setText( Const.NVL(input.getRejectsTableName(), "") );
		
		wErrorCountField.setText( Const.NVL(input.getErrorCountField(), ""));
		wErrorDescriptionsField.setText( Const.NVL(input.getErrorDescriptionsField(), ""));
		wErrorFieldsField.setText( Const.NVL(input.getErrorFieldsField(), ""));
		wErrorCodesField.setText( Const.NVL(input.getErrorCodesField(), ""));
        
		Table table = wFields.table;
		if (input.getKeyFields().size()>0) table.removeAll();
		for (String keyField : input.getKeyFields()) {
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(1, Const.NVL(keyField, ""));
		}

        wFields.setRowNums();
		wFields.optWidth(true);
		
		setComboValues();
		
		wStepname.selectAll();
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void getInfo(RejectsMeta rejectsMeta) {
    	rejectsMeta.setDatabaseMeta( transMeta.findDatabase(wRejectsConnection.getText()) );
    	rejectsMeta.setDatabaseName( wRejectsConnection.getText() );

    	rejectsMeta.setRejectsSchemaName( wRejectsSchema.getText() );
    	rejectsMeta.setRejectsTableName( wRejectsTable.getText() );
    	
    	rejectsMeta.setErrorCountField( wErrorCountField.getText() );
    	rejectsMeta.setErrorDescriptionsField( wErrorDescriptionsField.getText() );
    	rejectsMeta.setErrorFieldsField( wErrorFieldsField.getText() );
    	rejectsMeta.setErrorCodesField( wErrorCodesField.getText() );
    	
    	rejectsMeta.getKeyFields().clear();
		int nrfields = wFields.nrNonEmpty();
		for (int i=0;i<nrfields;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			rejectsMeta.getKeyFields().add( ti.getText(1) );
		}
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;
		stepname = wStepname.getText(); // return value

		getInfo(input);
		
		dispose();
	}
	
	// Generate code for create table...
	// Conversions done by Database
	//
	private void sql()
	{
		try
		{
			RejectsMeta info = new RejectsMeta();
			getInfo(info);
			
			SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, null);
			if (!sql.hasError())
			{
				if (sql.hasSQL())
				{
					SQLEditor sqledit = new SQLEditor(shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(), sql.getSQL());
					sqledit.open();
				}
				else
				{
					MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION );
					mb.setMessage(Messages.getString("RejectsDialog.NoSQL.DialogMessage"));
					mb.setText(Messages.getString("RejectsDialog.NoSQL.DialogTitle"));
					mb.open(); 
				}
			}
			else
			{
				MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
				mb.setMessage(sql.getError());
				mb.setText(Messages.getString("System.Dialog.Error.Title"));
				mb.open(); 
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, Messages.getString("RejectsDialog.BuildSQLError.DialogTitle"), Messages.getString("RejectsDialog.BuildSQLError.DialogMessage"), ke);
		}
	}

}
