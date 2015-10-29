package com.novartis.opensource.yada.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.extend.RewriteRule;

/**
 * A sample of how you might write a custom rule.
 * @since PROVISIONAL
 */
public class YADARewriteRule extends RewriteRule {


    
		@Override
		public RewriteMatch matches(HttpServletRequest request, HttpServletResponse response) {

        // return null if we don't want the request
        if (!request.getRequestURI().startsWith("/staff/")) return null;

		@SuppressWarnings("unused")
		Integer id = null;
        try {
            // grab the things out of the url we need
            id = Integer.valueOf(request.getRequestURI().replaceFirst(
                "/staff/([0-9]+)/", "$1"));
        } catch (NumberFormatException e) {
            // if we don't get a good id then return null
            return null;
        }

        // match required with clean parameters
        return new YADARewriteMatch();
    }

}
