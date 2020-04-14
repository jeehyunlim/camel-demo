<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <HelloServer>
            <MsgId><xsl:value-of select="/Hello/id"/></MsgId>
            <Msg><xsl:value-of select="/Hello/message"/></Msg>
        </HelloServer>
    </xsl:template>

</xsl:stylesheet>