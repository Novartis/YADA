<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright 2016 Novartis Institutes for BioMedical Research Inc.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"/>
	<xsl:template match="/">
		<xsl:element name="html">
			<xsl:element name="head">
			</xsl:element>
			<xsl:element name="body">
				<xsl:apply-templates/>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<xsl:template match="RESULTSET">
		  <xsl:element name="table">
		    <xsl:attribute name="border">
		      <xsl:value-of select="1"/>
		    </xsl:attribute>
		    <xsl:attribute name="style">
		      <xsl:value-of select="'border-collapse:collapse;'"/>
		    </xsl:attribute>
		    <xsl:element name="thead">
		      <xsl:element name="caption">
		        <xsl:element name="div">
			        <xsl:value-of select="'qname: '"/>
			        <xsl:value-of select="@qname"/>
						</xsl:element>        
						<xsl:element name="div">
			        <xsl:value-of select="'Records: '"/>
			        <xsl:value-of select="@records"/>
			      </xsl:element>
		      </xsl:element>
		      <xsl:element name="tr">
		       <xsl:call-template name="header">
		          <xsl:with-param name="nodes" select="ROW[1]/*"/>
		        </xsl:call-template>
		      </xsl:element>
		    </xsl:element>
		    <xsl:element name="tbody">
		      <xsl:apply-templates/>
		    </xsl:element>
		  </xsl:element>
		</xsl:template>
		<xsl:template name="header">
		  <xsl:param name="nodes"/>
		  <xsl:for-each select="$nodes">
		    <xsl:element name="td">
		       <xsl:value-of select="name(.)"/>
		    </xsl:element>
		  </xsl:for-each>
		</xsl:template>
		<xsl:template match="ROW">
		  <xsl:element name="tr">
		    <xsl:for-each select="./*">
		      <xsl:element name="td">
		        <xsl:value-of select="."/>
		      </xsl:element>
		    </xsl:for-each>
		  </xsl:element>
		</xsl:template>
</xsl:stylesheet>