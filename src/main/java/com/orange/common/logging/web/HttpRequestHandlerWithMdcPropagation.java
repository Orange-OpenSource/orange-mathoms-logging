package com.orange.common.logging.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Apache {@link HttpRequestHandler} that enables propagating part or all of the
 * {@link MDC} context over Http requests
 * <p>
 * Requires SLF4J as the logging facade API and Apache Http as the client Http
 * stack.
 * <p>
 * By default propagates {@code requestId} MDC attribute set by the
 * {@link RequestIdFilter} as {@code X-Track-RequestId} header but it can be
 * overridden using the {@code slf4j.tools.propagate.mdc.fields} Java property
 * or using the non-default constructor.
 * 
 * @author pismy
 *
 */
public class HttpRequestHandlerWithMdcPropagation implements HttpRequestHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestHandlerWithMdcPropagation.class);
	private final Map<String, String> mdcName2HeaderName;

	/**
	 * Default constructor
	 * <p>
	 * Retrieves propagated MDC context attributes configuration through the
	 * {@code slf4j.tools.propagate.mdc.fields} Java property formatted as:
	 * 
	 * <pre class=code>
	 * &lt;mdc name 1&gt;: &lt;header name 1&gt;, &lt;mdc name 2&gt;: &lt;header name 2&gt;, &lt;mdc name 3&gt;: &lt;header name 3&gt;
	 * </pre>
	 * 
	 * Example:
	 * 
	 * <pre class=code>
	 * requestId: X-Track-RequestId, sessionId: X-Track-SessionId, time: X-Track-Time
	 * </pre>
	 * 
	 * <p>
	 * Default:
	 * 
	 * <pre class=code>
	 * requestId: X-Track-RequestId
	 * </pre>
	 * 
	 */
	public HttpRequestHandlerWithMdcPropagation() {
		mdcName2HeaderName = new HashMap<>();
		String mdcFieldsConfig = System.getProperty("slf4j.tools.propagate.mdc.fields", "requestId: X-Track-RequestId").trim();
		String[] mdcFields = mdcFieldsConfig.split(",");
		for (String mdc2Header : mdcFields) {
			int idx = mdc2Header.indexOf(':');
			if (idx <= 0) {
				LOGGER.warn("Unexpected configuration format in '{}': items should be formatted as '<mdc name>: <header name>'", mdcFieldsConfig);
			} else {
				String mdc = mdc2Header.substring(0, idx).trim();
				String header = mdc2Header.substring(idx + 1).trim();
				if (mdc.isEmpty() || header.isEmpty()) {
					LOGGER.warn("Unexpected configuration format in '{}': items should be formatted as '<mdc name>: <header name>'", mdcFieldsConfig);
				} else {
					mdcName2HeaderName.put(mdc, header);
				}
			}
		}
	}

	/**
	 * Constructor that allows configuring MDC attributes to propagate as a map.
	 * 
	 * @param mdcName2HeaderName
	 *            Map of MDC attributes to propagate. Key: MDC attribute name;
	 *            value: Http request header name
	 */
	public HttpRequestHandlerWithMdcPropagation(Map<String, String> mdcName2HeaderName) {
		this.mdcName2HeaderName = mdcName2HeaderName;
	}

	@Override
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
		for (Entry<String, String> e : mdcName2HeaderName.entrySet()) {
			String mdcValue = MDC.get(e.getKey());
			if (mdcValue != null) {
				request.addHeader(new BasicHeader(e.getValue(), mdcValue));
			}
		}
	}
}
