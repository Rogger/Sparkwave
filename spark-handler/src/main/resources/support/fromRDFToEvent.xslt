<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:wpdemo="http://www.foi.se/support/wp4demo#"
	xmlns:j.0="http://www.foi.se/support/wp4demo#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
	exclude-result-prefixes="wpdemo owl rdf rdfs xsd"
	>
	<xsl:output method="xml" version="1.0" indent="yes"
		omit-xml-declaration="yes" encoding="utf8" doctype-public="-//W3C//DTD SVG 20010904//EN" />



	<!-- Find the Event instance: (An object with type Event) -->
	<xsl:template match="/">
		<xsl:apply-templates
			select="/rdf:RDF/j.0:*[rdf:type[@rdf:resource='http://www.foi.se/support/wp4demo#Event']]"></xsl:apply-templates>
	</xsl:template>


	<!-- Find the event -->
	<!-- <xsl:template match="*[rdf:type[@rdf:resource='http://www.foi.se/support/wp4demo#Event']]"> -->
	<xsl:template
		match="/rdf:RDF/j.0:*[rdf:type[@rdf:resource='http://www.foi.se/support/wp4demo#Event']]">


		<xsl:element name='Event'>
	<!-- <xsl:attribute name="wpdemo" namespace="xmls">http://www.foi.se/support/wp4demo#</xsl:attribute>  -->
		

			<xsl:attribute name="name"><xsl:value-of
				select="j.0:name/text()" /></xsl:attribute>
			<xsl:attribute name="date"><xsl:value-of
				select="j.0:date/text()" /></xsl:attribute>
			<xsl:attribute name="ref_uri"><xsl:value-of select="@rdf:about" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="name()" /></xsl:attribute>

			<xsl:for-each select="j.0:location">
				<xsl:element name='location'>
					<xsl:attribute name="name"><xsl:value-of
						select="@rdf:resource" /></xsl:attribute>
					<xsl:attribute name="longitude"><xsl:value-of
						select="rdf:Description/j.0:longitude/text()" /></xsl:attribute>
					<xsl:attribute name="latitude"> <xsl:value-of
						select="rdf:Description/j.0:latitude/text()" /></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
			<source-data>
				<xsl:apply-templates select="/rdf:RDF/j.0:EventSourceData"></xsl:apply-templates>
			</source-data>
			<event-description>
				<xsl:apply-templates select="*"></xsl:apply-templates>
			</event-description>
		</xsl:element>
	</xsl:template>



	<!-- NOOP: These should not show up in the event description -->

	<xsl:template name="NOOP"
		match="j.0:name|j.0:date|j.0:location">
	</xsl:template>


	<xsl:template name="expantSourceData" match="j.0:EventSourceData">
		<xsl:element name='source-reference'>
			<xsl:choose>
				<xsl:when test="@rdf:about">
					<xsl:attribute name="uri"><xsl:value-of
						select="@rdf:about" /></xsl:attribute>
				</xsl:when>
				<xsl:when test="@rdf:resource">
					<xsl:attribute name="uri"><xsl:value-of
						select="@rdf:resource" /></xsl:attribute>

				</xsl:when>
				<xsl:otherwise>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:element>
	</xsl:template>

	<!-- Event description -->
	<xsl:template name="genExpand" match="j.0:*|rdf:Description">

		<xsl:choose>
			<xsl:when test="@rdf:about">
				<xsl:element name='object'>
					<xsl:attribute name="uri"><xsl:value-of
						select="@rdf:about" /></xsl:attribute>
					<xsl:attribute name="type"><xsl:value-of
						select="local-name()" /></xsl:attribute>

					<xsl:if test="local-name()='Description'">
						<xsl:attribute name="type"><xsl:value-of
							select="'UNKNOWN'" /></xsl:attribute>
					</xsl:if>

					<xsl:apply-templates select="j.0:*|rdf:Description"></xsl:apply-templates>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@rdf:resource">
				<xsl:element name='{local-name(.)}'>
					<xsl:element name='object'>
						<xsl:attribute name="uri"><xsl:value-of
							select="@rdf:resource" /></xsl:attribute>
						<xsl:apply-templates select="j.0:*|rdf:Description"></xsl:apply-templates>
					</xsl:element>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name='{local-name(.)}'>
					<xsl:value-of select="child::text()" />
					<xsl:apply-templates select="j.0:*|rdf:Description"></xsl:apply-templates>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>

