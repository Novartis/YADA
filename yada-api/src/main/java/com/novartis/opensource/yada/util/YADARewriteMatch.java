package com.novartis.opensource.yada.util;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;


/**
 * A sample of how you might write a custom match.
 * @since PROVISIONAL
 */
class YADARewriteMatch extends RewriteMatch {
    

    /**
     * Default constructor
     */
    YADARewriteMatch() 
    {
        
    }

    
		@Override
		public boolean execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        // lookup things in the db based on id

        // do something like forward to a jsp
        request.setAttribute("YADARewriteMatch", this);
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/some-view.jsp");
        rd.forward(request, response);
        // in the jsp you can use request.getAttribute("sampleRewriteMatch") to fetch this object

        return true;
    }
}