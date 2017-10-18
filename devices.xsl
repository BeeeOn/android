<?xml version="1.0" encoding="utf-8"?>
<x:stylesheet xmlns:x="http://www.w3.org/1999/XSL/Transform"
		xmlns:e="http://exslt.org/common"
		xmlns:d="http://exslt.org/dates-and-times"
		xmlns:tools="http://schemas.android.com/tools"
		extension-element-prefixes="e d"
		version="1.0">

	<x:output method="text" encoding="utf-8" />
	<x:strip-space elements="*" />

	<x:param name="VERBOSE" select="'no'" />

	<x:variable name="types.xml" select="document('types.xml', .)/types" />
	<x:variable name="enums.xml" select="document('enums.xml', .)/enums" />

	<x:variable name="TYPE_ENUM_ID" select="'6'" />

	<x:template match="/">
		<x:message>generating file generated_strings_devices.xml...</x:message>
		<x:document href="generated_strings_devices.xml"
			message="xml" encoding="utf-8" indent="yes" media-type="text/plain">
			<x:comment>
				<x:text> generated at </x:text>
				<x:value-of select="d:date-time()" />
				<x:text> </x:text>
			</x:comment>

			<resources tools:locale="en">
				<x:apply-templates select="devices" mode="resources" />
				<x:apply-templates select="$types.xml" mode="resources" />
				<x:apply-templates select="$enums.xml" mode="resources" />
			</resources>
		</x:document>

		<x:message>generating file Device.java.part...</x:message>
		<x:document href="DeviceType.java.part"
			omit-xml-declaration="yes"
			message="text" encoding="utf-8" media-type="text/plain">
			<x:apply-templates select="devices" />
		</x:document>
	</x:template>

	<x:template name="res-string-output">
		<x:param name="name" />
		<x:variable name="res"
			select="document('generated_strings_devices.xml')/resources" />

		<x:choose>
			<x:when test="not($res/string[@name = $name])">
				<x:message>
					<x:text>! resource </x:text>
					<x:value-of select="$name" />
					<x:text> not found&#xA;</x:text>
				</x:message>
			</x:when>
			<x:when test="$VERBOSE = 'yes'">
				<x:message>
					<x:text>resource </x:text>
					<x:value-of select="$name" />
					<x:text> exists&#xA;</x:text>
				</x:message>
			</x:when>
		</x:choose>

		<x:value-of select="$name" />
	</x:template>

	<x:template match="/devices">
		<x:text>&#x9;/** BEGIN OF GENERATED CONTENT **/&#xA;</x:text>
		<x:apply-templates select="device" />
		<x:text>&#xA;</x:text>

		<x:text>&#x9;/** Version from specification of this devices list */&#xA;</x:text>
		<x:text>&#x9;public static final String DEVICES_VERSION = "2";&#xA;</x:text>
		<x:text>&#xA;</x:text>

		<x:text>&#x9;/** Generation time (GMT) of this devices list */&#xA;</x:text>
		<x:text>&#x9;public static final long DEVICES_DATE = </x:text>
		<x:value-of select="d:seconds(d:date-time())" />
		<x:text>000l;&#xA;</x:text>

		<x:text>&#xA;&#x9;/** END OF GENERATED CONTENT **/&#xA;</x:text>
	</x:template>

	<x:template match="device">
		<x:message>
			<x:text>+ device [</x:text>
			<x:value-of select="@id" />
			<x:text>] </x:text>
			<x:value-of select="name" />
		</x:message>

		<x:text>&#x9;TYPE_</x:text>
		<x:value-of select="position() - 1" />
		<x:text>(</x:text>
		<x:text>"</x:text>
		<x:value-of select="@id" />
		<x:text>", </x:text>
		
		<x:text>"</x:text>
		<x:value-of select="name" />
		<x:text>", </x:text>

		<x:text>R.string.</x:text>
		<x:call-template name="res-string-output">
			<x:with-param name="name">
				<x:text>devices__dev_</x:text>
				<x:value-of select="@id" />
				<x:text>_name</x:text>
			</x:with-param>
		</x:call-template>
		<x:text>, </x:text>

		<x:text>R.string.</x:text>
		<x:call-template name="res-string-output">
			<x:with-param name="name">
				<x:text>devices__dev_</x:text>
				<x:value-of select="@id" />
				<x:text>_vendor</x:text>
			</x:with-param>
		</x:call-template>

		<x:text>) {&#xA;</x:text>

		<x:apply-templates select="modules" />

		<x:text>&#x9;}</x:text>

		<x:if test="position() != last()">
			<x:text>,&#xA;</x:text>
		</x:if>
		<x:if test="position() = last()">
			<x:text>;&#xA;</x:text>
		</x:if>
	</x:template>

	<x:template match="modules">
		<x:text>&#x9;&#x9;@Override&#xA;</x:text>
		<x:text disable-output-escaping="yes">&#x9;&#x9;public List&#x3c;Module&#x3e; createModules(Device device) {&#xA;</x:text>
		<x:text>&#x9;&#x9;&#x9;return Arrays.asList(&#xA;</x:text>
				
		<x:apply-templates select="sensor|control" />

		<x:text>&#x9;&#x9;&#x9;);&#xA;</x:text>
		<x:text>&#x9;&#x9;}&#xA;</x:text>
	</x:template>

	<x:template match="sensor|control">
		<x:variable name="type" select="@type" />
		<x:variable name="subtype" select="@subtype" />

		<x:message>
			<x:text>  - </x:text>
			<x:value-of select="local-name()" />
			<x:text> [</x:text>
			<x:value-of select="@id" />
			<x:text>] </x:text>
			<x:value-of select="$types.xml/type[@id=$type]/name" />
		</x:message>

		<x:text>&#x9;&#x9;&#x9;&#x9;new Module(device, </x:text>

		<!-- id -->
		<x:text>"</x:text>
		<x:value-of select="@id" />
		<x:text>", </x:text>

		<!-- typeId -->
		<x:value-of select="$type" />
		<x:text>, </x:text>

		<!-- sort -->
		<x:text>null, </x:text>
		<!-- groupRes -->
		<x:text>null, </x:text>

		<!-- nameRes -->
		<x:text>R.string.</x:text>
		<x:call-template name="res-string-output">
			<x:with-param name="name">
				<x:if test="name">
					<x:text>devices__mod_</x:text>
					<x:value-of select="../../@id" />
					<x:text>_</x:text>
					<x:value-of select="@id" />
				</x:if>
				<x:if test="not(name)">
					<x:text>devices__type_</x:text>
					<x:value-of select="@type" />
				</x:if>
			</x:with-param>
		</x:call-template>
		<x:text>, </x:text>

		<!-- isActuator -->
		<x:choose>
			<x:when test="local-name() = 'sensor'">
				<x:text>false, </x:text>
			</x:when>
			<x:otherwise>
				<x:text>true, </x:text>
			</x:otherwise>
		</x:choose>

		<!-- rules -->
		<x:text>null, </x:text>

		<x:choose>
			<x:when test="$types.xml/type[@id=$type]/range">
				<x:apply-templates select="$types.xml/type[@id=$type]/range" />
			</x:when>

			<x:when test="$type = $TYPE_ENUM_ID and $enums.xml/enum[@id=$subtype]/values">
				<x:apply-templates select="$enums.xml/enum[@id=$subtype]/values" />
			</x:when>

			<x:when test="$types.xml/type[@id=$type]/values">
				<x:apply-templates select="$types.xml/type[@id=$type]/values" />
			</x:when>
		</x:choose>

		<!-- default value -->
		<x:text>null</x:text>

		<x:if test="position() != last()">
			<x:text>),&#xA;</x:text>
		</x:if>
		<x:if test="position() = last()">
			<x:text>)&#xA;</x:text>
		</x:if>
	</x:template>

	<x:template match="type/values|enum/values">
		<x:text>Arrays.asList(&#xA;</x:text>

		<x:for-each select="value">
			<x:text>&#x9;&#x9;&#x9;&#x9;&#x9;</x:text>
			<x:text>new EnumValue.Item(</x:text>

			<x:value-of select="@equals" />
			<x:text>, </x:text>

			<x:text>"</x:text>
			<x:value-of select="@equals" />
			<x:text>", </x:text>

			<x:text>R.string.</x:text>
			<x:call-template name="res-string-output">
				<x:with-param name="name">
					<x:if test="local-name(../..) = 'enum'">
						<x:text>devices__enum_</x:text>
					</x:if>
					<x:if test="local-name(../..) = 'type'">
						<x:text>devices__type_</x:text>
					</x:if>
					<x:value-of select="../../@id" />
					<x:text>_val_</x:text>
					<x:value-of select="@equals" />
				</x:with-param>
			</x:call-template>

			<x:if test="position() != last()">
				<x:text>),&#xA;</x:text>
			</x:if>
			<x:if test="position() = last()">
				<x:text>)&#xA;</x:text>
			</x:if>
		</x:for-each>

		<x:text>&#x9;&#x9;&#x9;&#x9;), </x:text>
	</x:template>

	<x:template match="type/range">
		<x:text>new BaseValue.Constraints(</x:text>

		<x:value-of select="@min" />
		<x:text>d, </x:text>

		<x:value-of select="@max" />
		<x:text>d, </x:text>

		<x:value-of select="@step" />
		<x:text>d),&#xA;</x:text>

		<x:text>&#x9;&#x9;&#x9;&#x9;</x:text>
	</x:template>

	<x:template match="devices" mode="resources">
		<x:for-each select="device">
			<string name="devices__dev_{@id}_name">
				<x:value-of select="name" />
			</string>
			<string name="devices__dev_{@id}_vendor">
				<x:value-of select="vendor" />
			</string>

			<x:for-each select="modules/sensor|modules/control">
				<x:if test="name">
					<string name="devices__mod_{../../@id}_{@id}">
						<x:value-of select="name" />
					</string>
				</x:if>
			</x:for-each>
		</x:for-each>
	</x:template>

	<x:template match="types" mode="resources">
		<string name="devices__type_unknown">unknown</string>
		<string name="devices__type_refresh">refresh</string>

		<x:for-each select="type">
			<string name="devices__type_{name}">
				<x:value-of select="translate(name, '_-/', '   ')" />
			</string>
			<string name="devices__type_{@id}">
				<x:value-of select="translate(name, '_-/', '   ')" />
			</string>

			<x:for-each select="values/value">
				<string name="devices__type_{../../@id}_val_{@equals}">
					<x:value-of select="translate(., '_-/', '   ')" />
				</string>
			</x:for-each>
		</x:for-each>
	</x:template>

	<x:template match="enums" mode="resources">
		<x:for-each select="enum/values/value">
			<string name="devices__enum_{../../@id}_val_{@equals}">
				<x:value-of select="translate(., '_-/', '   ')" />
			</string>
		</x:for-each>
	</x:template>

</x:stylesheet>
