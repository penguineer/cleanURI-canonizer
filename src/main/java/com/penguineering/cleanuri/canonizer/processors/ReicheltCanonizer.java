package com.penguineering.cleanuri.canonizer.processors;

import java.net.URI;

import io.micronaut.context.annotation.Bean;
import net.jcip.annotations.ThreadSafe;
import ru.lanwen.verbalregex.VerbalExpression;

/**
 * Canonizer for the Reichelt online catalogue.
 */
@ThreadSafe
@Bean
public class ReicheltCanonizer implements Canonizer {
	public static final String PREFIX = "http://www.reichelt.de/index.html?ARTICLE=";

	static final VerbalExpression artidRegex = VerbalExpression.regex().startOfLine().then("http").anything()
			.then("://www.reichelt.de/").anything().then("-p").capture().anything().endCapture().then(".html")
			.anything().endOfLine().build();

	static final VerbalExpression artidRegex2 = VerbalExpression.regex().startOfLine().then("http").anything()
			.then("://www.reichelt.de/index.html?ARTICLE=").capture().anything().endCapture().endOfLine().build();

	public ReicheltCanonizer() {
	}

	@Override
	public boolean isSuitable(URI uri) {
		if (uri == null)
			throw new IllegalArgumentException("URI argument must not be null!");

		final String authority = uri.getAuthority();

		return authority != null && authority.endsWith("reichelt.de") &&
				(artidRegex.test(uri.toASCIIString()) || artidRegex2.test(uri.toASCIIString()));
	}

	@Override
	public URI canonize(URI uri) {
		if (!this.isSuitable(uri))
			throw new IllegalArgumentException("URI does not match this site!");

		final String ART_id = getArticleID(uri);

		return URI.create(PREFIX + ART_id);
	}

	private String getArticleID(URI uri) {
		final String artid;

		if (artidRegex.test(uri.toASCIIString()))
			artid = artidRegex.getText(uri.toASCIIString(), 1);
		else if (artidRegex2.test(uri.toASCIIString()))
			artid = artidRegex2.getText(uri.toASCIIString(), 1);
		else
			artid = null;

		return artid;
	}

}
