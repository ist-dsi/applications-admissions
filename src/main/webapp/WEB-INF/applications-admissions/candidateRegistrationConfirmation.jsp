<%--
    Copyright Â© 2014 Instituto Superior Técnico

    This file is part of Applications and Admissions Module.

    Applications and Admissions Module is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Applications and Admissions Module is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MGP Viewer.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="org.joda.time.DateTime"%>
<%@page import="pt.ist.fenixframework.FenixFramework"%>
<%@page import="com.google.gson.JsonObject"%>
<%@page import="pt.ist.applications.admissions.domain.Contest"%>
<jsp:directive.include file="headers.jsp" />

<div class="page-header">
	<h1>
		<spring:message code="label.applications.admissions.contest"/>
		<span id="contestName"></span>
	</h1>
</div>
	<%
	    final JsonObject contestJson = (JsonObject) request.getAttribute("contest");
	    final Contest contest = FenixFramework.getDomainObject(contestJson.get("id").getAsString());
	%>
	<spring:message code="label.applications.admissions.contest.candidate.registration.confirmation"/>

<script type="text/javascript">
	var contest = ${contest};
	$(document).ready(function() {
		$('#contestName').html(contest.contestName);
	});
</script>