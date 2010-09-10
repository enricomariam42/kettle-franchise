/* 
 * Copyright (c) 2009 kJube.  All rights reserved. 
 */ 


package be.kjube.plugins.configurator;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.LabelText;
import org.pentaho.di.ui.core.widget.LabelTextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import be.kjube.util.Kjube;


/**
 * @since 28-07-2009
 */

public class KjubeConfiguratorDialog extends JobEntryDialog implements JobEntryDialogInterface
{
	private static Class<?> PKG = KjubeConfigurator.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private LabelText wName;

    private FormData fdName;

    private LabelTextVar wCustomerParameterName;
    private LabelTextVar wApplicationParameterName;
    private LabelTextVar wLifecycleParameterName;
    private LabelTextVar wConfigFilePath;

    private Button wOK, wAdd, wCancel;

    private KjubeConfigurator jobEntry;

    private Shell shell;

    private SelectionAdapter lsDef;
    
	private Group gParametersGroup;
	
	private boolean changed;

    public KjubeConfiguratorDialog(Shell parent, JobEntryInterface jobEntry, Repository rep, JobMeta jobMeta)
    {
        super(parent, jobEntry, rep, jobMeta);
        this.jobEntry = (KjubeConfigurator) jobEntry;
        
        if (this.jobEntry.getName() == null)
            this.jobEntry.setName(BaseMessages.getString(PKG, "KjubeConfiguratorDialog.Name.Default"));
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
        shell.setText(BaseMessages.getString(PKG, "KjubeConfiguratorDialog.Title"));

        int margin = Const.MARGIN;

        // Job entry name line
        wName = new LabelText(shell, BaseMessages.getString(PKG, "KjubeConfiguratorDialog.Name.Label"), BaseMessages.getString(PKG, "KjubeConfiguratorDialog.Name.Tooltip"));
        wName.addModifyListener(lsMod);
        fdName = new FormData();
        fdName.top = new FormAttachment(0, 0);
        fdName.left = new FormAttachment(0, 0);
        fdName.right = new FormAttachment(100, 0);
        wName.setLayoutData(fdName);
        
		
	     // ////////////////////////
	     // START OF SERVER SETTINGS GROUP///
	     // /
	    gParametersGroup = new Group(shell, SWT.SHADOW_NONE);
	    props.setLook(gParametersGroup);
	    gParametersGroup.setText(BaseMessages.getString(PKG, "KjubeConfiguratorDialog.Parameters.Group.Label"));

	    FormLayout gParametersLayout = new FormLayout();
	    gParametersLayout.marginWidth = 10;
	    gParametersLayout.marginHeight = 10;
	    gParametersGroup.setLayout(gParametersLayout);

        // ServerName line
        wCustomerParameterName = new LabelTextVar(jobMeta, gParametersGroup, BaseMessages.getString(PKG, "KjubeConfiguratorDialog.CustomerParameter.Label"), BaseMessages.getString(PKG, "KjubeConfiguratorDialog.CustomerParameter.Tooltip", Kjube.DEFAULT_CUSTOMER_PARAMETER));
        props.setLook(wCustomerParameterName);
        wCustomerParameterName.addModifyListener(lsMod);
        FormData fdCustomerParameterName = new FormData();
        fdCustomerParameterName.left = new FormAttachment(0, 0);
        fdCustomerParameterName.top = new FormAttachment(0, 0);
        fdCustomerParameterName.right = new FormAttachment(100, 0);
        wCustomerParameterName.setLayoutData(fdCustomerParameterName);
        
        // UserName line
        wApplicationParameterName = new LabelTextVar(jobMeta, gParametersGroup, BaseMessages.getString(PKG, "KjubeConfiguratorDialog.ApplicationParameter.Label"), BaseMessages.getString(PKG, "KjubeConfiguratorDialog.ApplicationParameter.Tooltip", Kjube.DEFAULT_APPLICATION_PARAMETER));
        props.setLook(wApplicationParameterName);
        wApplicationParameterName.addModifyListener(lsMod);
        FormData fdApplicationParameterName = new FormData();
        fdApplicationParameterName.left = new FormAttachment(0, 0);
        fdApplicationParameterName.top = new FormAttachment(wCustomerParameterName, margin);
        fdApplicationParameterName.right = new FormAttachment(100, 0);
        wApplicationParameterName.setLayoutData(fdApplicationParameterName);

        // Password line
        wLifecycleParameterName = new LabelTextVar(jobMeta, gParametersGroup, BaseMessages.getString(PKG, "KjubeConfiguratorDialog.LifecycleParameter.Label"), BaseMessages.getString(PKG, "KjubeConfiguratorDialog.LifecycleParameter.Tooltip", Kjube.DEFAULT_LIFECYCLE_PARAMETER));
        props.setLook(wLifecycleParameterName);
        wLifecycleParameterName.addModifyListener(lsMod);
        FormData fdLifecycleParameterName = new FormData();
        fdLifecycleParameterName.left = new FormAttachment(0, 0);
        fdLifecycleParameterName.top = new FormAttachment(wApplicationParameterName, margin);
        fdLifecycleParameterName.right = new FormAttachment(100, 0);
        wLifecycleParameterName.setLayoutData(fdLifecycleParameterName);

        
        // Proxy host line
        wConfigFilePath = new LabelTextVar(jobMeta, gParametersGroup, BaseMessages.getString(PKG, "KjubeConfiguratorDialog.ConfigFilePath.Label"), BaseMessages.getString(PKG, "KjubeConfiguratorDialog.ConfigFilePath.Tooltip", Kjube.DEFAULT_CONFIG_FILE_PATH));
        props.setLook(wConfigFilePath);
        wConfigFilePath.addModifyListener(lsMod);
        FormData fdConfigFilePath = new FormData();
        fdConfigFilePath.left 	= new FormAttachment(0, 0);
        fdConfigFilePath.top		= new FormAttachment(wLifecycleParameterName, 2*margin);
        fdConfigFilePath.right	= new FormAttachment(100, 0);
        wConfigFilePath.setLayoutData(fdConfigFilePath);

        
	     FormData fdParametersGroup = new FormData();
	     fdParametersGroup.left = new FormAttachment(0, margin);
	     fdParametersGroup.top = new FormAttachment(wName, margin);
	     fdParametersGroup.right = new FormAttachment(100, -margin);
	     gParametersGroup.setLayoutData(fdParametersGroup);

	     // ///////////////////////////////////////////////////////////
	     // / END OF PARAMETERS GROUP
	     // ///////////////////////////////////////////////////////////
        


        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wAdd = new Button(shell, SWT.PUSH);
        wAdd.setText(BaseMessages.getString(PKG, "KjubeConfiguratorDialog.Button.AddVerifyParameters.Label"));
        wAdd.setToolTipText(BaseMessages.getString(PKG, "KjubeConfiguratorDialog.Button.AddVerifyParameters.Tooltip"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wAdd, wCancel }, margin, gParametersGroup);

        // Add listeners
        //
        wCancel.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { cancel(); }});
        wOK.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { ok(); }});
        wAdd.addListener(SWT.Selection, new Listener() { public void handleEvent(Event e) { addVerifyParameters(); }});
        
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wName.addSelectionListener(lsDef);
        wCustomerParameterName.addSelectionListener(lsDef);
        wApplicationParameterName.addSelectionListener(lsDef);
        wLifecycleParameterName.addSelectionListener(lsDef);
        wConfigFilePath.addSelectionListener(lsDef);

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
        props.setDialogSize(shell, "KjubeConfiguratorDialogDialogSize");
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return jobEntry;
    }

    public void addVerifyParameters() {
    	StringBuffer feedback = new StringBuffer(100);
    	KjubeConfigurator config = new KjubeConfigurator();
    	getInfo(config);
    	
    	// Verify the customer parameter...
    	//
    	String parameter = config.getActualCustomerParameterName();
    	if (Const.indexOfString(parameter, jobMeta.listParameters())<0) {
			// We need to add this parameter!
			//
    		try {
    			jobMeta.addParameterDefinition(parameter, "", BaseMessages.getString(PKG, "KjubeConfiguratorDialog.CustomerParameter.ParamDesc"));
    			feedback.append("Parameter '"+parameter+" was added to the parent job").append(Const.CR);
    		} catch(DuplicateParamException ex) {
    			// IGNORE
    		}
    	} else {
			feedback.append("Parameter '"+parameter+" already exists in the parent job").append(Const.CR);
		}
    	
		// Check the application parameter...
		//
		parameter = config.getActualApplicationParameterName();
    	if (Const.indexOfString(parameter, jobMeta.listParameters())<0) {
			// We need to add this parameter!
			//
			try {
				jobMeta.addParameterDefinition(parameter, "", BaseMessages.getString(PKG, "KjubeConfiguratorDialog.ApplicationParameter.ParamDesc"));
				feedback.append("Parameter '"+parameter+" was added to the parent job").append(Const.CR);
			} catch(DuplicateParamException ex) {
				// IGNORE
			}
    	} else {
			feedback.append("Parameter '"+parameter+" already exists in the parent job").append(Const.CR);
		}

		// Check the lifecycle parameter...
		//
		parameter = config.getActualLifecycleParameterName();
    	if (Const.indexOfString(parameter, jobMeta.listParameters())<0) {
    		// We need to add this parameter!
    		//
    		try {
    			jobMeta.addParameterDefinition(parameter, "", BaseMessages.getString(PKG, "KjubeConfiguratorDialog.LifecycleParameter.ParamDesc"));
    			feedback.append("Parameter '"+parameter+" was added to the parent job").append(Const.CR);
    		} catch(DuplicateParamException ex) {
    			// IGNORE
    		}
    	} else {
    		feedback.append("Parameter '"+parameter+" already exists in the parent job").append(Const.CR);
    	}
		
		EnterTextDialog dialog = new EnterTextDialog(shell, "Feedback", "Verification feedback:", feedback.toString());
		dialog.setReadOnly();
		dialog.open();
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

        wCustomerParameterName.setText(Const.NVL(jobEntry.getCustomerParameterName(), ""));
        wApplicationParameterName.setText(Const.NVL(jobEntry.getApplicationParameterName(), ""));
        wLifecycleParameterName.setText(Const.NVL(jobEntry.getLifecycleParameterName(), ""));
        wConfigFilePath.setText(Const.NVL(jobEntry.getConfigFilePath(), ""));
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
    
    private void getInfo(KjubeConfigurator entry) {
        entry.setCustomerParameterName(wCustomerParameterName.getText());
        entry.setApplicationParameterName(wApplicationParameterName.getText());
        entry.setLifecycleParameterName(wLifecycleParameterName.getText());
        entry.setConfigFilePath(wConfigFilePath.getText());
	}


	public String toString()
    {
        return this.getClass().getName();
    }
}