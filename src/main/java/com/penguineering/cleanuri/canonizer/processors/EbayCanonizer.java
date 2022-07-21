package com.penguineering.cleanuri.canonizer.processors;

import java.net.URI;

import io.micronaut.context.annotation.Bean;
import net.jcip.annotations.ThreadSafe;
import ru.lanwen.verbalregex.VerbalExpression;

@ThreadSafe
@Bean
public class EbayCanonizer implements Canonizer {
	public static final String PREFIX = "https://www.ebay.de/itm/";

	static final VerbalExpression idRegex = VerbalExpression.regex().startOfLine().then("http").anything()
			.then("://www.ebay.de/").anything().then("/").capture().anything().endCapture().then("?").anything().endOfLine().build();

	public EbayCanonizer() {
	}

	@Override
	public boolean isSuitable(URI uri) {
		if (uri == null)
			throw new IllegalArgumentException("URI argument must not be null!");

		final String authority = uri.getAuthority();

		return authority != null && authority.endsWith("ebay.de");
	}

	@Override
	public URI canonize(URI uri) {
		if (!this.isSuitable(uri))
			throw new IllegalArgumentException("URI does not match this site!");

		final String uriStr = uri.toASCIIString();

		if (!idRegex.test(uriStr))
			throw new IllegalArgumentException("URI does not fit the expected structure.");

		final String id = idRegex.getText(uriStr, 1);

		return URI.create(PREFIX + id);
	}

}
