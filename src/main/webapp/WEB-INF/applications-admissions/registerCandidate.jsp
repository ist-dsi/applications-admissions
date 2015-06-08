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

<jsp:directive.include file="headers.jsp" />

<div class="page-header">
	<h1>
		<span id="contestName"></span>
		<spring:message code="label.applications.admissions.contest.candidate.register" text="Register New Candidate"/>
	</h1>
</div>

<div class="page-body">
	<form id="registerForm" class="form-horizontal" action="#" method="POST">
		<div class="form-group">
			<label class="control-label col-sm-2" for="contestName">
				<spring:message code="label.applications.admissions.contest.name" text="label.applications.admissions.contest.name" />
			</label>
			<div class="col-sm-10">
				<input name="name" type="text" class="form-control" id="name" required="required" value=""/>
			</div>
		</div>
		<div class="form-group">
			<div class="col-sm-10 col-sm-offset-2">
				<button id="submitRequest" class="btn btn-default">
					<spring:message code="label.register" text="Register" />
				</button>
			</div>
		</div>
	</form>
</div>

<script type="text/javascript">
	var contextPath = '<%=contextPath%>';
	var contest = ${contest};
	$('#contestName').html(contest.contestName);
	$('form').submit(function () {
		$.ajax({
			url: contextPath + '/admissions/contest/' + contest.id + '/registerCandidate',
			type: 'POST',
			data: JSON.stringify($(this).serializeArray()),
			contentType: 'application/json',
			success: function (data) {
				window.open(contextPath + '/admissions/contest/' + contest.id, "_self");
			},
			error: function (jqXHR, textStatus, errorThrown) {
				alert('An error has occured!! :-(' + errorThrown)
			}
		})
		return false
	})
</script>