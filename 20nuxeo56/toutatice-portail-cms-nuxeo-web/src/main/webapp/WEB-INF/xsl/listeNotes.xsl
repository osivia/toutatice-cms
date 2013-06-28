<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:bridge="java:fr.toutatice.portail.cms.nuxeo.portlets.bridge.BridgeFunctions" 
	extension-element-prefixes="bridge">
	
	<xsl:param name="bridge" />
	
	
	<xsl:output version="4.0" method="html" indent="yes"
				encoding="UTF-8" doctype-public="-//W3C//DTD HTML 4.01//EN"
				doctype-system="http://www.w3.org/TR/html4/strict.dtd" />
				

	<xsl:template match="/">
	   		<xsl:apply-templates select="./document"/>
	</xsl:template>
	
	
	<xsl:template match="/document">
	
		<xsl:call-template name="setTitle">
			<xsl:with-param name="title"
				select="@title" />
		</xsl:call-template>
	
		<ul>
	   		<xsl:apply-templates select="./document"/>
	   	</ul>
	</xsl:template>

	<xsl:template match="/document/document">
		<li>
			<a>
				<xsl:attribute name="href">
         			<xsl:value-of select="bridge:windowLink($bridge,  @id)" />
      			</xsl:attribute>
      			<xsl:value-of select="@title" />
      		</a>
		</li>
	</xsl:template>

	<xsl:template name="setTitle">
		<xsl:param name="title" />
		<xsl:value-of select="bridge:setTitle($bridge, $title)" />
	</xsl:template>
	
	
	<xsl:template match="@*|*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>