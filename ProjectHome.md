# A framework for rapid deployment of kettle solutions. #

<img src='http://www.kjube.be/images/KFF_KUBUS_160px_v002.png' align='right'>
Kettle (a.k.a Pentaho Data Integration) is the leading open source data integration tool. From it's inception until today kettle has evolved to a powerful data integration platform that can match the likes of proprietary data integration tools as Informatica, Datastage or BODI.  To say it using the Kettle terminology, the Kettle tool set (Spoon, Kettle, Chef, Kitchen, Cookbook, ...) allows you to open any type of data integration restaurant, from fastfood over bistro to a 5 star Michelin restaurant.<br>
<br>
The Kettle Franchising Factory (KFF) adds on top of the existing kettle platform the necessary tools to open multiple data integration restaurants in a rapid, flexible and organised way. KFF allows you to deploy a large series of data integration solutions (multi-customer, multi-solution) in a fully standardized way.<br>
<br>
KFF is composed of:<br>
<ul><li>Kettle plugins<br>
</li><li>re-usable transformations/jobs<br>
</li><li>logging/scheduling framework<br>
</li><li>standards<br>
</li><li>naming conventions<br>
</li><li>best practices for set-up<br>
</li><li>directory structures<br>
</li><li>...</li></ul>

<h1>Quickstart</h1>

<ul><li><a href='QuickStartRunTemplate.md'>Run KFF with the provided template (Real quick start)</a>
</li><li><a href='QuickStartSingleEnvironment.md'>Setting up a single environment (DEV)</a>
</li><li><a href='QuickStartMultipleEnvironments.md'>Setting up a multiple environments (DEV, TST, UAI, PRD)</a></li></ul>

<h1>KFF documentation</h1>

<ol><li><a href='Standards.md'>Standards and conventions</a>
<ol><li><a href='StandardsSoftware.md'>PDI and jre software</a>
</li><li><a href='StandardsDirectory.md'>Directory structure</a>
</li><li><a href='StandardsNaming.md'>Naming conventions</a>
</li><li><a href='StandardsVariables.md'>KFF variables</a>
</li></ol></li><li><a href='Plugin.md'>Plug-ins</a>
<ol><li><a href='PluginDataGrid.md'>Data Grid</a>
</li><li><a href='PluginEnvironmentConfigurator.md'>Environment Configurator</a>
</li></ol></li><li><a href='Templates.md'>Templates</a>
<ol><li><a href='TemplateDatawarehouseProject.md'>Data warehouse project</a>
</li></ol></li><li><a href='Reusable.md'>Reusable kettle coding</a>