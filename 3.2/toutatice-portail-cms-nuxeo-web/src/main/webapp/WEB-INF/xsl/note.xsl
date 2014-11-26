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

		<xsl:variable name="note" select="document/schema[@name='note']/note" />
		<xsl:variable name="displayMode" select="bridge:wysiwygDisplayMode($bridge)" />


		<xsl:call-template name="setTitle">
			<xsl:with-param name="title"
				select="document/schema[@name='dublincore']/title" />
		</xsl:call-template>


		<div class="nuxeo-note">

			<div class="nuxeo-note-html">
				<p>
					<xsl:value-of select="bridge:transformWysiwyg($bridge, $note)"
						disable-output-escaping="yes" />
				</p>
			</div>

			<xsl:choose>
				<xsl:when test="$displayMode = 'complet'">
					<xsl:apply-templates select="document/schema[@name='files']" />
				</xsl:when>
				<xsl:when test="$displayMode = 'partiel'">
					<div class="nuxeo-note-switch-mode">
						<a>
							<xsl:attribute name="href">
         						<xsl:value-of select="bridge:maximizedLink($bridge)" />
      						</xsl:attribute>
							Voir la suite
						</a>
					</div>
				</xsl:when>
			</xsl:choose>

		</div>
	</xsl:template>

	<xsl:template name="setTitle">
		<xsl:param name="title" />
		<xsl:value-of select="bridge:setTitle($bridge, $title)" />
	</xsl:template>


	<xsl:template match="schema[@name='files']/files">
		<xsl:choose>
			<xsl:when test="node()">
				<div class="nuxeo-note-files">
					<h3>
						<span>Fichiers joints</span>
					</h3>
					<ul>
						<xsl:apply-templates select="./item/file" />
					</ul>
				</div>
			</xsl:when>
		</xsl:choose>
	</xsl:template>


	<xsl:template match="schema[@name='files']/files/item/file">

		<xsl:variable name="urlFile">/nuxeo/nxfile/default/<xsl:value-of select="/document/@id" />/files:files/<xsl:value-of select="position() - 1" />/file/<xsl:value-of select="filename" /></xsl:variable>
		<li>
			<a>
				<xsl:attribute name="href">
         			<xsl:value-of select="bridge:link($bridge,  $urlFile)" />
      			</xsl:attribute>

				<xsl:value-of select="filename" />
			</a>
		</li>

	</xsl:template>

</xsl:stylesheet>