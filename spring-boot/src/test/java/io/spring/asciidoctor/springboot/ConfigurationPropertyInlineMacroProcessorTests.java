/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.asciidoctor.springboot;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@code ConfigurationPropertyInlineMacroProcessor}.
 *
 * @author Andy Wilkinson
 */
class ConfigurationPropertyInlineMacroProcessorTests {

	private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

	private final List<LogRecord> logRecords = new ArrayList<>();

	ConfigurationPropertyInlineMacroProcessorTests() {
		this.asciidoctor.registerLogHandler(this.logRecords::add);
	}

	@Test
	void whenPropertyThatExistsIsReferencedADebugMessageIsLogged() {
		assertThat(convert("Using the property configprop:example.property.alpha[]"))
			.contains("<code>example.property.alpha</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.DEBUG);
	}

	@Test
	void whenPropertyIsWithinAMapPropertyThatExistsADebugMessageIsLogged() {
		assertThat(convert("configprop:example.property.delta.a.b.c[]"))
			.contains("<code>example.property.delta.a.b.c</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.DEBUG);
		assertThat(this.logRecords).extracting(LogRecord::getMessage)
			.containsExactly("Configuration property 'example.property.delta.a.b.c' successfully validated.");
	}

	@Test
	void whenPropertyIsReferencedAndEnvvarFormatIsSpecifiedTheOutputIsAllUpperCase() {
		assertThat(convert("Using the property configprop:example.property.alpha[format=envvar]"))
			.contains("<code>EXAMPLE_PROPERTY_ALPHA</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.DEBUG);
	}

	@Test
	void whenPropertyIsWithinAMapPropertyAndEnvvarFormatIsSpecifiedTheOutputIsAllUpperCase() {
		assertThat(convert("configprop:example.property.delta.a.b.c[format=envvar]"))
			.contains("<code>EXAMPLE_PROPERTY_DELTA_A_B_C</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.DEBUG);
		assertThat(this.logRecords).extracting(LogRecord::getMessage)
			.containsExactly("Configuration property 'example.property.delta.a.b.c' successfully validated.");
	}

	@Test
	void whenConversionIsPerformedMultipleTimesThenFormatAttributeIsHonoured() {
		assertThat(convert("Using the property configprop:example.property.alpha[format=envvar]"))
			.contains("<code>EXAMPLE_PROPERTY_ALPHA</code>");
		assertThat(convert("Using the property configprop:example.property.alpha[format=envvar]"))
			.contains("<code>EXAMPLE_PROPERTY_ALPHA</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.DEBUG, Severity.DEBUG);
	}

	@Test
	void whenDeprecatedPropertyIsReferencedAsDeprecatedAndEnvvarFormatIsSpecifiedTheOutputIsAllUpperCase() {
		assertThat(convert("Using the property configprop:example.property.bravo[deprecated,format=envvar]"))
			.contains("<code>EXAMPLE_PROPERTY_BRAVO</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.DEBUG);
	}

	@Test
	void whenPropertyThatDoesNotExistIsReferencedAWarningIsLogged() {
		assertThat(convert("configprop:does.not.exist[]")).contains("<code>does.not.exist</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.WARN);
		assertThat(this.logRecords).extracting(LogRecord::getMessage)
			.containsExactly("Configuration property 'does.not.exist' not found.");
	}

	@Test
	void whenPropertyThatIsDeprecatedIsReferencedAsDeprecatedADebugMessageIsLogged() {
		assertThat(convert("configprop:example.property.bravo[deprecated]"))
			.contains("<code>example.property.bravo</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.DEBUG);
		assertThat(this.logRecords).extracting(LogRecord::getMessage)
			.containsExactly("Configuration property 'example.property.bravo' successfully validated.");
	}

	@Test
	void whenPropertyThatIsDeprecatedIsReferencedAWarningIsLogged() {
		assertThat(convert("configprop:example.property.bravo[]")).contains("<code>example.property.bravo</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.WARN);
		assertThat(this.logRecords).extracting(LogRecord::getMessage)
			.containsExactly("Configuration property 'example.property.bravo' is deprecated.");
	}

	@Test
	void whenPropertyThatIsNotDeprecatedIsReferencedAsDeprecatedAWarningIsLogged() {
		assertThat(convert("configprop:example.property.charlie[deprecated]"))
			.contains("<code>example.property.charlie</code>");
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactly(Severity.WARN);
		assertThat(this.logRecords).extracting(LogRecord::getMessage)
			.containsExactly("Configuration property 'example.property.charlie' is not deprecated.");
	}

	private String convert(String source) {
		Options options = new Options();
		options.setSafe(SafeMode.SERVER);
		return this.asciidoctor.convert(source, options);
	}

}
