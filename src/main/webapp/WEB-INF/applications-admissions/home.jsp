<%--
    Copyright © 2014 Instituto Superior T�cnico

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

<style type="text/css" title="currentStyle">

thead tr .header {
	background-image: url();
	background-repeat: no-repeat;
	background-position: center right;
	cursor: pointer;
}
</style>

<% if (Contest.canManageContests()) { %>
		<button class="btn btn-default" onclick="<%= "window.open('" + contextPath + "/admissions/createContest', '_self')" %>">
			<spring:message code="label.applications.admissions.contest.create"/>
		</button>
<% } %>
<p></p>
<p></p>
<h3 id="NoResults" style="display: none;"><spring:message code="label.search.empty" text="No available results." /></h3>

<table id="contestTable" class="table tdmiddle" style="display: none;">
	<thead>
		<tr>
			<th class="header" id="sTitle"><a><spring:message code="label.applications.admissions.contest" text="Contest"/></a></th>
			<th class="header" id="sBegin"><a><spring:message code="label.applications.admissions.contest.beginDate" text="Begin Date"/></a></th>
			<th class="header" id="sEnd"><a><spring:message code="label.applications.admissions.contest.endDate" text="End Date"/></a></th>
			<th></th>
		</tr>
	</thead>
	<tbody id="contestList">
	</tbody>
</table>


<script type="text/javascript">
	var contextPath = '<%= contextPath %>';
	var contests = ${contests};
	var table = document.getElementById('contestTable');
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
        makeSortable();
		});

	 	function makeSortable() {
	        var th = table.tHead, i;
	        th && (th = th.rows[0]) && (th = th.cells);
	        if (th) i = th.length;
	        else return; // if no `<thead>` then do nothing
	        while (--i >= 0) (function (i) {
	            var dir = 1;
	            th[i].addEventListener('click', function (e) {e.preventDefault();sortTable(table, i, (dir = 1 - dir))});
	        }(i));
	    }
	 	 function sortTable(table, col, reverse) {
		        var tb = table.tBodies[0], // use `<tbody>` to ignore `<thead>` and `<tfoot>` rows
		            tr = Array.prototype.slice.call(tb.rows, 0), // put rows into array
		            i;
		        reverse = -((+reverse) || -1);
		        tr = tr.sort(function (a, b) { // sort rows
		            return reverse // `-1 *` if want opposite order
		                * (a.cells[col].textContent.trim() // using `.textContent.trim()` for test
		                    .localeCompare(b.cells[col].textContent.trim())
		                   );
		        });
		        for(i = 0; i < tr.length; ++i) tb.appendChild(tr[i]); // append each row in order
		    }
	

</script>
