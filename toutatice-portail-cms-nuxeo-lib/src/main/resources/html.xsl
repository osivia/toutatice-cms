<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:bridge="java:fr.toutatice.portail.cms.nuxeo.core.XSLFunctions" 
	extension-element-prefixes="bridge">
	
	<xsl:param name="bridge" />
	
	<xsl:output method="html" 
		encoding="UTF-8" indent="yes"
		standalone="no" omit-xml-declaration="yes" />
	
	
	<xsl:template match="/">
	   	<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="/HTML">
		<xsl:apply-templates select="BODY" />
	</xsl:template>
	
	
	<xsl:template match="/HTML/BODY">
		<div>
				<xsl:apply-templates select="node()" />
		</div>
	</xsl:template>
	
	
	<xsl:template match="@src">
		<xsl:attribute name="src">
         	<xsl:value-of select="bridge:link($bridge,  .)" />
      </xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@href">
		<xsl:attribute name="href">
         	<xsl:value-of select="bridge:link($bridge,  .)" />
      </xsl:attribute>
	</xsl:template>
	
<!-- 	
	
	
	<xsl:template match="IMG/@src">
		<xsl:attribute name="src">
         	<xsl:value-of select="bridge:link($bridge,  .)" />
      </xsl:attribute>
	</xsl:template>
	
	<xsl:template match="A/@href">
		<xsl:attribute name="href">
         	<xsl:value-of select="bridge:link($bridge,  .)" />
      </xsl:attribute>
	</xsl:template>
-->
		<xsl:template match="AREA/@href">
		<xsl:attribute name="href">
         	<xsl:value-of select="bridge:link($bridge,  .)" />
      </xsl:attribute>
	</xsl:template>
	
	
	
	<xsl:template match="@*|*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	

</xsl:stylesheet>