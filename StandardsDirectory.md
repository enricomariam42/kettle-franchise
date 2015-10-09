# Standard directory structure #
KFF proposes the following directory structure (all directory names lower case):

Root directory and subdirectories

```
* /kff               --> kff root
* /kff/projects      --> contains your ETL code
* /kff/reusable      --> contains re-usable pieces of ETL code
* /kff/software      --> contains different versions of (PDI) software
* /kff/documentation --> contains kff documentation
```

## Project directory ##
The following standard applies to the project directory.

**Each data integration project should be included in a separate sub-folder**<br>
Standard set-up expects a folder per customer and sub-folders per project.<br>
<br>
<pre><code>* /kff/projects/customer_name/project_name/<br>
</code></pre>

Example<br>
<pre><code>* /kff/projects/coca-cola/production_datamart/<br>
* /kff/projects/coca-cola/purchasing_datamart/<br>
* /kff/projects/pepsi/datawarehouse/<br>
* /kff/projects/nike/customer_data_quality/<br>
* /kff/projects/nike/retail_finance_eai/<br>
</code></pre>

<b>Each project directory has the following sub-directories:</b>

<pre><code>* /kff/pr.. /cfg      --&gt; contains configuration files as kettle.properties, etc ..<br>
* /kff/pr.. /code     --&gt; contains your .ktr, .kjb and other code<br>
* /kff/pr.. /data/in  --&gt; incoming data files<br>
* /kff/pr.. /data/out --&gt; outgoing data files<br>
* /kff/pr.. /ddl      --&gt; SQL scripts and DDL<br>
* /kff/pr.. /dmp      --&gt; database dump files<br>
* /kff/pr.. /tmp      --&gt; temporary files<br>
</code></pre>

<h2>Reusable directory</h2>
The "reusable" directory contains pieces of reusable PDI coding.<br>
<br>
Examples:<br>
<pre><code>* /kff/reusable/cookbook        --&gt; Roland Boumans cookbook<br>
* /kff/reusable/kff-logging     --&gt; kff logging framework<br>
* /kff/reusable/kff-archiver    --&gt; kff etl-code archiver<br>
* /kff/reusable/norman-mailer   --&gt; kff mass mailer application<br>
</code></pre>

<h2>Software</h2>
The software directory should include all the software you need for your project(s).<br>
<br>
Example:<br>
<pre><code>* /kff/software/jre/            --&gt; Different versions of JRE you might use<br>
* /kff/software/pdi/3.2.3       --&gt; PDI version 3.2.3<br>
* /kff/software/pdi/4.0.0       --&gt; PDI version 4.0.0<br>
</code></pre>

<ul><li>We recommend that you include a stable version of your java run time environment in your software directory. The only dependency kettle has is the java run time, therefore if you have a stable JRE, it is worth deploying together with your solution.<br>
</li><li>We also recommend that you store different versions of PDI you are using, one next to the other. This will facilitate upgrade scenarios, where you might be using an older version of PDI in production while you are using the most recent version.