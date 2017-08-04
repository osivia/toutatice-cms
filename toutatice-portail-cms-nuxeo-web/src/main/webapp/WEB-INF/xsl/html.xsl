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
            <xsl:attribute name="class">clearfix no-ajax-link</xsl:attribute>
        
            <xsl:apply-templates select="node()" />
        </xsl:element>
    </xsl:template>


    <xsl:template match="IMG" name="image">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />

            <xsl:if test="not(ancestor::* [@class = 'no-format']) and not(ancestor::TABLE)">
                <xsl:attribute name="class">
                    <xsl:value-of select="@class" />
                    <xsl:text> img-responsive</xsl:text>
                    
                    <!-- Reprise des contenus TinyMCE pour Nuxeo < 6.0 -->
                    <xsl:if test="contains(ancestor::* [contains(@style, 'text-align')][1]/@style, 'text-align: center;')">
                        <xsl:text> center-block</xsl:text>
                    </xsl:if>
                    <xsl:if test="contains(ancestor::* [contains(@style, 'text-align')][1]/@style, 'text-align: right;')">
                        <xsl:text> right-block</xsl:text>
                    </xsl:if>
                </xsl:attribute>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="IMG[@class = 'enlargeable']">
        <xsl:choose>
            <xsl:when test="parent::A">
                <xsl:call-template name="image" />
            </xsl:when>
            
            <xsl:otherwise>
                <xsl:element name="a">
                    <xsl:attribute name="href"><xsl:value-of select="bridge:thumbnailSource($bridge,  @src)" /></xsl:attribute>
                    <xsl:attribute name="class">thumbnail no-ajax-link <xsl:value-of select="bridge:thumbnailClasses($bridge,  @style)" /></xsl:attribute>
                    <xsl:attribute name="data-fancybox">gallery</xsl:attribute>
                    
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
                
                <xsl:element name="div">
                	<xsl:attribute name="id">VIDEO_<xsl:value-of select="generate-id(.)" /></xsl:attribute>
                
            	</xsl:element>
            </xsl:element>
        </xsl:element>
    
        <xsl:element name="a">
            <xsl:attribute name="href">#<xsl:value-of select="generate-id(.)" /></xsl:attribute>
            <xsl:attribute name="class">thumbnail fancybox_wivibox</xsl:attribute>
            <xsl:attribute name="onclick">launchPlayer('VIDEO_<xsl:value-of select="generate-id(.)" />', '<xsl:value-of select="@src" />')</xsl:attribute>
            
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

	<xsl:template match="A[@href]">
       <xsl:element name="a">
       		<xsl:variable name="url" select="bridge:link($bridge,  @href)" />
      		<xsl:attribute name="href"><xsl:value-of select="$url" /></xsl:attribute>
     		
			<xsl:if test="not(@target) and not(contains($url,bridge:getBasePath($bridge))) and not(starts-with($url, '#'))">
	     		<xsl:attribute name="target">_blank</xsl:attribute>
	     	 </xsl:if>
	     	 
 			<xsl:apply-templates select="@*[name(.)!='href']|node()" />
        </xsl:element>        
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