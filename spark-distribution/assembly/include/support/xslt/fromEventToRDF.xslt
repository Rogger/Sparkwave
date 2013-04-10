<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Edited by XMLSpy® 25.3 -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:wpdemo="http://www.foi.se/support/wp4demo#" xmlns:supportOnt="http://www.foi.se/support/wp4demo#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:owl="http://www.w3.org/2002/07/owl#"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema#">
	<xsl:output method="xml" version="1.0" indent="yes"
		omit-xml-declaration="no" encoding="utf8" doctype-public="-//W3C//DTD SVG 20010904//EN" />


	<!-- Event -->

	<xsl:template name='Event' match="/Event">

		<xsl:comment>
			RDF generated based on SUPPORT Event format.
		</xsl:comment>
		<rdf:RDF>


			<!-- If/else case: Check if type contains URI: Take the part after # in 
				such cases -->
			<xsl:choose>
				<xsl:when test="contains(@type,'#')">

					<xsl:element name="supportOnt:{substring-after(@type,'#')}">
						<xsl:attribute name="rdf:about"><xsl:value-of
							select="@ref_uri" /></xsl:attribute>

						<supportOnt:date>
							<xsl:value-of select="@date" />
						</supportOnt:date>
						<supportOnt:name>
							<xsl:value-of select="@name" />
						</supportOnt:name>
						
						<supportOnt:generated-by>
							<xsl:value-of select="@generated-by" />
						</supportOnt:generated-by>
						
						<xsl:apply-templates select="event-description/*"></xsl:apply-templates>
						<rdf:type rdf:resource="http://www.foi.se/support/wp4demo#Event" />
						<xsl:apply-templates select="/Event/location"></xsl:apply-templates>

					</xsl:element>

				</xsl:when>
				<xsl:otherwise>
					<xsl:element name="{@type}">
						<xsl:attribute name="rdf:about"><xsl:value-of
							select="@ref_uri" /></xsl:attribute>

						<supportOnt:date>
							<xsl:value-of select="@date" />
						</supportOnt:date>
						<supportOnt:name>
							<xsl:value-of select="@name" />
						</supportOnt:name>
						
							<supportOnt:generated-by>
							<xsl:value-of select="@generated-by" />
						</supportOnt:generated-by>
						
						<xsl:apply-templates select="event-description/*"></xsl:apply-templates>
						<rdf:type rdf:resource="http://www.foi.se/support/wp4demo#Event" />
						<xsl:apply-templates select="/Event/location"></xsl:apply-templates>

					</xsl:element>


				</xsl:otherwise>

			</xsl:choose>



			<xsl:apply-templates select="/Event/source-data/source-reference"></xsl:apply-templates>
		</rdf:RDF>
	</xsl:template>



	<xsl:template name='Extra'
		match="/Event/node()[not(self::event-description)]">
		<xsl:apply-templates select="*"></xsl:apply-templates>
	</xsl:template>


	<!-- source-reference -->
	<xsl:template name='source-reference' match='source-reference'>
		<xsl:element name='supportOnt:EventSourceData'>
			<xsl:attribute name="rdf:about"><xsl:value-of select="@uri" /></xsl:attribute>
		</xsl:element>
	</xsl:template>


	<!-- Location -->
	<xsl:template name='location' match='/Event/location'>
		<xsl:element name='supportOnt:location'>
			<xsl:attribute name="rdf:resource"><xsl:value-of select="@name" /></xsl:attribute>
		</xsl:element>
	</xsl:template>


	<!-- Event description -->
	<xsl:template name='event_desc' match='*'>
		<xsl:choose>

			<xsl:when test="@uri and @type and @type='UNKNOWN'">

				<xsl:element name='rdf:Description'>
					<xsl:attribute name="rdf:about"><xsl:value-of
						select="@uri" /></xsl:attribute>
					<xsl:apply-templates select="*"></xsl:apply-templates>
				</xsl:element>

			</xsl:when>

			<xsl:when test="@uri and @type">

				<xsl:element name='supportOnt:{@type}'>
					<xsl:attribute name="rdf:about"><xsl:value-of
						select="@uri" /></xsl:attribute>
					<xsl:apply-templates select="*"></xsl:apply-templates>
				</xsl:element>

			</xsl:when>

			<xsl:when test="@uri">

				<xsl:element name='rdf:Description'>
					<xsl:attribute name="rdf:about"><xsl:value-of
						select="@uri" /></xsl:attribute>
					<xsl:apply-templates select="*"></xsl:apply-templates>
				</xsl:element>
			</xsl:when>

			<xsl:otherwise>
				<xsl:element name='supportOnt:{name()}'>
				<xsl:value-of select="child::text()" />
					<xsl:apply-templates select="*"></xsl:apply-templates>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

</xsl:stylesheet>


