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
<link rel="stylesheet" type="text/css" href="https://cdn.rawgit.com/xdan/datetimepicker/master/jquery.datetimepicker.css"/ >
<script type="text/javascript" src="https://cdn.rawgit.com/xdan/datetimepicker/master/jquery.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/xdan/datetimepicker/master/jquery.datetimepicker.js"></script>

<div class="page-header">
	<h1>
		<spring:message code="label.applications.admissions.contest.create" text="Create New Contest"/>
	</h1>
</div>

<div class="page-body">
	<spring:url var="createContestURL" value="/admissions/createContest/save" />
	<form class="form-horizontal" action="${createContestURL}" method="POST">
		<div class="form-group">
			<label class="control-label col-sm-2" for="contestName">
				<spring:message code="label.applications.admissions.contest.name" text="label.applications.admissions.contest.name" />
			</label>
			<div class="col-sm-10">
				<input name="contestName" type="text" class="form-control" id="contestName" required="required" value=""/>
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-2" for="beginDate">
				<spring:message code="label.applications.admissions.contest.beginDate" text="label.applications.admissions.contest.beginDate" />
			</label>
			<div class="col-sm-10">
				<input name="beginDate" type="text" class="form-control" id="beginDate" required="required" value=""/>
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-2" for="endDate">
				<spring:message code="label.applications.admissions.contest.endDate" text="label.applications.admissions.contest.endDate" />
			</label>
			<div class="col-sm-10">
				<input name="endDate" type="text" class="form-control" id="endDate" required="required" value=""/>
			</div>
		</div>
		<div class="form-group">
			<div class="col-sm-10 col-sm-offset-2">
				<button id="submitRequest" class="btn btn-default">
					<spring:message code="label.create" text="Create" />
				</button>
			</div>
		</div>
	</form>
</div>

<script type="text/javascript">
	var contextPath = '<%=contextPath%>';
	$(document).ready(function() {
		jQuery('#beginDate').datetimepicker({
			format: 'Y-m-d H:i'
			});
		jQuery('#endDate').datetimepicker({
			format: 'Y-m-d H:i'
			});
	})
	$('form').submit(function () {
		$.ajax({
			url: $(this).attr('action'),
			type: 'POST',
			data: JSON.stringify($(this).serializeArray()),
			contentType: 'application/json',
			success: function (data) {
				window.open(contextPath + '/admissions', "_self");
			},
			error: function (jqXHR, textStatus, errorThrown) {
				alert('An error has occured!! :-(' + errorThrown)
			}
		})
		return false
	})
</script>