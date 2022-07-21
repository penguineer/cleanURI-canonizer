package com.penguineering.cleanuri.canonizer.processors;

import java.net.URI;

import io.micronaut.context.annotation.Bean;
import net.jcip.annotations.ThreadSafe;
import ru.lanwen.verbalregex.VerbalExpression;

@ThreadSafe
@Bean
public class AmazonCanonizer implements Canonizer {
	public static final String PREFIX = "http://www.amazon.de/dp/";
	static final VerbalExpression idRegex = VerbalExpression.regex().startOfLine().anything().then("/dp/").capture()
			.anything().endCapture().then("/").anything().endOfLine().build();

	public AmazonCanonizer() {
	}

	@Override
	public boolean isSuitable(URI uri) {
		if (uri == null)
			throw new IllegalArgumentException("URI argument must not be null!");

		final String authority = uri.getAuthority();

		return authority != null && authority.endsWith("amazon.de");
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
