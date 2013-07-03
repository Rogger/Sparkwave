<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- 25.3 -->
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:wpdemo="http://www.foi.se/support/wp4demo#"
	xmlns:supportOnt="http://www.foi.se/support/wp4demo#"
	xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
	xmlns:owl="http://www.w3.org/2002/07/owl#"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
	exclude-result-prefixes="wpdemo owl rdf rdfs xsd"
	>
	<xsl:output method="xml" version="1.0" indent="yes"
		omit-xml-declaration="yes" encoding="utf8" doctype-public="-//W3C//DTD SVG 20010904//EN" />



	<!-- Find the Event instance: (An object with type Event) -->
	<!-- Alternative 1: "Event "Type is within the element -->
	<xsl:template match="/">
		<xsl:apply-templates
			select="/rdf:RDF/supportOnt:*[rdf:type[@rdf:resource='http://www.foi.se/support/wp4demo#Event']]"></xsl:apply-templates>
			<xsl:apply-templates
			select="/rdf:RDF/supportOnt:Event"></xsl:apply-templates>
	</xsl:template>

	
	<!--  Second alternative: Where the element name is Event -->



	<!-- Find the event -->
	<!-- <xsl:template match="*[rdf:type[@rdf:resource='http://www.foi.se/support/wp4demo#Event']]"> -->
	<xsl:template
		match="/rdf:RDF/supportOnt:*[rdf:type[@rdf:resource='http://www.foi.se/support/wp4demo#Event']]">


		<xsl:element name='Event'>
	<!-- <xsl:attribute name="wpdemo" namespace="xmls">http://www.foi.se/support/wp4demo#</xsl:attribute>  -->
		

			<xsl:attribute name="name"><xsl:value-of
				select="supportOnt:name/text()" /></xsl:attribute>
			<xsl:attribute name="id"><xsl:value-of
				select="supportOnt:id/text()" /></xsl:attribute>
			<xsl:attribute name="date"><xsl:value-of
				select="supportOnt:date/text()" /></xsl:attribute>
			<xsl:attribute name="ref_uri"><xsl:value-of select="@rdf:about" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="name()" /></xsl:attribute>
			<xsl:attribute name="generated-by"><xsl:value-of select="supportOnt:generated-by/@rdf:resource" /></xsl:attribute>


			<xsl:for-each select="supportOnt:location">
				<xsl:element name='location'>
					<xsl:attribute name="name"><xsl:value-of
						select="@rdf:resource" /></xsl:attribute>
					<xsl:attribute name="longitude"><xsl:value-of
						select="rdf:Description/supportOnt:longitude/text()" /></xsl:attribute>
					<xsl:attribute name="latitude"> <xsl:value-of
						select="rdf:Description/supportOnt:latitude/text()" /></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
			<source-data>
				<xsl:apply-templates select="/rdf:RDF/supportOnt:EventSourceData"></xsl:apply-templates>
			</source-data>
			<event-description>
				<xsl:apply-templates select="*"></xsl:apply-templates>
			</event-description>
		</xsl:element>
	</xsl:template>



<!-- Find the event (Alternative 2: Tag is name Event.. type is defined within)-->
	<!-- <xsl:template match="*[rdf:type[@rdf:resource='http://www.foi.se/support/wp4demo#Event']]"> -->
	<xsl:template
		match="/rdf:RDF/supportOnt:Event">


		<xsl:element name='Event'>
	<!-- <xsl:attribute name="wpdemo" namespace="xmls">http://www.foi.se/support/wp4demo#</xsl:attribute>  -->
		

			<xsl:attribute name="name"><xsl:value-of
				select="supportOnt:name/text()" /></xsl:attribute>
			<xsl:attribute name="date"><xsl:value-of
				select="supportOnt:date/text()" /></xsl:attribute>
			<xsl:attribute name="ref_uri"><xsl:value-of select="@rdf:about" /></xsl:attribute>
			<xsl:attribute name="type"><xsl:value-of select="rdf:type/@rdf:resource" /></xsl:attribute>
			<xsl:attribute name="generated-by"><xsl:value-of select="supportOnt:generated-by/@rdf:resource" /></xsl:attribute>


			<xsl:for-each select="supportOnt:location">
				<xsl:element name='location'>
					<xsl:attribute name="name"><xsl:value-of
						select="@rdf:resource" /></xsl:attribute>
					<xsl:attribute name="longitude"><xsl:value-of
						select="rdf:Description/supportOnt:longitude/text()" /></xsl:attribute>
					<xsl:attribute name="latitude"> <xsl:value-of
						select="rdf:Description/supportOnt:latitude/text()" /></xsl:attribute>
				</xsl:element>
			</xsl:for-each>
			<source-data>
				<xsl:apply-templates select="/rdf:RDF/supportOnt:EventSourceData"></xsl:apply-templates>
			</source-data>
			<event-description>
				<xsl:apply-templates select="*"></xsl:apply-templates>
			</event-description>
		</xsl:element>
	</xsl:template>


	<!-- NOOP: These should not show up in the event description -->

	<xsl:template name="NOOP"
		match="supportOnt:name|supportOnt:date|supportOnt:location|supportOnt:generated-by">
	</xsl:template>


	<xsl:template name="expantSourceData" match="supportOnt:EventSourceData">
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
				<xsl:attribute name="mimeType"><xsl:value-of	select="supportOnt:mimeType/text()" /></xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!-- Event description -->
	<xsl:template name="genExpand" match="supportOnt:*|rdf:Description">

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

					<xsl:apply-templates select="supportOnt:*|rdf:Description"></xsl:apply-templates>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@rdf:resource">
				<xsl:element name='{local-name(.)}'>
					<xsl:element name='object'>
						<xsl:attribute name="uri"><xsl:value-of
							select="@rdf:resource" /></xsl:attribute>
						<xsl:apply-templates select="supportOnt:*|rdf:Description"></xsl:apply-templates>
					</xsl:element>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name='{local-name(.)}'>
					<xsl:value-of select="child::text()" />
					<xsl:apply-templates select="supportOnt:*|rdf:Description"></xsl:apply-templates>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>

