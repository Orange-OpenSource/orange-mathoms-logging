package com.orange.common.logging.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

/**
 * A {@link Filter servlet filter} that adds a generated unique request ID to
 * the logging context ({@link MDC})
 * <p>
 * Requires SLF4J as the logging facade API.
 * <p>
 * With a log management system such as ELK, this will help you track a complete
 * callflow, filtering logs from a unique request. Quite valuable when your
 * system processes thousands of requests per second and produces terabytes of
 * logs each day...
 * <p>
 * Note that in a micro-services architecture, upon calling other services you
 * can transfer this generated {@code requestId} in a request header
 * {@code X-Track-RequestId}, thus implementing an end-to-end callflow tracking.
 * 
 * <h2>configuration</h2>
 * The request attribute, MDC attribute and request header can be overridden programmatically,
 * with filter init parameters or Java properties:
 * 
 * <table border=1>
 * <tr>
 * <th>parameter</th>
 * <th>Java property</th>
 * <th>filter init param</th>
 * <th>default value</th>
 * </tr>
 * <tr>
 * <td>request header name</td>
 * <td>{@code slf4j.tools.request_filter.header}</td>
 * <td>{@code header}</td>
 * <td>{@code X-Track-RequestId}</td>
 * </tr>
 * <tr>
 * <td>request attribute name</td>
 * <td>{@code slf4j.tools.request_filter.attribute}</td>
 * <td>{@code attribute}</td>
 * <td>{@code track.requestId}</td>
 * </tr>
 * <tr>
 * <td>MDC attribute name</td>
 * <td>{@code slf4j.tools.request_filter.mdc}</td>
 * <td>{@code mdc}</td>
 * <td>{@code requestId}</td>
 * </tr>
 * </table>
 * 
 * <h2>web.xml configuration example</h2>
 * 
 * <pre style="font-size: medium">
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;web-app&gt;
 * 
 *   &lt;!-- filter declaration with init params --&gt;
 *   &lt;filter&gt;
 *     &lt;filter-name&gt;RequestIdFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;com.orange.experts.utils.logging.web.RequestIdFilter&lt;/filter-class&gt;
 *     &lt;init-param&gt;
 *       &lt;!-- example: request id is passed by Apache mod_unique_id --&gt;
 *       &lt;param-name&gt;header&lt;/param-name&gt;
 *       &lt;param-value&gt;UNIQUE_ID&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *   &lt;/filter&gt;
 *   
 *   &lt;!-- filter mapping --&gt;
 *   &lt;filter-mapping&gt;
 *     &lt;filter-name&gt;RequestIdFilter&lt;/filter-name&gt;
 *     &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *   &lt;/filter-mapping&gt;
 * &lt;/web-app&gt;
 * </pre>
 * 
 * @author pismy
 */
public class RequestIdFilter implements Filter {

	private String headerName;
	private String attributeName;
	private String mdcName;

	/**
	 * Default constructor
	 * <p>
	 * Retrieves configuration from Java properties (see class doc)
	 */
	public RequestIdFilter() {
		headerName = System.getProperty("slf4j.tools.request_filter.header", "X-Track-RequestId");
		attributeName = System.getProperty("slf4j.tools.request_filter.attribute", "track.requestId");
		mdcName = System.getProperty("slf4j.tools.request_filter.mdc", "requestId");
	}
	
	/**
	 * Filter init method
	 * <p>
	 * Loads configuration from filter configuration
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		headerName = getConfig(filterConfig, "header", headerName);
		attributeName = getConfig(filterConfig, "attribute", attributeName);
		mdcName = getConfig(filterConfig, "mdc", mdcName);
	}
	
	private String getConfig(FilterConfig filterConfig, String param, String defaultValue) {
		String valueFromConfig = filterConfig.getInitParameter(param);
		return valueFromConfig == null ? defaultValue : valueFromConfig;
	}

	/**
	 * The (incoming) request header name specifying the request ID
	 * <p>
	 * That can be used when chaining several tiers, for end-to-end callflow tracking.
	 * <p>
	 * Default: {@code X-Track-RequestId}
	 */
	public String getHeaderName() {
		return headerName;
	}

	/**
	 * The (incoming) request header name specifying the request ID
	 * <p>
	 * That can be used when chaining several tiers, for end-to-end callflow tracking.
	 * <p>
	 * Default: {@code X-Track-RequestId}
	 */
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	/**
	 * The internal attribute name where the request ID will be stored
	 * <p>
	 * Default: {@code track.requestId}
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * The internal attribute name where the request ID will be stored
	 * <p>
	 * Default: {@code track.requestId}
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * The MDC attribute name that will be used to store the request ID
	 * <p>
	 * Default: {@code requestId}
	 */
	public String getMdcName() {
		return mdcName;
	}

	/**
	 * The MDC attribute name that will be used to store the request ID
	 * <p>
	 * Default: {@code requestId}
	 */
	public void setMdcName(String mdcName) {
		this.mdcName = mdcName;
	}

	/**
	 * Filter implementation
	 * <ul>
	 * <li>checks whether the current request has an attached request id,
	 * <li>if not, tries to get one from request headers (implements end-to-end
	 * callflow traceability),
	 * <li>if not, generates one
	 * <li>attaches it to the request (as an attribute) and to the {@link MDC}
	 * context.
	 * </ul>
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// checks whether the current request has an attached request id
		String reqId = (String) request.getAttribute(attributeName);
		if (reqId == null) {
			// retrieve id from request headers
			if (request instanceof HttpServletRequest) {
				reqId = ((HttpServletRequest) request).getHeader(headerName);
			}
			if (reqId == null) {
				// no requestId (either from attributes or headers): generate
				// one
				reqId = Long.toHexString(System.nanoTime());
			}
			// attach to request
			request.setAttribute(attributeName, reqId);
		}

		// attach to MDC context
		MDC.put(mdcName, reqId);

		try {
			chain.doFilter(request, response);
		} finally {
			// remove from MDC context
			MDC.remove(mdcName);
		}
	}

	public void destroy() {
	}
}