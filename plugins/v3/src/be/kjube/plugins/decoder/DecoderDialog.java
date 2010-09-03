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

package be.kjube.plugins.decoder;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.widget.LabelComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class DecoderDialog extends BaseStepDialog implements StepDialogInterface
{
	private DecoderMeta input;
	
	private ModifyListener lsMod ;
	private int middle;
	private int margin;
	
	/**
	 * all fields from the previous steps
	 */
	private RowMetaInterface prevFields = null;
	
    private LabelComboVar wConnection;
    private LabelTextVar  wSchema;

    private Label wlDataTableIdField;
    private CCombo wDataTableIdField;

    private Label wlDataKeyField;
    private CCombo wDataKeyField;
    
    private Label wlDataDataField;
    private CCombo wDataDataField;

    private Label wlMaskSourceStep;
    private CCombo wMaskSourceStep;

    private Label wlMaskTableIdField;
    private CCombo wMaskTableIdField;

    private Label wlMaskTableNameField;
    private CCombo wMaskTableNameField;

    private Label wlMaskFieldIdField;
    private CCombo wMaskFieldIdField;

    private Label wlMaskFieldNameField;
    private CCombo wMaskFieldNameField;

    private Label wlMaskFieldTrimField;
    private CCombo wMaskFieldTrimField;

    private Label wlModifyTables;
    private Button wModifyTables;

	protected String[]	prevSteps;

	protected RowMetaInterface	prevMaskFields;

	protected String[]	prevMaskSteps;
    
	public DecoderDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(DecoderMeta)in;
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
		shell.setText(Messages.getString("DecoderDialog.Shell.Title")); //$NON-NLS-1$

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(Messages.getString("DecoderDialog.Stepname.Label")); //$NON-NLS-1$
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
		Control lastControl = wStepname;
		
		// Connection
		//
        wConnection = new LabelComboVar(transMeta, shell, Messages.getString("DecoderDialog.Connection.Label"), Messages.getString("DecoderDialog.Connection.Tooltip", ""));
        props.setLook(wConnection);
        wConnection.addModifyListener(lsMod);
        FormData fdConnection = new FormData();
        fdConnection.left = new FormAttachment(0, 0);
        fdConnection.top = new FormAttachment(lastControl, margin);
        fdConnection.right = new FormAttachment(100, 0);
        wConnection.setLayoutData(fdConnection);
		lastControl = wConnection;
	    
        // the schema
	    //
        wSchema = new LabelTextVar(transMeta, shell, Messages.getString("DecoderDialog.Schema.Label"), Messages.getString("DecoderDialog.Schema.Tooltip", ""));
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
        FormData fdSchema = new FormData();
        fdSchema.left = new FormAttachment(0, 0);
        fdSchema.top = new FormAttachment(lastControl, margin);
        fdSchema.right = new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);
        lastControl = wSchema;
		
		// DATA fields...
		//
		wDataTableIdField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wDataTableIdField.setToolTipText(Messages.getString("DecoderDialog.DataTableIdField.Tooltip"));
 		props.setLook(wDataTableIdField);
		FormData fdDataTableIdField = new FormData();
		fdDataTableIdField.left  = new FormAttachment(middle, 0);
		fdDataTableIdField.top   = new FormAttachment(lastControl, margin);
		fdDataTableIdField.right = new FormAttachment(100, 0);
		wDataTableIdField.setLayoutData(fdDataTableIdField);
		wlDataTableIdField=new Label(shell, SWT.RIGHT);
		wlDataTableIdField.setText(Messages.getString("DecoderDialog.DataTableIdField.Label"));
		props.setLook(wlDataTableIdField);
		FormData fdlDataTableIdField = new FormData();
		fdlDataTableIdField.left  = new FormAttachment(0, 0);
		fdlDataTableIdField.top   = new FormAttachment(wDataTableIdField, 0, SWT.CENTER);
		fdlDataTableIdField.right = new FormAttachment(middle, -margin);
		wlDataTableIdField.setLayoutData(fdlDataTableIdField);

		wDataKeyField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wDataKeyField.setToolTipText(Messages.getString("DecoderDialog.DataKeyField.Tooltip"));
 		props.setLook(wDataKeyField);
		FormData fdDataKeyField = new FormData();
		fdDataKeyField.left  = new FormAttachment(middle, 0);
		fdDataKeyField.top   = new FormAttachment(lastControl, margin);
		fdDataKeyField.right = new FormAttachment(100, 0);
		wDataKeyField.setLayoutData(fdDataKeyField);
		wlDataKeyField=new Label(shell, SWT.RIGHT);
		wlDataKeyField.setText(Messages.getString("DecoderDialog.DataKeyField.Label"));
		props.setLook(wlDataKeyField);
		FormData fdlDataKeyField = new FormData();
		fdlDataKeyField.left  = new FormAttachment(0, 0);
		fdlDataKeyField.top   = new FormAttachment(wDataKeyField, 0, SWT.CENTER);
		fdlDataKeyField.right = new FormAttachment(middle, -margin);
		wlDataKeyField.setLayoutData(fdlDataKeyField);
		lastControl = wDataKeyField;

		wDataDataField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wDataDataField.setToolTipText(Messages.getString("DecoderDialog.DataDataField.Tooltip"));
 		props.setLook(wDataDataField);
		FormData fdDataDataField = new FormData();
		fdDataDataField.left  = new FormAttachment(middle, 0);
		fdDataDataField.top   = new FormAttachment(lastControl, margin);
		fdDataDataField.right = new FormAttachment(100, 0);
		wDataDataField.setLayoutData(fdDataDataField);
		wlDataDataField=new Label(shell, SWT.RIGHT);
		wlDataDataField.setText(Messages.getString("DecoderDialog.DataDataField.Label"));
		props.setLook(wlDataDataField);
		FormData fdlDataDataField = new FormData();
		fdlDataDataField.left  = new FormAttachment(0, 0);
		fdlDataDataField.top   = new FormAttachment(wDataDataField, 0, SWT.CENTER);
		fdlDataDataField.right = new FormAttachment(middle, -margin);
		wlDataDataField.setLayoutData(fdlDataDataField);
		lastControl = wDataDataField;

		// Mask fields
		//
		wMaskSourceStep=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wMaskSourceStep.setToolTipText(Messages.getString("DecoderDialog.MaskSourceStep.Tooltip"));
 		props.setLook(wMaskSourceStep);
		FormData fdMaskSourceStep = new FormData();
		fdMaskSourceStep.left  = new FormAttachment(middle, 0);
		fdMaskSourceStep.top   = new FormAttachment(lastControl, margin);
		fdMaskSourceStep.right = new FormAttachment(100, 0);
		wMaskSourceStep.setLayoutData(fdMaskSourceStep);
		wlMaskSourceStep=new Label(shell, SWT.RIGHT);
		wlMaskSourceStep.setText(Messages.getString("DecoderDialog.MaskSourceStep.Label"));
		props.setLook(wlMaskSourceStep);
		FormData fdlMaskSourceStep = new FormData();
		fdlMaskSourceStep.left  = new FormAttachment(0, 0);
		fdlMaskSourceStep.top   = new FormAttachment(wMaskSourceStep, 0, SWT.CENTER);
		fdlMaskSourceStep.right = new FormAttachment(middle, -margin);
		wlMaskSourceStep.setLayoutData(fdlMaskSourceStep);
		lastControl = wMaskSourceStep;
		
		wMaskTableIdField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wMaskTableIdField.setToolTipText(Messages.getString("DecoderDialog.MaskTableIdField.Tooltip"));
 		props.setLook(wMaskTableIdField);
		FormData fdMaskTableIdField = new FormData();
		fdMaskTableIdField.left  = new FormAttachment(middle, 0);
		fdMaskTableIdField.top   = new FormAttachment(lastControl, margin);
		fdMaskTableIdField.right = new FormAttachment(100, 0);
		wMaskTableIdField.setLayoutData(fdMaskTableIdField);
		wlMaskTableIdField=new Label(shell, SWT.RIGHT);
		wlMaskTableIdField.setText(Messages.getString("DecoderDialog.MaskTableIdField.Label"));
		props.setLook(wlMaskTableIdField);
		FormData fdlMaskTableIdField = new FormData();
		fdlMaskTableIdField.left  = new FormAttachment(0, 0);
		fdlMaskTableIdField.top   = new FormAttachment(wMaskTableIdField, 0, SWT.CENTER);
		fdlMaskTableIdField.right = new FormAttachment(middle, -margin);
		wlMaskTableIdField.setLayoutData(fdlMaskTableIdField);
		lastControl = wMaskTableIdField;

		wMaskTableNameField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wMaskTableNameField.setToolTipText(Messages.getString("DecoderDialog.MaskTableNameField.Tooltip"));
 		props.setLook(wMaskTableNameField);
		FormData fdMaskTableNameField = new FormData();
		fdMaskTableNameField.left  = new FormAttachment(middle, 0);
		fdMaskTableNameField.top   = new FormAttachment(lastControl, margin);
		fdMaskTableNameField.right = new FormAttachment(100, 0);
		wMaskTableNameField.setLayoutData(fdMaskTableNameField);
		wlMaskTableNameField=new Label(shell, SWT.RIGHT);
		wlMaskTableNameField.setText(Messages.getString("DecoderDialog.MaskTableNameField.Label"));
		props.setLook(wlMaskTableNameField);
		FormData fdlMaskTableNameField = new FormData();
		fdlMaskTableNameField.left  = new FormAttachment(0, 0);
		fdlMaskTableNameField.top   = new FormAttachment(wMaskTableNameField, 0, SWT.CENTER);
		fdlMaskTableNameField.right = new FormAttachment(middle, -margin);
		wlMaskTableNameField.setLayoutData(fdlMaskTableNameField);
		lastControl = wMaskTableNameField;

		wMaskFieldIdField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wMaskFieldIdField.setToolTipText(Messages.getString("DecoderDialog.MaskFieldIdField.Tooltip"));
 		props.setLook(wMaskFieldIdField);
		FormData fdMaskFieldIdField = new FormData();
		fdMaskFieldIdField.left  = new FormAttachment(middle, 0);
		fdMaskFieldIdField.top   = new FormAttachment(lastControl, margin);
		fdMaskFieldIdField.right = new FormAttachment(100, 0);
		wMaskFieldIdField.setLayoutData(fdMaskFieldIdField);
		wlMaskFieldIdField=new Label(shell, SWT.RIGHT);
		wlMaskFieldIdField.setText(Messages.getString("DecoderDialog.MaskFieldIdField.Label"));
		props.setLook(wlMaskFieldIdField);
		FormData fdlMaskFieldIdField = new FormData();
		fdlMaskFieldIdField.left  = new FormAttachment(0, 0);
		fdlMaskFieldIdField.top   = new FormAttachment(wMaskFieldIdField, 0, SWT.CENTER);
		fdlMaskFieldIdField.right = new FormAttachment(middle, -margin);
		wlMaskFieldIdField.setLayoutData(fdlMaskFieldIdField);
		lastControl = wMaskFieldIdField;

		wMaskFieldNameField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wMaskFieldNameField.setToolTipText(Messages.getString("DecoderDialog.MaskFieldNameField.Tooltip"));
 		props.setLook(wMaskFieldNameField);
		FormData fdMaskFieldNameField = new FormData();
		fdMaskFieldNameField.left  = new FormAttachment(middle, 0);
		fdMaskFieldNameField.top   = new FormAttachment(lastControl, margin);
		fdMaskFieldNameField.right = new FormAttachment(100, 0);
		wMaskFieldNameField.setLayoutData(fdMaskFieldNameField);
		wlMaskFieldNameField=new Label(shell, SWT.RIGHT);
		wlMaskFieldNameField.setText(Messages.getString("DecoderDialog.MaskFieldNameField.Label"));
		props.setLook(wlMaskFieldNameField);
		FormData fdlMaskFieldNameField = new FormData();
		fdlMaskFieldNameField.left  = new FormAttachment(0, 0);
		fdlMaskFieldNameField.top   = new FormAttachment(wMaskFieldNameField, 0, SWT.CENTER);
		fdlMaskFieldNameField.right = new FormAttachment(middle, -margin);
		wlMaskFieldNameField.setLayoutData(fdlMaskFieldNameField);
		lastControl = wMaskFieldNameField;

    wMaskFieldTrimField=new CCombo(shell, SWT.BORDER | SWT.LEFT);
    wMaskFieldTrimField.setToolTipText(Messages.getString("DecoderDialog.MaskFieldTrimField.Tooltip"));
    props.setLook(wMaskFieldTrimField);
    FormData fdMaskFieldTrimField = new FormData();
    fdMaskFieldTrimField.left  = new FormAttachment(middle, 0);
    fdMaskFieldTrimField.top   = new FormAttachment(lastControl, margin);
    fdMaskFieldTrimField.right = new FormAttachment(100, 0);
    wMaskFieldTrimField.setLayoutData(fdMaskFieldTrimField);
    wlMaskFieldTrimField=new Label(shell, SWT.RIGHT);
    wlMaskFieldTrimField.setText(Messages.getString("DecoderDialog.MaskFieldTrimField.Label"));
    props.setLook(wlMaskFieldTrimField);
    FormData fdlMaskFieldTrimField = new FormData();
    fdlMaskFieldTrimField.left  = new FormAttachment(0, 0);
    fdlMaskFieldTrimField.top   = new FormAttachment(wMaskFieldTrimField, 0, SWT.CENTER);
    fdlMaskFieldTrimField.right = new FormAttachment(middle, -margin);
    wlMaskFieldTrimField.setLayoutData(fdlMaskFieldTrimField);
    lastControl = wMaskFieldTrimField;

		wModifyTables=new Button(shell, SWT.CHECK );
		wModifyTables.setToolTipText(Messages.getString("DecoderDialog.ModifyTables.Tooltip"));
 		props.setLook(wModifyTables);
		FormData fdModifyTables = new FormData();
		fdModifyTables.left  = new FormAttachment(middle, 0);
		fdModifyTables.top   = new FormAttachment(lastControl, margin);
		fdModifyTables.right = new FormAttachment(100, 0);
		wModifyTables.setLayoutData(fdModifyTables);
		wlModifyTables=new Label(shell, SWT.RIGHT);
		wlModifyTables.setText(Messages.getString("DecoderDialog.ModifyTables.Label"));
		props.setLook(wlModifyTables);
		FormData fdlModifyTables = new FormData();
		fdlModifyTables.left  = new FormAttachment(0, 0);
		fdlModifyTables.top   = new FormAttachment(wModifyTables, 0, SWT.CENTER);
		fdlModifyTables.right = new FormAttachment(middle, -margin);
		wlModifyTables.setLayoutData(fdlModifyTables);
		lastControl = wModifyTables;

		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(Messages.getString("System.Button.OK"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(Messages.getString("System.Button.Cancel"));
		
		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);
        
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
		setComboValues(); // get field names in the background
		
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void setComboValues() {
		
		for (DatabaseMeta db : transMeta.getDatabases()) {
			wConnection.add(db.getName());
		}
		
    final String sourceStepName = Const.NVL(wlMaskSourceStep.getText(), "");
		
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);
					
					List<StepMeta> previousSteps = transMeta.findPreviousSteps(stepMeta, true);
					prevSteps = new String[previousSteps.size()];
					for (int i=0;i<prevSteps.length;i++) {
						prevSteps[i] = previousSteps.get(i).getName();
					}
				} catch (KettleException e) {
					String msg = Messages.getString("DecoderDialog.DoMapping.UnableToFindInput");
					log.logError(toString(), msg);
				}
				if (prevFields!=null) {
					final String[] prevStepFieldNames = prevFields.getFieldNames();
					Arrays.sort(prevStepFieldNames);
					Arrays.sort(prevSteps);
	
					
					shell.getDisplay().syncExec(new Runnable() {
					
						public void run() {

							CCombo[] combos = new CCombo[] {
								    wDataTableIdField,
								    wDataKeyField,
								    wDataDataField,
							};
							
							for (CCombo combo : combos) {
								combo.setItems(prevStepFieldNames);
							}
							
							combos = new CCombo[] {
							    wMaskSourceStep,
							};
							
							for (CCombo combo : combos) {
								combo.setItems(prevSteps);
							}
						}
					});
				}
				
				try {
					prevMaskFields = transMeta.getPrevStepFields(sourceStepName);
				} catch (KettleException e) {
					String msg = Messages.getString("DecoderDialog.DoMapping.UnableToFindInput");
					log.logError(toString(), msg);
				}
				if (prevMaskFields!=null) {
					final String[] prevMaskFieldNames = prevMaskFields.getFieldNames();
					Arrays.sort(prevMaskFieldNames);
	
					shell.getDisplay().syncExec(new Runnable() {
					
						public void run() {

							CCombo[] combos = new CCombo[] {
								    wMaskTableIdField,
								    wMaskTableNameField,
								    wMaskFieldIdField,
								    wMaskFieldNameField,
                    wMaskFieldTrimField,
							};
							
							for (CCombo combo : combos) {
								combo.setItems(prevMaskFieldNames);
							}
						}
					});
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
        wConnection.setText(input.getConnection()==null ? Const.NVL(input.getConnectionName(), "") : input.getConnection().getName());
        wSchema.setText(Const.NVL(input.getSchema(), ""));

	    wDataTableIdField.setText(Const.NVL(input.getDataTableIdField(), ""));
	    wDataKeyField.setText(Const.NVL(input.getDataKeyField(), ""));
	    wDataDataField.setText(Const.NVL(input.getDataDataField(), ""));
	    wMaskSourceStep.setText(Const.NVL(input.getMaskSourceStep(), ""));
	    wMaskTableIdField.setText(Const.NVL(input.getMaskTableIdField(), ""));
	    wMaskTableNameField.setText(Const.NVL(input.getMaskTableNameField(), ""));
	    wMaskFieldIdField.setText(Const.NVL(input.getMaskFieldIdField(), ""));
	    wMaskFieldNameField.setText(Const.NVL(input.getMaskFieldNameField(), ""));
      wMaskFieldTrimField.setText(Const.NVL(input.getMaskFieldTrimField(), ""));
	    
	    wModifyTables.setSelection( input.isModifyingTables() );
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
		
    	input.setConnection(  transMeta.findDatabase(wConnection.getText()) );
    	input.setConnectionName( wConnection.getText() );
    	input.setSchema( wSchema.getText() );
    	
	    input.setDataTableIdField( wDataTableIdField.getText() );
	    input.setDataKeyField( wDataKeyField.getText() );
	    input.setDataDataField( wDataDataField.getText() );
	    input.setMaskSourceStep( wMaskSourceStep.getText() );
	    input.setMaskTableIdField( wMaskTableIdField.getText() );
	    input.setMaskTableNameField( wMaskTableNameField.getText() );
	    input.setMaskFieldIdField( wMaskFieldIdField.getText() );
	    input.setMaskFieldNameField( wMaskFieldNameField.getText() );
      input.setMaskFieldTrimField( wMaskFieldTrimField.getText() );

	    input.setModifyingTables( wModifyTables.getSelection() );
	    
	    dispose();
	}
}
