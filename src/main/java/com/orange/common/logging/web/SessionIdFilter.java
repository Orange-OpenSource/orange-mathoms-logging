/*
 * Copyright (C) 2017 Orange
 *
 * This software is distributed under the terms and conditions of the 'Apache-2.0'
 * license which can be found in the file 'LICENSE.txt' in this package distribution
 * or at 'http://www.apache.org/licenses/LICENSE-2.0'.
 */
package com.orange.common.logging.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.MDC;

/**
 * Both a {@link Filter servlet filter} and a {@link HttpSessionListener session listener} that adds the session ID to the logging context (
 * {@link MDC})
 * <p>
 * Requires SLF4J as the logging facade API.
 * <p>
 * With a log management system such as ELK, this will help you in incident analysis, filtering logs from a unique session.
 * <p>
 * By default the session ID MDC attribute is {@code sessionId} but can be overridden with the Java property {@code slf4j.tools.session_filter.mdc}
 * or the servlet filter configuration {@code mdc}.
 * 
 * @author pismy
 */
@WebListener
public class SessionIdFilter implements Filter, HttpSessionListener {

	private String mdcName;
	
	/**
	 * Default constructor
	 * <p>
	 * Retrieves configuration from Java properties (see class doc)
	 */
	public SessionIdFilter() {
		mdcName = System.getProperty("slf4j.tools.session_filter.mdc", "sessionId");
	}
	
	/**
	 * Filter init method
	 * <p>
	 * Loads configuration from filter configuration
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		mdcName = getConfig(filterConfig, "mdc", mdcName);
	}
	
	private String getConfig(FilterConfig filterConfig, String param, String defaultValue) {
		String valueFromConfig = filterConfig.getInitParameter(param);
		return valueFromConfig == null ? defaultValue : valueFromConfig;
	}

	/**
	 * The MDC attribute name that will be used to store the session ID
	 * <p>
	 * Default: {@code sessionId}
	 */
	public String getMdcName() {
		return mdcName;
	}

	/**
	 * The MDC attribute name that will be used to store the session ID
	 * <p>
	 * Default: {@code sessionId}
	 */
	public void setMdcName(String mdcName) {
		this.mdcName = mdcName;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpSession session = ((HttpServletRequest) request).getSession(false);
			if (session != null) {
				// attach to MDC context
				MDC.put(mdcName, session.getId());
			}
		}

		try {
			chain.doFilter(request, response);
		} finally {
			// detach from MDC context
			MDC.remove(mdcName);
		}
	}

	public void destroy() {
	}

	public void sessionCreated(HttpSessionEvent se) {
		MDC.put(mdcName, se.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		MDC.remove(mdcName);
	}
}