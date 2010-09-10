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

package be.kjube.plugins.trim;

import java.util.ArrayList;
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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

import be.kjube.plugins.trim.TrimMeta.TrimType;


public class TrimDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = Trim.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private TrimMeta input;
	
	private ModifyListener lsMod ;
	private int middle;
	private int margin;
	
	/**
	 * all fields from the previous steps
	 */
	private RowMetaInterface prevFields = null;
    
	/**
	 * List of ColumnInfo that should have the previous fields combo box
	 */
	private List<ColumnInfo> fieldColumns = new ArrayList<ColumnInfo>();
	
    private Label wlTrimmingAllFields;
    private Button wTrimmingAllFields;

    private Label wlExcludeFields;
    private Button wExcludeFields;

    private Label wlTrimType;
    private CCombo wTrimType;

	private Label        wlFields;
	private TableView    wFields;
	private FormData     fdlFields, fdFields;

	private String[]	trimOptions;
	
	public TrimDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(TrimMeta)in;
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
		
		trimOptions = new String[] { 
				TrimType.LEFT.getDescription(), 
				TrimType.RIGHT.getDescription(), 
				TrimType.BOTH.getDescription(), 
				TrimType.NONE.getDescription(), 
			}; 
		
		middle = props.getMiddlePct();
		margin = Const.MARGIN;
		
		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "TrimDialog.Shell.Title")); //$NON-NLS-1$

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "TrimDialog.Stepname.Label")); //$NON-NLS-1$
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
		
		// Trim all strings?
		//
		wlTrimmingAllFields=new Label(shell, SWT.RIGHT);
		wlTrimmingAllFields.setText(BaseMessages.getString(PKG, "TrimDialog.TrimmingAllFields.Label"));
		props.setLook(wlTrimmingAllFields);
		FormData fdlTrimmingAllFields = new FormData();
		fdlTrimmingAllFields.left  = new FormAttachment(0, 0);
		fdlTrimmingAllFields.top   = new FormAttachment(wStepname, margin);
		fdlTrimmingAllFields.right = new FormAttachment(middle, -margin);
		wlTrimmingAllFields.setLayoutData(fdlTrimmingAllFields);

		wTrimmingAllFields=new Button(shell, SWT.CHECK);
		wTrimmingAllFields.setToolTipText(BaseMessages.getString(PKG, "TrimDialog.TrimmingAllFields.Tooltip"));
 		props.setLook(wTrimmingAllFields);
		FormData fdTrimmingAllFields = new FormData();
		fdTrimmingAllFields.left  = new FormAttachment(middle, 0);
		fdTrimmingAllFields.top   = new FormAttachment(wStepname, margin);
		fdTrimmingAllFields.right = new FormAttachment(100, 0);
		wTrimmingAllFields.setLayoutData(fdTrimmingAllFields);

		// Trim type for all strings
		//

		wTrimType=new CCombo(shell, SWT.BORDER | SWT.LEFT);
		wTrimType.setToolTipText(BaseMessages.getString(PKG, "TrimDialog.TrimType.Tooltip"));
		wTrimType.setItems(trimOptions);
 		props.setLook(wTrimType);
		FormData fdTrimType = new FormData();
		fdTrimType.left  = new FormAttachment(middle, 0);
		fdTrimType.top   = new FormAttachment(wTrimmingAllFields, margin);
		fdTrimType.right = new FormAttachment(100, 0);
		wTrimType.setLayoutData(fdTrimType);

		wlTrimType=new Label(shell, SWT.RIGHT);
		wlTrimType.setText(BaseMessages.getString(PKG, "TrimDialog.TrimType.Label"));
		props.setLook(wlTrimType);
		FormData fdlTrimType = new FormData();
		fdlTrimType.left  = new FormAttachment(0, 0);
		fdlTrimType.top   = new FormAttachment(wTrimType, 0, SWT.CENTER);
		fdlTrimType.right = new FormAttachment(middle, -margin);
		wlTrimType.setLayoutData(fdlTrimType);

		// Trim all strings?
		//
		wlExcludeFields=new Label(shell, SWT.RIGHT);
		wlExcludeFields.setText(BaseMessages.getString(PKG, "TrimDialog.ExcludeFields.Label"));
		props.setLook(wlExcludeFields);
		FormData fdlExcludeFields = new FormData();
		fdlExcludeFields.left  = new FormAttachment(0, 0);
		fdlExcludeFields.top   = new FormAttachment(wTrimType, margin);
		fdlExcludeFields.right = new FormAttachment(middle, -margin);
		wlExcludeFields.setLayoutData(fdlExcludeFields);

		wExcludeFields=new Button(shell, SWT.CHECK);
		wExcludeFields.setToolTipText(BaseMessages.getString(PKG, "TrimDialog.ExcludeFields.Tooltip"));
 		props.setLook(wExcludeFields);
		FormData fdExcludeFields = new FormData();
		fdExcludeFields.left  = new FormAttachment(middle, 0);
		fdExcludeFields.top   = new FormAttachment(wTrimType, margin);
		fdExcludeFields.right = new FormAttachment(100, 0);
		wExcludeFields.setLayoutData(fdExcludeFields);

		
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wGet=new Button(shell, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
		
		
		setButtonPositions(new Button[] { wOK, wGet, wCancel }, margin, null);
        
		addFields();
		
		SelectionAdapter lsFlag = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	setFlags();
            	input.setChanged();
            }
        };
        wTrimmingAllFields.addSelectionListener(lsFlag);
		wExcludeFields.addSelectionListener(lsFlag);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();    } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );

		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		setFlags();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	private void addFields()
	{
		ColumnInfo[] colinf=new ColumnInfo[] {
			new ColumnInfo(BaseMessages.getString(PKG, "TrimDialog.Fieldname.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{},false),
			new ColumnInfo(BaseMessages.getString(PKG, "TrimDialog.TrimType.Column"), ColumnInfo.COLUMN_TYPE_CCOMBO, trimOptions, false),
		};
		
        // Table with fields
		wlFields=new Label(shell, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "TrimDialog.Fields.Label"));
 		props.setLook(wlFields);
		fdlFields=new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top  = new FormAttachment(wExcludeFields, margin);
		wlFields.setLayoutData(fdlFields);
		
		wFields=new TableView(transMeta, shell, 
				  SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
				  colinf, 
				  Const.isEmpty(input.getFieldName())?0:input.getFieldName().length,  
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
		fieldColumns.add(colinf[0]);
		
	}
	private void setFlags()
	{
		boolean allFields = wTrimmingAllFields.getSelection() && !wExcludeFields.getSelection();
		
		wlFields.setEnabled(!allFields);
		wFields.setEnabled(!allFields);
		if (allFields) {
			wFields.table.setBackground(GUIResource.getInstance().getColorLightGray());
		} else {
			wFields.table.setBackground(GUIResource.getInstance().getColorBackground());
		}
		wGet.setEnabled(!allFields);
		
		wlTrimType.setEnabled(allFields);
		wTrimType.setEnabled(allFields);
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null)
			{
                TableItemInsertListener insertListener = new TableItemInsertListener() 
                {    
            	   public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v)
                        { 
            		        tableItem.setText(2, TrimType.BOTH.getDescription());
                            return v.isString();
                        } 
                    };
                    
                BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, insertListener);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
		}

	}
	private void setComboValues() {
		Runnable fieldLoader = new Runnable() {
			public void run() {
				try {
					prevFields = transMeta.getPrevStepFields(stepname);

				} catch (KettleException e) {
					String msg = BaseMessages.getString(PKG, "TrimDialog.DoMapping.UnableToFindInput");
					log.logError(toString(), msg);
				}
				String[] prevStepFieldNames = prevFields.getFieldNames();
				if(prevStepFieldNames!=null){
					Arrays.sort(prevStepFieldNames);
	
					for (int i = 0; i < fieldColumns.size(); i++) {
						ColumnInfo colInfo = (ColumnInfo) fieldColumns.get(i);
						if(colInfo!=null) colInfo.setComboValues(prevStepFieldNames);
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
		wTrimmingAllFields.setSelection(input.isTrimmingAllFields());
		wTrimType.setText(input.getAllFieldsTrimType().getDescription());
		wExcludeFields.setSelection(input.isExcludingFromTrim());
        
		Table table = wFields.table;
		if (input.getFieldName().length>0) table.removeAll();
		for (int i=0;i<input.getFieldName().length;i++)
		{
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setText(0, ""+(i+1));
			if(input.getFieldName()[i]!=null) ti.setText(1, input.getFieldName()[i]);
			if(input.getFieldTrimType()[i]!=null) ti.setText(2, input.getFieldTrimType()[i].getDescription());
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
		
		input.setTrimmingAllFields( wTrimmingAllFields.getSelection() );
		input.setAllFieldsTrimType( TrimType.getTrimTypeByDescription(wTrimType.getText()) );
		input.setExcludingFromTrim( wExcludeFields.getSelection() );
		
		int nrfields = wFields.nrNonEmpty();
		input.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			TableItem ti = wFields.getNonEmpty(i);
			input.getFieldName()[i] = ti.getText(1);
			input.getFieldTrimType()[i] = TrimType.getTrimTypeByDescription(ti.getText(2));
		}
		dispose();
	}
}
