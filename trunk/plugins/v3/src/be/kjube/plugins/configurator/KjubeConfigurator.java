/* 
 * Copyright (c) 2009 kJube.  All rights reserved. 
 */ 


package be.kjube.plugins.configurator;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceReference;
import org.w3c.dom.Node;

import be.kjube.util.Kjube;

/**
 * The kJube configurator
 *
 * (c) 2008-2009 kJube
 * @since 23-07-2009
 *
 */

@org.pentaho.di.core.annotations.Job(
		id="KjubeConfigurator",
		categoryDescription="kJube.Category.Name",
		i18nPackageName="be.kjube.plugins",
		image="be/kjube/plugins/images/configurator.png", 
		type = JobEntryType.NONE,
		name="kJube.Plugins.KjubeConfigurator.Name",
		tooltip="kJube.Plugins.KjubeConfigurator.Description"
	)
public class KjubeConfigurator extends JobEntryBase implements Cloneable, JobEntryInterface
{
	
	private String customerParameterName;
	private String applicationParameterName;
	private String lifecycleParameterName;
	private String configFilePath;
	
	public KjubeConfigurator(String name)
	{
		super(name, "");

		setID(-1L);
	    setJobEntryType(JobEntryType.NONE);
	}

	public KjubeConfigurator()
	{
		this("");
	}

	public KjubeConfigurator(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        KjubeConfigurator je = (KjubeConfigurator) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(128);
		
		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("customer_parameter",      customerParameterName));
		retval.append("      ").append(XMLHandler.addTagValue("application_parameter",   applicationParameterName));
		retval.append("      ").append(XMLHandler.addTagValue("lifecycle_parameter",     lifecycleParameterName));
		retval.append("      ").append(XMLHandler.addTagValue("config_file_path",        configFilePath));

		return retval.toString();
	}
	
	  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
	    try
	    {
	        super.loadXML(entrynode, databases, slaveServers);
	      	
	        customerParameterName     = XMLHandler.getTagValue(entrynode, "customer_parameter");
	        applicationParameterName  = XMLHandler.getTagValue(entrynode, "application_parameter");
	        lifecycleParameterName    = XMLHandler.getTagValue(entrynode, "lifecycle_parameter");
	        configFilePath            = XMLHandler.getTagValue(entrynode, "config_file_path");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry from XML node", xe);
		}
	}


	  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
	    try
	    {
	        super.loadRep(rep, id_jobentry, databases, slaveServers);
	        
	        customerParameterName    = rep.getJobEntryAttributeString(id_jobentry, "customer_parameter");
	        applicationParameterName = rep.getJobEntryAttributeString(id_jobentry, "application_parameter");
	        lifecycleParameterName   = rep.getJobEntryAttributeString(id_jobentry, "lifecycle_parameter");
	        configFilePath           = rep.getJobEntryAttributeString(id_jobentry, "config_file_path");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job) throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "customer_parameter",      customerParameterName);
			rep.saveJobEntryAttribute(id_job, getID(), "application_parameter",   applicationParameterName);
			rep.saveJobEntryAttribute(id_job, getID(), "lifecycle_parameter",     lifecycleParameterName);
			rep.saveJobEntryAttribute(id_job, getID(), "config_file_path",        configFilePath);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'ftp' to the repository for id_job="+id_job, dbe);
		}
	}
	
	public String getActualCustomerParameterName() {
		return Const.isEmpty(customerParameterName) ? Kjube.DEFAULT_CUSTOMER_PARAMETER : customerParameterName;
	}
	
	public String getActualApplicationParameterName() {
		return Const.isEmpty(applicationParameterName) ? Kjube.DEFAULT_APPLICATION_PARAMETER : applicationParameterName;
	}
	
	public String getActualLifecycleParameterName() {
		return Const.isEmpty(lifecycleParameterName) ? Kjube.DEFAULT_LIFECYCLE_PARAMETER : lifecycleParameterName;
	}
	
	public String calculateConfigFilePath() {
		return Const.isEmpty(configFilePath) ? environmentSubstitute(Kjube.DEFAULT_CONFIG_FILE_PATH) : environmentSubstitute(configFilePath);
	}
	
    
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		
		Result result = previousResult;

		try {
			String[] parameters = parentJob.listParameters();
			
			// Check the customer parameter...
			//
			String actualCustomerParameter = getActualCustomerParameterName();
			if (Const.indexOfString(actualCustomerParameter, parameters)<0) {
				throw new KettleException(Messages.getString("KjubeConfigurator.Exception.ParameterNotDefined", actualCustomerParameter));
			}
			String customer =  parentJob.getParameterValue(actualCustomerParameter);
				
			if (Const.isEmpty(customer)) {
				throw new KettleException(Messages.getString("KjubeConfigurator.Exception.NoValueSetForParameter", actualCustomerParameter));
			}
			
			// Check the application parameter...
			//
			String actualApplicationParameter = getActualApplicationParameterName();
			if (Const.indexOfString(actualApplicationParameter, parameters)<0) {
				throw new KettleException(Messages.getString("KjubeConfigurator.Exception.ParameterNotDefined", actualApplicationParameter));
			}
			String application = parentJob.getParameterValue(actualApplicationParameter);
			if (Const.isEmpty(application)) {
				throw new KettleException(Messages.getString("KjubeConfigurator.Exception.NoValueSetForParameter", actualApplicationParameter));
			}

			// Check the lifecycle parameter...
			//
			String actualLifecycleParameter = getActualLifecycleParameterName();
			if (Const.indexOfString(actualLifecycleParameter, parameters)<0) {
				throw new KettleException(Messages.getString("KjubeConfigurator.Exception.ParameterNotDefined", actualLifecycleParameter));
			}
			String lifecycle = parentJob.getParameterValue(actualLifecycleParameter);
			if (Const.isEmpty(lifecycle)) {
				throw new KettleException(Messages.getString("KjubeConfigurator.Exception.NoValueSetForParameter", actualLifecycleParameter));
			}

			// OK, now we have 3 parameters so we can calculate the file path!
			//
			String path = calculateConfigFilePath();
			
			// See if this file exists!!!
			//
			FileObject fileObject = KettleVFS.getFileObject(path);
			
			if (!fileObject.exists()) {
				throw new KettleException(Messages.getString("KjubeConfigurator.Exception.ConfigurationFileNotFound", path, customer, application, lifecycle));
			}
			
			// Now comes the actual work.
			
			// Load this properties file...
			//
			Properties properties = new Properties();
			properties.load(KettleVFS.getInputStream(fileObject));
			
			// set all the keys/value pairs found as variables...
			//
			Enumeration<Object> keys = properties.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = (String) properties.get(key);
				
				if (!Const.isEmpty(key)) {
					parentJob.setVariable(key, value);
					log.logDetailed(toString(), Messages.getString("KjubeConfigurator.Log.SetVariable", key, value));
				}
			}
			
			// All went well..
			//
			result.setNrErrors(0);
			result.setResult(true);
			
		} catch(Exception e) {
			log.logError(toString(), Messages.getString("KjubeConfigurator.Exception.UnexpectedError"), e);
			result.setNrErrors(1);
			result.setResult(false);
		}
		
		return result;
	}
	

    public boolean evaluates()
	{
		return true;
	}
    
    public boolean isUnconditional()
    {
        return false;
    }


    public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
    {
    }

    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta)
    {
      List<ResourceReference> references = super.getResourceDependencies(jobMeta);
      return references;
    }

	/**
	 * @return the customerParameterName
	 */
	public String getCustomerParameterName() {
		return customerParameterName;
	}

	/**
	 * @param customerParameterName the customerParameterName to set
	 */
	public void setCustomerParameterName(String customerParameterName) {
		this.customerParameterName = customerParameterName;
	}

	/**
	 * @return the applicationParameterName
	 */
	public String getApplicationParameterName() {
		return applicationParameterName;
	}

	/**
	 * @param applicationParameterName the applicationParameterName to set
	 */
	public void setApplicationParameterName(String applicationParameterName) {
		this.applicationParameterName = applicationParameterName;
	}

	/**
	 * @return the lifecycleParameterName
	 */
	public String getLifecycleParameterName() {
		return lifecycleParameterName;
	}

	/**
	 * @param lifecycleParameterName the lifecycleParameterName to set
	 */
	public void setLifecycleParameterName(String lifecycleParameterName) {
		this.lifecycleParameterName = lifecycleParameterName;
	}

	/**
	 * @return the configFilePath
	 */
	public String getConfigFilePath() {
		return configFilePath;
	}

	/**
	 * @param configFilePath the configFilePath to set
	 */
	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

    
}