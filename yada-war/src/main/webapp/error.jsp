<%@ page isErrorPage="true" contentType="application/json;charset=UTF-8"%>
<%--
@author Dave Varon
@since 0.6.3.0
 --%>
<%
    out.println(request.getSession().getAttribute("YADAException"));
    request.getSession().removeAttribute("YADAException");
%>
