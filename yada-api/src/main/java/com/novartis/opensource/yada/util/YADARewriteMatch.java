/**
 * Copyright 2016 Novartis Institutes for BioMedical Research Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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