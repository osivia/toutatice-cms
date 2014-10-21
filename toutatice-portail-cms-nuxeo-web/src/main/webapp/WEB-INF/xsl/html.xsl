<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:bridge="java:fr.toutatice.portail.cms.nuxeo.portlets.customizer.helpers.XSLFunctions"
    extension-element-prefixes="bridge">

    <xsl:param name="bridge" />

    <xsl:output
        method="html"
        encoding="UTF-8"
        indent="yes"
        standalone="no"
        omit-xml-declaration="yes" />


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

    <xsl:template match="IMG" name="image">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />

            <xsl:if test="not(ancestor::* [@class = 'no-format'])">
                <xsl:attribute name="class"><xsl:value-of select="@class" /> img-responsive</xsl:attribute>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="IMG[@class = 'fancybox']">
        <xsl:choose>
            <xsl:when test="parent::a">
                <xsl:call-template name="image" />
            </xsl:when>
            
            <xsl:otherwise>
                <a class="thumbnail fancybox" rel="gallery">
                    <xsl:attribute name="href">
                        <xsl:value-of select="bridge:thumbnailSource($bridge,  @src)" />
                    </xsl:attribute>
                
                    <xsl:call-template name="image" />
                </a>
            </xsl:otherwise>
        </xsl:choose>
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

    <xsl:template match="OBJECT/@data">
        <xsl:attribute name="data">
            <xsl:value-of select="bridge:link($bridge,  .)" />
      </xsl:attribute>
    </xsl:template>
    <xsl:template match="OBJECT/PARAM/@value">
        <xsl:attribute name="value">
            <xsl:value-of select="bridge:link($bridge,  .)" />
      </xsl:attribute>
    </xsl:template>

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