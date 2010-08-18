<?xml version="1.0"?>
<!--

kettle-report.xslt - an XSLT transformation that generates HTML documentation
from a Kettle (aka Pentaho Data Integration) transformation or job file.
This is part of kettle-cookbook, a kettle documentation generation framework.

Copyright (C) 2010 Roland Bouman

This library is free software; you can redistribute it and/or modify it under 
the terms of the GNU Lesser General Public License as published by the 
Free Software Foundation; either version 2.1 of the License, or (at your option)
any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY 
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along 
with this library; if not, write to 
the Free Software Foundation, Inc., 
59 Temple Place, Suite 330, 
Boston, MA 02111-1307 USA

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- =========================================================================
    XSLT PARAMETERS
========================================================================== -->
<xsl:param name="short_filename"/>
<xsl:param name="relative-path"/>

<!-- =========================================================================
    XSLT VARIABLES
========================================================================== -->
<xsl:variable name="normalized_short_filename" select="normalize-space($short_filename)"/>
<xsl:variable name="normalized_relative-path" select="normalize-space($relative-path)"/>

<xsl:variable name="file-separator" select="/index/@file-separator"/>

<xsl:variable 
    name="item" 
    select="
        /index/item[
            short_filename/text() = $normalized_short_filename
        and(relative-path/text()  = $normalized_relative-path
        or  relative-path[count(text())=0 and string-length($normalized_relative-path)=0])
        ]
    "
/>
<!--
    This opens the document that is the real subject of the transformation.
    By transforming index.xml, we have the opportunity to query other docs
    as well which may be useful for cross reference reports. 
    Currently, we are not actually using that, but this is why it was built 
    this way.
-->
<xsl:variable 
    name="document" 
    select="
        document($item/uri/text())
    "
/>

<xsl:variable name="item-type" select="local-name($document/*[1])"/>

<xsl:variable name="documentation-root"><xsl:call-template name="get-documentation-root"/></xsl:variable>

<xsl:variable name="quick-links">
    <div class="quicklinks">
        <a href="#diagram">Diagram</a>
    |   <a href="#parameters">Parameters</a>
    |   <a href="#connections">Database Connections</a>
    |   <a href="#files">Flat Files</a>
    |   <a href="#files">Variables</a>
    </div>
</xsl:variable>
<!-- =========================================================================
    KETTLE GENERIC
========================================================================== -->
<xsl:output
    method="html"
    version="4.0"
    encoding="UTF-8"
    omit-xml-declaration="yes"
    media-type="text/html"
    doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
    doctype-system="http://www.w3.org/TR/html4/loose.dtd"
/>

<xsl:template match="/">
<html>    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <xsl:comment>
            Debugging info. Ugly, huh? 
        
            count(/index): <xsl:value-of select="count(/index)"/>
            count(/index/item): <xsl:value-of select="count(/index/item)"/>
            count(/index/item[short_filename/text()=$normalized_short_filename]): <xsl:value-of select="count(/index/item[short_filename/text()=$normalized_short_filename])"/>
            count(/index/item[relative-path/text()=$normalized_relative-path]): <xsl:value-of select="count(/index/item[relative-path/text()=$normalized_relative-path])"/>
            count(/index/item[relative-path/text()=$normalized_relative-path and short_filename/text()=$normalized_short_filename]): <xsl:value-of select="count(/index/item[relative-path/text()=$normalized_relative-path and short_filename/text()=$normalized_short_filename])"/>
        
            input-dir: '<xsl:value-of select="/index/@input-dir"/>'
            output-dir: '<xsl:value-of select="/index/@output-dir"/>'
            file-separator: '<xsl:value-of select="/index/@file-separator"/>'
            
            short_filename: '<xsl:value-of select="$normalized_short_filename"/>'
            relative-path: '<xsl:value-of select="$normalized_relative-path"/>'
            uri: '<xsl:value-of select="$item/uri"/>'
            
        </xsl:comment>
        <title>Kettle Documentation: <xsl:value-of select="local-name($document/*)"/> "<xsl:value-of select="$document/*/name"/>"</title>
        <link rel="shortcut icon" type="image/x-icon">
            <xsl:attribute name="href">
                <xsl:value-of 
                    select="
                        concat(
                            $documentation-root
                        ,   '/images/spoon.png'
                        )
                    "
                />
            </xsl:attribute>
        </link>
        <link rel="stylesheet" type="text/css">
            <xsl:attribute name="href">
                <xsl:value-of 
                    select="
                        concat(
                            $documentation-root
                        ,   '/css/kettle.css'
                        )
                    "
                />
            </xsl:attribute>
        </link>
        <link rel="stylesheet" type="text/css">
            <xsl:attribute name="href">
                <xsl:value-of 
                    select="
                        concat(
                            $documentation-root
                        ,   '/css/', local-name($document/*),'.css'
                        )
                    "
                />
            </xsl:attribute>
        </link>
    </head>
    <body class="kettle-file" onload="drawHops()">
        <xsl:copy-of select="$quick-links"/>
                
        <xsl:for-each select="$document">
            <xsl:apply-templates/>
        </xsl:for-each>
        
        <script type="text/javascript">
            <xsl:attribute name="src">
                <xsl:value-of select="concat($documentation-root, '/js/wz_jsgraphics.js')"/>
            </xsl:attribute>
        </script>
        <script>
            <xsl:attribute name="src">
                <xsl:value-of select="concat($documentation-root, '/js/kettle.js')"/>
            </xsl:attribute>
        </script>
    </body>
</html>
</xsl:template>

<xsl:template match="parameters">
    <h2><a name="parameters">Parameters</a></h2>    
    <xsl:choose>
        <xsl:when test="parameter">
            <p>
                This <xsl:value-of select="$item-type"/> defines <xsl:value-of select="count(parameter)"/> parameters.
            </p>
            <table>
                <thead>
                    <tr>
                        <th>
                            Name
                        </th>
                        <th>
                            Default Value
                        </th>
                        <th>
                            Description
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each select="parameter">
                        <tr>
                            <th>
                                <xsl:value-of select="name"/>
                            </th>
                            <td>
                                <xsl:value-of select="default_value"/>
                            </td>
                            <td>
                                <xsl:value-of select="description"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
        </xsl:when>
        <xsl:otherwise>
            <p>
                This <xsl:value-of select="$item-type"/> does not define any parameters.
            </p>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="description">
    <xsl:param name="node" select="."/>
    <xsl:for-each select="$node">
        <p>
            <xsl:choose>
                <xsl:when test="description[text()]">
                    <xsl:value-of select="description"/>
                </xsl:when>
                <xsl:otherwise>
                    This <xsl:value-of select="$item-type"/> does not have a description.
                </xsl:otherwise>
            </xsl:choose>
        </p>
        <xsl:if test="extended_description[text()]">
            <p>
                <xsl:value-of select="extended_description"/>
            </p>
        </xsl:if>
    </xsl:for-each>
</xsl:template>
<!-- =========================================================================
    Utils
========================================================================== -->
<xsl:template name="get-documentation-root">
    <xsl:param name="path" select="$normalized_relative-path"/>
    <xsl:param name="documentation-root" select="'..'"/>
    <xsl:choose>
        <xsl:when test="contains($path, $file-separator)">
            <xsl:call-template name="get-documentation-root">
                <xsl:with-param name="path" select="substring-after($path, $file-separator)"/>
                <xsl:with-param name="documentation-root" select="concat($documentation-root, '/..')"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$documentation-root"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="max">
    <xsl:param name="values"/>
    <xsl:for-each select="$values">
        <xsl:sort data-type="number" order="descending"/>
        <xsl:if test="position()=1">
          <xsl:value-of select="."/>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="min">
    <xsl:param name="values"/>
    <xsl:for-each select="$values">
        <xsl:sort data-type="number"/>
        <xsl:if test="position()=1">
          <xsl:value-of select="."/>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<!-- =========================================================================
    VARIABLES
========================================================================== -->
<xsl:template name="get-variables">
    <xsl:param name="text"/>
    <xsl:param name="variables" select="''"/>
    <xsl:choose>
        <xsl:when test="contains($text, '${')">
            <xsl:variable name="after" select="substring-after($text, '${')"/>
            <xsl:variable name="var" select="substring-before($after, '}')"/>
            <xsl:variable name="vars">
                <xsl:choose>
                    <xsl:when test="contains(concat(',', $variables), concat(',', $var, ','))"><xsl:value-of select="$variables"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="concat($variables, $var, ',')"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:call-template name="get-variables">
                <xsl:with-param name="text" select="substring-after($after, '}')"/>
                <xsl:with-param name="variables" select="$vars"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$variables"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="variable-list">
    <xsl:variable name="nodes-using-variables" select="$document//*[contains(text(), '${')]"/>
    <xsl:variable name="list">
        <xsl:for-each select="$nodes-using-variables">
            <xsl:call-template name="get-variables">
                <xsl:with-param name="text" select="text()"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:variable>    
    <xsl:value-of select="$list"/>
</xsl:template>

<xsl:template name="unique-variables">
    <xsl:param name="before" select="''"/>
    <xsl:param name="after">
        <xsl:call-template name="variables"/>
    </xsl:param>
    <xsl:variable name="var" select="substring-before($after, ',')"/>
    <xsl:if test="not(contains(concat(',', $before, ','), concat(',',$var,',')))">
        <tr>
            <th>
                <xsl:value-of select="$var"/>
            </th>
            <td>
            </td>
        </tr>
    </xsl:if>
    <xsl:if test="string-length($var)!=0">
        <xsl:call-template name="unique-variables">
            <xsl:with-param name="before" select="concat($before, ',', $var)"/>
            <xsl:with-param name="after" select="substring-after($after, ',')"/>
        </xsl:call-template>
    </xsl:if>
</xsl:template>

<xsl:template name="variables">
    <xsl:variable name="variables-list">
        <xsl:call-template name="variable-list"/>
    </xsl:variable>
    <h2>
        <a name="variables">Variables</a>
    </h2>
    <xsl:choose>
        <xsl:when test="string-length($variables-list)=0">
            This <xsl:value-of select="$item-type"/> does not read any variables.
        </xsl:when>
        <xsl:otherwise>
            This <xsl:value-of select="$item-type"/> reads the following variables:
            <table>
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Value</th>
                        <th>Used in</th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:call-template name="unique-variables">
                        <xsl:with-param name="after" select="$variables-list"/>
                    </xsl:call-template>
                </tbody>
            </table>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<!-- =========================================================================
    DIAGRAM
========================================================================== -->
<xsl:template name="transformation-diagram">
    <xsl:param name="transformation" select="$document/transformation"/>
    
    <xsl:variable name="steps" select="$transformation/step"/>
    <xsl:variable name="error-handlers" select="$transformation/step_error_handling/error"/>

    <xsl:variable name="xlocs" select="$steps/GUI/xloc"/>
    <xsl:variable name="ylocs" select="$steps/GUI/yloc"/>
    <xsl:variable name="hops" select="$transformation/order/hop"/>

    <xsl:variable name="max-xloc">
        <xsl:call-template name="max">
            <xsl:with-param name="values" select="$xlocs"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="min-xloc">
        <xsl:call-template name="min">
            <xsl:with-param name="values" select="$xlocs"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="max-yloc">
        <xsl:call-template name="max">
            <xsl:with-param name="values" select="$ylocs"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="min-yloc">
        <xsl:call-template name="min">
            <xsl:with-param name="values" select="$ylocs"/>
        </xsl:call-template>
    </xsl:variable>

    <h2><a name="diagram">Diagram</a></h2>    
    <div class="diagram" id="diagram">
        <xsl:attribute name="style">
            width: <xsl:value-of select="($max-xloc - $min-xloc) + 128"/>px;
            height: <xsl:value-of select="($max-yloc - $min-yloc) + 128"/>px;
        </xsl:attribute>
        <xsl:for-each select="$steps">
            <xsl:variable name="name" select="name/text()"/>
            <xsl:variable name="xloc" select="GUI/xloc - $min-xloc"/>
            <xsl:variable name="yloc" select="GUI/yloc - $min-yloc"/>
            <xsl:variable name="text-pixels" select="string-length(name) * 4"/>            
            <xsl:variable name="hide" select="GUI/draw/text()='N'"/>
            <div>
                <xsl:attribute name="id"><xsl:value-of select="$name"/></xsl:attribute>
                <xsl:attribute name="class">
                    step-icon
                    step-icon-<xsl:value-of select="type"/>
                    <xsl:if test="$error-handlers[target_step/text() = $name]">
                        step-error
                    </xsl:if>                                        
                    <xsl:if test="$hide">
                        step-hidden
                    </xsl:if>
                </xsl:attribute>                
                <xsl:attribute name="style">
                    left:<xsl:value-of select="$xloc"/>px;
                    top:<xsl:value-of select="$yloc"/>px;
                </xsl:attribute>
                <div class="step-hops">
                    <xsl:for-each select="$hops[from/text()=$name]">
                        <xsl:variable name="from" select="from/text()"/>
                        <xsl:variable name="to" select="to/text()"/>
                        <a>
                            <xsl:attribute name="class">
                                step-hop
                                <xsl:if 
                                    test="
                                        $error-handlers[
                                            source_step/text()=$from
                                        and target_step/text()=$to
                                        ]
                                    "
                                >
                                step-hop-error
                                </xsl:if>
                            </xsl:attribute>
                            <xsl:attribute name="href"><xsl:value-of select="concat('#', $to)"/></xsl:attribute>
                        </a>
                    </xsl:for-each>
                </div>
            </div>
            <a>
                <xsl:attribute name="class">
                    step-label
                    <xsl:if test="$hide">
                        step-label-hidden
                    </xsl:if>
                </xsl:attribute>
                <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
                <xsl:attribute name="style">
                    top:<xsl:value-of select="$yloc + 32"/>px;
                    left:<xsl:value-of select="$xloc - ($text-pixels div 3)"/>px;                    
                    </xsl:attribute>
                <xsl:value-of select="$name"/>
            </a>
        </xsl:for-each>
    </div>
</xsl:template>

<xsl:template name="job-diagram">
    <xsl:param name="job" select="$document/job"/>
    <xsl:variable name="entries" select="$job/entries/entry"/>

    <xsl:variable name="xlocs" select="$entries/xloc"/>
    <xsl:variable name="ylocs" select="$entries/yloc"/>
    <xsl:variable name="hops" select="$job/hops/hop"/>

    <xsl:variable name="max-xloc">
        <xsl:call-template name="max">
            <xsl:with-param name="values" select="$xlocs"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="min-xloc">
        <xsl:call-template name="min">
            <xsl:with-param name="values" select="$xlocs"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="max-yloc">
        <xsl:call-template name="max">
            <xsl:with-param name="values" select="$ylocs"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="min-yloc">
        <xsl:call-template name="min">
            <xsl:with-param name="values" select="$ylocs"/>
        </xsl:call-template>
    </xsl:variable>

    <h2><a name="diagram">Diagram</a></h2>    
    <div class="diagram" id="diagram">
        <xsl:attribute name="style">
            width: <xsl:value-of select="($max-xloc - $min-xloc) + 128"/>px;
            height: <xsl:value-of select="($max-yloc - $min-yloc) + 128"/>px;
        </xsl:attribute>
        <xsl:for-each select="$entries">
            <xsl:variable name="name" select="name/text()"/>
            <xsl:variable name="xloc" select="xloc - $min-xloc"/>
            <xsl:variable name="yloc" select="yloc - $min-yloc"/>
            <xsl:variable name="text-pixels" select="string-length(name) * 4"/>
            <div>
                <xsl:attribute name="id"><xsl:value-of select="$name"/></xsl:attribute>
                <xsl:attribute name="class">
                    entry-icon
                    entry-icon-<xsl:choose>
                        <xsl:when test="type/text()='SPECIAL'"><xsl:value-of select="name"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="type"/></xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>                
                <xsl:attribute name="style">
                    left:<xsl:value-of select="$xloc"/>px;
                    top:<xsl:value-of select="$yloc"/>px;
                </xsl:attribute>
                <div class="entry-hops">
                    <xsl:for-each select="$hops[from/text()=$name]">
                        <a>
                            <xsl:attribute name="class">
                                entry-hop
                                <xsl:choose>
                                    <xsl:when test="unconditional/text()='Y'">entry-hop-unconditional</xsl:when>
                                    <xsl:when test="evaluation/text()='Y'">entry-hop-true</xsl:when>
                                    <xsl:when test="evaluation/text()='N'">entry-hop-false</xsl:when>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:attribute name="href"><xsl:value-of select="concat('#', to/text())"/></xsl:attribute>
                        </a>
                    </xsl:for-each>
                </div>
            </div>
            <a class="entry-label">
                <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
                <xsl:attribute name="style">
                    top:<xsl:value-of select="$yloc + 32"/>px;
                    left:<xsl:value-of select="$xloc - ($text-pixels div 3)"/>px;
                </xsl:attribute>
                <xsl:value-of select="$name"/>
            </a>
        </xsl:for-each>
    </div>
</xsl:template>
<!-- =========================================================================
    KETTLE TRANSFORMATION
========================================================================== -->
<xsl:template match="transformation">
    <xsl:apply-templates select="info"/>

    <xsl:apply-templates select="parameters"/>
    <xsl:call-template name="variables"/>
    <xsl:call-template name="transformation-diagram"/>
    
    <h2><a name="connections">Database Connections</a></h2>    
    <xsl:choose>
        <xsl:when test="connection">
            This transformation defines <xsl:value-of select="count(connection)"/> database connections.
            <h3>Database Connection Summary</h3>
            <table>
                <thead>
                    <th>
                        Name
                    </th>
                    <th>
                        Type
                    </th>
                    <th>
                        Access
                    </th>
                    <th>
                        Host
                    </th>
                    <th>
                        Port
                    </th>
                    <th>
                        User
                    </th>
                </thead>
                <tbody>
                    <xsl:for-each select="connection">
                        <tr>
                            <th>
                                <a>
                                    <xsl:attribute name="href">#connection.<xsl:value-of select="name"/></xsl:attribute>
                                    <xsl:value-of select="name"/>
                                </a>
                            </th>
                            <td>
                                <xsl:value-of select="type"/>
                            </td>
                            <td>
                                <xsl:value-of select="access"/>
                            </td>
                            <td>
                                <xsl:value-of select="server"/>
                            </td>
                            <td>
                                <xsl:value-of select="port"/>
                            </td>
                            <td>
                                <xsl:value-of select="username"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
            <h3>Database Connection Details</h3>
            <xsl:apply-templates select="connection"/>
        </xsl:when>
        <xsl:otherwise>
            This transformation does no define any database connections.
        </xsl:otherwise>
    </xsl:choose>

    <h2>Flat Files</h2>
    <p>
        t.b.d.
    </p>
</xsl:template>



<xsl:template match="transformation/info">
    <h1>Transformation: "<xsl:value-of select="name"/>"</h1>
    <xsl:call-template name="description"/>
</xsl:template>

<xsl:template match="transformation/connection">
    <h4>
        <a>
            <xsl:attribute name="name">connection.<xsl:value-of select="name"/></xsl:attribute>
            <xsl:value-of select="name"/>
        </a>
    </h4>
    <table>
        <thead>
            <tr>
                <th>Property</th>
                <th>Value</th>
            </tr>
        </thead>
        <tbody>
            <xsl:apply-templates select="*"/>
        </tbody>
    </table>
</xsl:template>

<xsl:template match="transformation/connection/*[node()]">
    <xsl:variable name="tag" select="local-name()"/>
    <tr>
        <td><xsl:value-of select="$tag"/></td>
        <td>
            <xsl:choose>
                <xsl:when test="$tag = 'attributes'">
                    
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>            
        </td>
    </tr>
</xsl:template>

<!-- =========================================================================
    KETTLE JOB
========================================================================== -->

<xsl:template match="job">
    <h1>Job: "<xsl:value-of select="name"/>"</h1>
    <table>
        <thead>
            <tr>
                <th>What?</th>
                <th>Who?</th>
                <th>When?</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <th>Created</th>
                <td><xsl:value-of select="created_user"/></td>
                <td><xsl:value-of select="created_date"/></td>
            </tr>
            <tr>
                <th>Modified</th>
                <td><xsl:value-of select="modified_user"/></td>
                <td><xsl:value-of select="modified_date"/></td>
            </tr>
        </tbody>
    </table>
    <xsl:call-template name="description"/>
    <xsl:apply-templates select="parameters"/>
    <xsl:call-template name="variables"/>
    <xsl:call-template name="job-diagram"/>
</xsl:template>

</xsl:stylesheet>
