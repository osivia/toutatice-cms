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
        <xsl:element name="div">
            <xsl:apply-templates select="node()" />
        </xsl:element>
    </xsl:template>


    <xsl:template match="IMG" name="image">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />

            <xsl:if test="not(ancestor::* [@class = 'no-format'])">
                <xsl:attribute name="class"><xsl:value-of select="@class" /> img-responsive</xsl:attribute>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="IMG[@class = 'enlargeable']">
        <xsl:choose>
            <xsl:when test="parent::a">
                <xsl:call-template name="image" />
            </xsl:when>
            
            <xsl:otherwise>
                <xsl:element name="a">
                    <xsl:attribute name="href"><xsl:value-of select="bridge:thumbnailSource($bridge,  @src)" /></xsl:attribute>
                    <xsl:attribute name="rel">gallery</xsl:attribute>
                    <xsl:attribute name="class">thumbnail fancybox <xsl:value-of select="bridge:thumbnailClasses($bridge,  @style)" /></xsl:attribute>
                    
                    <xsl:copy>
                        <xsl:apply-templates select="@*|node()" />
                    
                        <xsl:attribute name="style"></xsl:attribute>
                    </xsl:copy>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template match="VIDEO[@class = 'enlargeable']">
        <xsl:element name="div">
            <xsl:attribute name="class">hidden</xsl:attribute>
            
            <xsl:element name="div">
                <xsl:attribute name="id"><xsl:value-of select="generate-id(.)" /></xsl:attribute>
                
                <xsl:copy-of select="." />
            </xsl:element>
        </xsl:element>
    
        <xsl:element name="a">
            <xsl:attribute name="href">#<xsl:value-of select="generate-id(.)" /></xsl:attribute>
            <xsl:attribute name="class">thumbnail fancybox_video</xsl:attribute>
            
            <xsl:element name="img">
                <xsl:attribute name="src"><xsl:value-of select="@poster" /></xsl:attribute>
                <xsl:attribute name="alt"></xsl:attribute>
                <xsl:attribute name="class">img-responsive</xsl:attribute>
            </xsl:element>
        </xsl:element>
    </xsl:template>


    <xsl:template match="VIDEO[@class = 'wivibox']">
        <xsl:element name="div">
            <xsl:attribute name="class">hidden</xsl:attribute>
            
            <xsl:element name="div">
                <xsl:attribute name="id"><xsl:value-of select="generate-id(.)" /></xsl:attribute>
            </xsl:element>
        </xsl:element>
    
        <xsl:element name="a">
            <xsl:attribute name="href">#<xsl:value-of select="generate-id(.)" /></xsl:attribute>
            <xsl:attribute name="class">thumbnail fancybox_inline</xsl:attribute>
            <xsl:attribute name="onclick">launchPlayer('<xsl:value-of select="generate-id(.)" />', '<xsl:value-of select="@src" />')</xsl:attribute>
            
            <xsl:element name="img">
                <xsl:attribute name="src"><xsl:value-of select="@poster" /></xsl:attribute>
                <xsl:attribute name="alt"></xsl:attribute>
                <xsl:attribute name="class">img-responsive</xsl:attribute>
            </xsl:element>
        </xsl:element>
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