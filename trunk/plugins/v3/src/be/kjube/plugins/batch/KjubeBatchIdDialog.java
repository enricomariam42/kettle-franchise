/* 
 * Copyright (c) 2009 kJube.  All rights reserved. 
 */ 


package be.kjube.plugins.batch;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelComboVar;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import be.kjube.plugins.batch.KjubeBatchId.OperationType;
import be.kjube.util.Kjube;


/**
 * This dialog allows you to edit the FTP Get job entry settings. 
 * 
 * @author Matt
 * @since 19-06-2003
 */

public class KjubeBatchIdDialog extends JobEntryDialog implements JobEntryDialogInterface
{
    private LabelText wName;

    private LabelComboVar wBatchIdConnection;
    private LabelTextVar  wBatchIdSchema;
    private LabelTextVar  wBatchIdTable;

    private LabelComboVar wBatchLoggingConnection;
    private LabelTextVar  wBatchLoggingSchema;
    private LabelTextVar  wBatchLoggingTable;

	private LabelComboVar wPeriodicity;
	private LabelComboVar wOperationType;
	private LabelTextVar  wBatchIdVariable;

    private Button wOK, wSQL, wCancel;

    private KjubeBatchId jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
	private boolean changed;
    

    public KjubeBatchIdDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntry, rep, jobMeta);
        this.jobEntry = (KjubeBatchId) jobEntry;
        
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(Messages.getString("KjubeBatchIdDialog.Name.Default"));
    }


    public JobEntryInterface open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, props.getJobsDialogStyle());
        props.setLook(shell);
        JobDialog.setShellImage(shell, jobEntry);

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                jobEntry.setChanged();
            }
        };
        changed = jobEntry.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.getString("KjubeBatchIdDialog.Title"));

        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, Messages.getString("KjubeBatchIdDialog.Name.Label"), Messages.getString("KjubeBatchIdDialog.Name.Tooltip"));
        wName.addModifyListener(lsMod);
        FormData fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
		
	    ////////////////////////////////////////////////:
	    // 
	    // The batch id connection
        //
	    Group gBatchIdGroup = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(gBatchIdGroup);
	    gBatchIdGroup.setText(Messages.getString("KjubeBatchIdDialog.BatchId.Group.Label"));

	    FormLayout gBatchIdLayout = new FormLayout();
	    gBatchIdLayout.marginWidth = 10;
	    gBatchIdLayout.marginHeight = 10;
	    gBatchIdGroup.setLayout(gBatchIdLayout);

        wBatchIdConnection = new LabelComboVar(jobMeta, gBatchIdGroup, Messages.getString("KjubeBatchIdDialog.BatchIdConnection.Label"), Messages.getString("KjubeBatchIdDialog.BatchIdConnection.Tooltip", Kjube.DEFAULT_BATCH_ID_CONNECTION));
        props.setLook(wBatchIdConnection);
        wBatchIdConnection.addModifyListener(lsMod);
        FormData fdBatchIdConnection = new FormData();
        fdBatchIdConnection.left = new FormAttachment(0, 0);
        fdBatchIdConnection.top = new FormAttachment(0, 0);
        fdBatchIdConnection.right = new FormAttachment(100, 0);
        wBatchIdConnection.setLayoutData(fdBatchIdConnection);
	    
        // the batch id schema
	    //
        wBatchIdSchema = new LabelTextVar(jobMeta, gBatchIdGroup, Messages.getString("KjubeBatchIdDialog.BatchIdSchema.Label"), Messages.getString("KjubeBatchIdDialog.BatchIdSchema.Tooltip", Kjube.DEFAULT_BATCH_ID_SCHEMA));
        props.setLook(wBatchIdSchema);
        wBatchIdSchema.addModifyListener(lsMod);
        FormData fdBatchIdSchema = new FormData();
        fdBatchIdSchema.left = new FormAttachment(0, 0);
        fdBatchIdSchema.top = new FormAttachment(wBatchIdConnection, margin);
        fdBatchIdSchema.right = new FormAttachment(100, 0);
        wBatchIdSchema.setLayoutData(fdBatchIdSchema);
        
        // the batch id table        
        //
        wBatchIdTable = new LabelTextVar(jobMeta, gBatchIdGroup, Messages.getString("KjubeBatchIdDialog.BatchIdTable.Label"), Messages.getString("KjubeBatchIdDialog.BatchIdTable.Tooltip", Kjube.DEFAULT_BATCH_ID_TABLE));
        props.setLook(wBatchIdTable);
        wBatchIdTable.addModifyListener(lsMod);
        FormData fdBatchIdTable = new FormData();
        fdBatchIdTable.left = new FormAttachment(0, 0);
        fdBatchIdTable.top = new FormAttachment(wBatchIdSchema, margin);
        fdBatchIdTable.right = new FormAttachment(100, 0);
        wBatchIdTable.setLayoutData(fdBatchIdTable);

	    FormData fdBatchIdGroup = new FormData();
	    fdBatchIdGroup.left = new FormAttachment(0, margin);
	    fdBatchIdGroup.top = new FormAttachment(wName, margin*2);
	    fdBatchIdGroup.right = new FormAttachment(100, -margin);
	    gBatchIdGroup.setLayoutData(fdBatchIdGroup);

	    /////////////////////////////////////////////////////////////
	    /// END OF THE BATCH ID GROUP
	    /////////////////////////////////////////////////////////////

	    
	    ////////////////////////////////////////////////:
	    // 
	    // The batch logging connection
        //
	    Group gBatchLoggingGroup = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(gBatchLoggingGroup);
	    gBatchLoggingGroup.setText(Messages.getString("KjubeBatchIdDialog.BatchLogging.Group.Label"));

	    FormLayout gBatchLoggingLayout = new FormLayout();
	    gBatchLoggingLayout.marginWidth = 10;
	    gBatchLoggingLayout.marginHeight = 10;
	    gBatchLoggingGroup.setLayout(gBatchLoggingLayout);

        wBatchLoggingConnection = new LabelComboVar(jobMeta, gBatchLoggingGroup, Messages.getString("KjubeBatchIdDialog.BatchLoggingConnection.Label"), Messages.getString("KjubeBatchIdDialog.BatchLoggingConnection.Tooltip", Kjube.DEFAULT_BATCH_LOGGING_CONNECTION));
        props.setLook(wBatchLoggingConnection);
        wBatchLoggingConnection.addModifyListener(lsMod);
        FormData fdBatchLoggingConnection = new FormData();
        fdBatchLoggingConnection.left = new FormAttachment(0, 0);
        fdBatchLoggingConnection.top = new FormAttachment(0, 0);
        fdBatchLoggingConnection.right = new FormAttachment(100, 0);
        wBatchLoggingConnection.setLayoutData(fdBatchLoggingConnection);
	    
        // the batch logging schema
	    //
        wBatchLoggingSchema = new LabelTextVar(jobMeta, gBatchLoggingGroup, Messages.getString("KjubeBatchIdDialog.BatchLoggingSchema.Label"), Messages.getString("KjubeBatchIdDialog.BatchLoggingSchema.Tooltip", Kjube.DEFAULT_BATCH_LOGGING_SCHEMA));
        props.setLook(wBatchLoggingSchema);
        wBatchLoggingSchema.addModifyListener(lsMod);
        FormData fdBatchLoggingSchema = new FormData();
        fdBatchLoggingSchema.left = new FormAttachment(0, 0);
        fdBatchLoggingSchema.top = new FormAttachment(wBatchLoggingConnection, margin);
        fdBatchLoggingSchema.right = new FormAttachment(100, 0);
        wBatchLoggingSchema.setLayoutData(fdBatchLoggingSchema);
        
        // the batch logging table        
        //
        wBatchLoggingTable = new LabelTextVar(jobMeta, gBatchLoggingGroup, Messages.getString("KjubeBatchIdDialog.BatchLoggingTable.Label"), Messages.getString("KjubeBatchIdDialog.BatchLoggingTable.Tooltip", Kjube.DEFAULT_BATCH_LOGGING_TABLE));
        props.setLook(wBatchLoggingTable);
        wBatchLoggingTable.addModifyListener(lsMod);
        FormData fdBatchLoggingTable = new FormData();
        fdBatchLoggingTable.left = new FormAttachment(0, 0);
        fdBatchLoggingTable.top = new FormAttachment(wBatchLoggingSchema, margin);
        fdBatchLoggingTable.right = new FormAttachment(100, 0);
        wBatchLoggingTable.setLayoutData(fdBatchLoggingTable);

	    FormData fdBatchLoggingGroup = new FormData();
	    fdBatchLoggingGroup.left = new FormAttachment(0, margin);
	    fdBatchLoggingGroup.top = new FormAttachment(gBatchIdGroup, margin*2);
	    fdBatchLoggingGroup.right = new FormAttachment(100, -margin);
	    gBatchLoggingGroup.setLayoutData(fdBatchLoggingGroup);
	    Control lastControl = gBatchLoggingGroup;
	    
	    /////////////////////////////////////////////////////////////
	    /// END OF THE BATCH LOGGING GROUP
	    /////////////////////////////////////////////////////////////

	    Group gGeneralGroup = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(gGeneralGroup);
	    gGeneralGroup.setText(Messages.getString("KjubeBatchIdDialog.General.Group.Label"));

	    FormLayout gGeneralLayout = new FormLayout();
	    gGeneralLayout.marginWidth = 10;
	    gGeneralLayout.marginHeight = 10;
	    gGeneralGroup.setLayout(gGeneralLayout);

	    // Now add the periodicity combo box.
	    //
 		wPeriodicity=new LabelComboVar(jobMeta, gGeneralGroup, Messages.getString("KjubeBatchIdDialog.Periodicity.Label"), Messages.getString("KjubeBatchIdDialog.Periodicity.Tooltip"));
		wPeriodicity.addModifyListener(lsMod);
		FormData fdPeriodicity = new FormData();
		fdPeriodicity.top  = new FormAttachment(0, 0);
		fdPeriodicity.left = new FormAttachment(0, 0);
		fdPeriodicity.right= new FormAttachment(100, 0);
		wPeriodicity.setLayoutData(fdPeriodicity);
		wPeriodicity.setItems(KjubeBatchId.PERIODICITY_OPTIONS);
		
	    // Now add the operation type combo box.
	    //
		wOperationType=new LabelComboVar(jobMeta, gGeneralGroup, Messages.getString("KjubeBatchIdDialog.OperationType.Label"), Messages.getString("KjubeBatchIdDialog.OperationType.Tooltip"));
 		props.setLook(wOperationType);
		wOperationType.addModifyListener(lsMod);
		FormData fdOperationType = new FormData();
		fdOperationType.top  = new FormAttachment(wPeriodicity, margin);
		fdOperationType.left = new FormAttachment(0, 0);
		fdOperationType.right= new FormAttachment(100, 0);
		wOperationType.setLayoutData(fdOperationType);
		for (OperationType operationType : OperationType.values()) {
			wOperationType.add(operationType.getDescription());
		}

		// Finally, the batch id variable name...
		//
        // the batch logging schema
	    //
        wBatchIdVariable = new LabelTextVar(jobMeta, gGeneralGroup, Messages.getString("KjubeBatchIdDialog.BatchIdVariable.Label"), Messages.getString("KjubeBatchIdDialog.BatchIdVariable.Tooltip", Kjube.DEFAULT_BATCH_ID_VARIABLE_NAME));
        props.setLook(wBatchIdVariable);
        wBatchIdVariable.addModifyListener(lsMod);
        FormData fdBatchIdVariable = new FormData();
        fdBatchIdVariable.left = new FormAttachment(0, 0);
        fdBatchIdVariable.top = new FormAttachment(wOperationType, margin);
        fdBatchIdVariable.right = new FormAttachment(100, 0);
        wBatchIdVariable.setLayoutData(fdBatchIdVariable);

	    FormData fdGeneralGroup = new FormData();
	    fdGeneralGroup.left = new FormAttachment(0, margin);
	    fdGeneralGroup.top = new FormAttachment(lastControl, margin*2);
	    fdGeneralGroup.right = new FormAttachment(100, -margin);
	    gGeneralGroup.setLayoutData(fdGeneralGroup);
	    lastControl = gGeneralGroup;

        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(Messages.getString("System.Button.OK"));
        wSQL = new Button(shell, SWT.PUSH);
        wSQL.setText(Messages.getString("KjubeBatchIdDialog.Button.SQL.Label"));
        wSQL.setToolTipText(Messages.getString("KjubeBatchIdDialog.Button.SQL.Tooltip"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(Messages.getString("System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wSQL, wCancel }, margin, lastControl);

        // Add listeners
        //
        wCancel.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { cancel(); }});
        wOK.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { ok(); }});
        wSQL.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { sql(); }});
        
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wBatchIdSchema.addSelectionListener(lsDef);
        wBatchIdTable.addSelectionListener(lsDef);
        wBatchLoggingSchema.addSelectionListener(lsDef);
        wBatchLoggingTable.addSelectionListener(lsDef);
        wBatchIdVariable.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        getData();
        
        BaseStepDialog.setSize(shell);

        shell.open();
        props.setDialogSize(shell, "KjubeBatchIdDialogDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }

    public void sql() {
    	KjubeBatchId config = new KjubeBatchId();
    	getInfo(config);

    	/*
    	 * LOG TABLE:
    	 * 
    	 * - Customer
    	 * - Project
    	 * - Lifecycle
    	 * - Batch name
    	 * - Last ID
    	 * - Start Date
    	 * - End Date
    	 * - Status code
    	 * - Status description
    	 */
    	// TODO!
    	//StringBuffer feedback = new StringBuffer(100);
    }


	public void dispose()
    {
        WindowProperty winprop = new WindowProperty(shell);
        props.setScreen(winprop);
        shell.dispose();
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {
        if (jobEntry.getName() != null)
            wName.setText(jobEntry.getName());
        wName.getTextWidget().selectAll();

        wBatchIdConnection.setText(jobEntry.getBatchIdConnection()==null ? Const.NVL(jobEntry.getBatchIdConnectionName(), "") : jobEntry.getBatchIdConnection().getName());
        wBatchIdSchema.setText(Const.NVL(jobEntry.getBatchIdSchema(), ""));
        wBatchIdTable.setText(Const.NVL(jobEntry.getBatchIdTable(), ""));
        
        wBatchLoggingConnection.setText(jobEntry.getBatchLoggingConnection()==null ? Const.NVL(jobEntry.getBatchLoggingConnectionName(), "") : jobEntry.getBatchLoggingConnection().getName());
        wBatchLoggingSchema.setText(Const.NVL(jobEntry.getBatchLoggingSchema(), ""));
        wBatchLoggingTable.setText(Const.NVL(jobEntry.getBatchLoggingTable(), ""));
        
        wPeriodicity.setText( Const.NVL(jobEntry.getPeriodicity(), ""));
        wOperationType.setText( jobEntry.getOperationType()==null ? "" : jobEntry.getOperationType().getDescription() );
        wBatchIdVariable.setText(Const.NVL(jobEntry.getBatchIdVariableName(), ""));
    }

    private void cancel()
    {
        jobEntry.setChanged(changed);
        jobEntry = null;
        dispose();
    }

    private void ok()
    {
        jobEntry.setName(wName.getText());
        
        getInfo(jobEntry);
		
        dispose();
    }
    
    private void getInfo(KjubeBatchId entry) {
    	
    	jobEntry.setBatchIdConnection(  jobMeta.findDatabase(wBatchIdConnection.getText()) );
    	jobEntry.setBatchIdConnectionName( wBatchIdConnection.getText() );
    	jobEntry.setBatchIdSchema(  wBatchIdSchema.getText() );
    	jobEntry.setBatchIdTable(  wBatchIdTable.getText() );
    	
    	jobEntry.setBatchLoggingConnection(  jobMeta.findDatabase(wBatchLoggingConnection.getText()) );
    	jobEntry.setBatchLoggingConnectionName( wBatchLoggingConnection.getText() );
    	jobEntry.setBatchLoggingSchema(  wBatchLoggingSchema.getText() );
    	jobEntry.setBatchLoggingTable(  wBatchLoggingTable.getText() );

    	jobEntry.setPeriodicity( wPeriodicity.getText() );
    	jobEntry.setBatchIdVariableName( wBatchIdVariable.getText() );
    	jobEntry.setOperationType( KjubeBatchId.findOperationTypeByDescription(wOperationType.getText()));
	}


	public String toString()
    {
        return this.getClass().getName();
    }
}