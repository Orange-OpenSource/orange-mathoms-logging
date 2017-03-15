package com.orange.common.logging.web;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

/**
 * A {@link Filter servlet filter} that adds the user {@link Principal} to the
 * logging context information (through {@link MDC})
 * <p>
 * If the principal name is a personal user information (such as login or email
 * address), it is recommended not to add it "as-is" to the logging context, but
 * generate a hash of it. This filter allows specifying a hashing algorithm
 * (none by default).
 * 
 * <h2>configuration</h2>
 * The hashing algorithm, MDC attribute and request attribute have default
 * values, but can be overridden programmatically, with filter init parameters
 * or Java properties:
 * 
 * <table border=1>
 * <tr>
 * <th>parameter</th>
 * <th>Java property</th>
 * <th>filter init param</th>
 * <th>default value</th>
 * </tr>
 * <tr>
 * <td>hashing algorithm</td>
 * <td>{@code slf4j.tools.principal_filter.hash_algorithm}</td>
 * <td>{@code hash_algorithm}</td>
 * <td>{@code none}</td>
 * </tr>
 * <tr>
 * <td>request attribute name</td>
 * <td>{@code slf4j.tools.principal_filter.attribute}</td>
 * <td>{@code attribute}</td>
 * <td>{@code track.userId}</td>
 * </tr>
 * <tr>
 * <td>MDC attribute name</td>
 * <td>{@code slf4j.tools.principal_filter.mdc}</td>
 * <td>{@code mdc}</td>
 * <td>{@code userId}</td>
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
 *     &lt;filter-name&gt;PrincipalFilter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;com.orange.experts.utils.logging.web.PrincipalFilter&lt;/filter-class&gt;
 *     &lt;init-param&gt;
 *       &lt;!-- example: SHA1 hashed principal --&gt;
 *       &lt;param-name&gt;hash_algorithm&lt;/param-name&gt;
 *       &lt;param-value&gt;SHA-1&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 *   &lt;/filter&gt;
 *   
 *   &lt;!-- filter mapping --&gt;
 *   &lt;filter-mapping&gt;
 *     &lt;filter-name&gt;PrincipalFilter&lt;/filter-name&gt;
 *     &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *   &lt;/filter-mapping&gt;
 * &lt;/web-app&gt;
 * </pre>
 * 
 * @author pismy
 *
 */
public class PrincipalFilter implements Filter {

	private String hashAlgorithm;
	private String attributeName;
	private String mdcName;

	public PrincipalFilter() throws NoSuchAlgorithmException {
		setHashAlgorithm(System.getProperty("slf4j.tools.principal_filter.hash_algorithm", "none"));
		attributeName = System.getProperty("slf4j.tools.principal_filter.attribute", "track.userId");
		mdcName = System.getProperty("slf4j.tools.principal_filter.mdc", "userId");
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		try {
			setHashAlgorithm(getConfig(filterConfig, "hash_algorithm", System.getProperty("slf4j.tools.principal_filter.hash_algorithm", hashAlgorithm)));
		} catch (NoSuchAlgorithmException e) {
			throw new ServletException(e);
		}
		attributeName = getConfig(filterConfig, "attribute", attributeName);
		mdcName = getConfig(filterConfig, "mdc", mdcName);
	}

	private String getConfig(FilterConfig filterConfig, String param, String defaultValue) {
		String valueFromConfig = filterConfig.getInitParameter(param);
		return valueFromConfig == null ? defaultValue : valueFromConfig;
	}

	/**
	 * The internal attribute name where the principal will be stored
	 * <p>
	 * Default: {@code track.userId}
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * The internal attribute name where the principal will be stored
	 * <p>
	 * Default: {@code track.userId}
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * The MDC attribute name that will be used to store the principal
	 * <p>
	 * Default: {@code userId}
	 */
	public String getMdcName() {
		return mdcName;
	}

	/**
	 * The MDC attribute name that will be used to store the principal
	 * <p>
	 * Default: {@code userId}
	 */
	public void setMdcName(String mdcName) {
		this.mdcName = mdcName;
	}

	/**
	 * The algorithm to use to hash the principal name
	 * <p>
	 * Supports:
	 * <ul>
	 * <li>{@code none}: the principal will be added "as-is" to the MDC context
	 * <li>{@code hashcode}: pseudo-hashing that simply computes the hashcode of
	 * the principal name (encoded in hexadecimal)
	 * <li>any other value: will use it as a {@link MessageDigest} algorithm
	 * </ul>
	 * <p>
	 * Default: {@code none}
	 * 
	 * @throws NoSuchAlgorithmException
	 *             if the specified algorithm does not exist
	 */
	public void setHashAlgorithm(String hashAlgorithm) throws NoSuchAlgorithmException {
		if (hashAlgorithm == null || "none".equalsIgnoreCase(hashAlgorithm)) {
			this.hashAlgorithm = hashAlgorithm;
		} else if ("hashcode".equalsIgnoreCase(hashAlgorithm)) {
			this.hashAlgorithm = hashAlgorithm;
		} else {
			// test the algorithm exists
			MessageDigest.getInstance(hashAlgorithm);
			this.hashAlgorithm = hashAlgorithm;
		}
	}

	/**
	 * The algorithm to use to hash the principal name
	 * <p>
	 * Supports:
	 * <ul>
	 * <li>{@code none}: the principal will be added "as-is" to the MDC context
	 * <li>{@code hashcode}: pseudo-hashing that simply computes the hashcode of
	 * the principal name (encoded in hexadecimal)
	 * <li>any other value: will use it as a {@link MessageDigest} algorithm
	 * </ul>
	 * <p>
	 * Default: {@code none}
	 */
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// retrieve userId and set
		if (request instanceof HttpServletRequest) {
			Principal principal = ((HttpServletRequest) request).getUserPrincipal();
			if (principal != null) {
				String ppal = principal.getName();
				if (hashAlgorithm == null || "none".equalsIgnoreCase(hashAlgorithm)) {
					// no hash
				} else if ("hashcode".equalsIgnoreCase(hashAlgorithm)) {
					// simply hashcode
					ppal = Strings.padStart(Integer.toHexString(ppal.hashCode()), 8, '0');
				} else {
					// hexadecimal hash
					try {
						MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
						ppal = BaseEncoding.base16().encode(digest.digest(ppal.getBytes()));
					} catch (NoSuchAlgorithmException e) {
						throw new ServletException(e);
					}
				}
				// add to MDC and request attribute
				MDC.put(mdcName, ppal);
				request.setAttribute(attributeName, ppal);
			}
		}

		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove(mdcName);
		}
	}

	public void destroy() {
	}
}