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
<%@page import="pt.ist.applications.admissions.domain.Contest"%>
<jsp:directive.include file="headers.jsp" />

<h3 id="NoResults" style="display: none;"><spring:message code="label.search.empty" text="No available results." /></h3>

<table id="contestTable" class="table tdmiddle" style="display: none;">
	<thead>
		<tr>
			<th><spring:message code="label.applications.admissions.contest" text="Contest"/></th>
			<th><spring:message code="label.applications.admissions.contest.beginDate" text="Begin Date"/></th>
			<th><spring:message code="label.applications.admissions.contest.endDate" text="End Date"/></th>
			<th></th>
		</tr>
	</thead>
	<tbody id="contestList">
	</tbody>
</table>

<% if (Contest.canManageContests()) { %>
		<button class="btn btn-default" onclick="<%= "window.open('" + contextPath + "/admissions/createContest', '_self')" %>">
			<spring:message code="label.applications.admissions.contest.create"/>
		</button>
<% } %>

<script type="text/javascript">
	var contextPath = '<%= contextPath %>';
	var contests = ${contests};
	$(document).ready(function() {
		if (contests.length == 0) {
			document.getElementById("NoResults").style.display = 'block';
		} else {
			document.getElementById("contestTable").style.display = 'block';
		}
        $(contests).each(function(i, c) {
            row = $('<tr/>').appendTo($('#contestList'));
            row.append($('<td/>').html('<a href="' + contextPath + '/admissions/contest/' + c.id + '">' + c.contestName + '</a>'));
            row.append($('<td/>').html(c.beginDate));
            row.append($('<td/>').html(c.endDate));
            row.append($('<td/>').html(''));
        });
	});
</script>
