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
<link rel="stylesheet" type="text/css" href="https://cdn.rawgit.com/xdan/datetimepicker/master/jquery.datetimepicker.css"/ >
<script type="text/javascript" src="https://cdn.rawgit.com/xdan/datetimepicker/master/jquery.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/xdan/datetimepicker/master/jquery.datetimepicker.js"></script>

<div class="page-header">
	<h1>
		<spring:message code="label.applications.admissions.contest"/>
		<span id="contestName"></span>
	</h1>
	<h4>
		<span>
			<span id="contestBeginDate" style="color: gray;"></span>
			-
			<span id="contestEndDate" style="color: gray;"></span>
		</span>
	</h4>
	<%
	    final JsonObject contestJson = (JsonObject) request.getAttribute("contest");
	    final Contest contest = FenixFramework.getDomainObject(contestJson.get("id").getAsString());
	%>
	<%
	    if (Contest.canManageContests()) {
	%>
	<table class="table" style="margin-top: 10px; margin-bottom: 0px;">
		<tr>
			<td>
				<button class="btn btn-default" onclick="showDeleteContest();">
					<spring:message code="label.link.delete.contest" text="Delete Contest"/>
				</button>
				<button class="btn btn-default" onclick="showEditContest();">
					<spring:message code="label.link.edit.contest" text="Edit Contest"/>
				</button>
			</td>
			<td colspan="2">
			</td>
		</tr>
		<tr>
			<td><label class="control-label" for="editLink"
				style="margin-right: 10px;"> <spring:message
						code="label.applications.admissions.contest.jury.link"
						text="Candidate Link" />
			</label> <span id="editLink"></span> <span id="noEditLink"> <spring:message
						code="label.link.not.available" text="Not Available" />
			</span></td>
			<td>
				<div id="undispose">
					<form method="POST"
						action="<%=contextPath + "/admissions/contest/" + contest.getExternalId() + "/undispose"%>">
						<button class="btn btn-default">
							<spring:message code="label.undispose" text="Undispose" />
						</button>
					</form>
				</div>
			</td>
			<td>
				<form method="POST"
					action="<%=contextPath + "/admissions/contest/" + contest.getExternalId() + "/generateLink"%>">
					<button class="btn btn-default">
						<spring:message code="label.link.generate.new"
							text="Generate New Link" />
					</button>
				</form>
			</td>
		</tr>
	</table>
	<div id="deleteContest" style="display: none; padding-left: 20px; padding-right: 20px;">
		<div class="warning-border">
			<h3 style="background-color: #DE2C2C; color: white; margin: 10px; padding: 10px;">
				<spring:message code="label.link.delete.contest" text="Delete Contest"/>
			</h3>
			<p style="margin-left: 50px; margin-right: 50px; font-size: medium;">
				<spring:message code="label.link.delete.contest.warning" text="Beware this operation cannot be reversed"/>
				<br/>
				<spring:message code="label.link.delete.contest.warning.input" text="To delete the contest input the contest name into the following box."/>
				<br/>
				<form method="POST" action="<%= contextPath + "/admissions/contest/" + contest.getExternalId() + "/delete" %>"
						style="margin-left: 50px;">
					<input id="checkContestName" type="text" name="contestName" size="50" onchange="checkActivateButton();"/>
					<button id="deleteButton" class="btn btn-default warning-border" onclick="return deleteContest();" disabled="disabled" style="background-color: #DE2C2C; color: white;">
						<spring:message code="label.link.delete.contest" text="Delete Contest"/>
					</button>
				</form>
			</p>
		</div>
	</div>
	<div id="editContest" style="display: none; padding-left: 20px; padding-right: 20px;">
		<div>
			<h3 style="margin: 10px; padding: 10px;">
				<spring:message code="label.link.edit.contest" text="Edit Contest"/>
			</h3>
			<p style="margin-left: 50px; margin-right: 50px; font-size: medium;">
				<form method="POST" action="<%= contextPath + "/admissions/contest/" + contest.getExternalId() + "/edit" %>"
						style="margin-left: 50px;" class="form-horizontal" id="editForm">
					<div class="form-group">
						<label class="control-label col-sm-2" for="contestName">
							<spring:message code="label.applications.admissions.contest.name" text="label.applications.admissions.contest.name" />
						</label>
						<div class="col-sm-10">
							<input name="contestName" type="text" class="form-control" id="contestName" required="required" value="<%= contest.getContestName() %>"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-sm-2" for="beginDate">
							<spring:message code="label.applications.admissions.contest.beginDate" text="label.applications.admissions.contest.beginDate" />
						</label>
						<div class="col-sm-10">
							<% final DateTime bdt = contest.getBeginDate(); %>
							<input name="beginDate" type="text" class="form-control" id="beginDate" required="required" value="<%= bdt == null ? "" : bdt.toString("yyyy-MM-dd HH:mm") %>"/>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label col-sm-2" for="endDate">
							<spring:message code="label.applications.admissions.contest.endDate" text="label.applications.admissions.contest.endDate" />
						</label>
						<div class="col-sm-10">
							<% final DateTime edt = contest.getEndDate(); %>
							<input name="endDate" type="text" class="form-control" id="endDate" required="required" value="<%= edt == null ? "" : edt.toString("yyyy-MM-dd HH:mm") %>"/>
						</div>
					</div>
					<div class="form-group">
						<div class="col-sm-10 col-sm-offset-2">
							<button id="submitRequest" class="btn btn-default">
								<spring:message code="label.link.edit.contest" text="Edit Contest"/>
							</button>
						</div>
					</div>
				</form>
			</p>
		</div>
	</div>
	<% } %>
</div>

<h3 id="NoResults" style="display: none;"><spring:message code="label.applications.none" text="No available results." /></h3>

<table id="candidateTable" class="table tdmiddle" style="display: none;">
	<thead>
		<tr>
			<th><spring:message code="label.applications.admissions.candidate.number" text="Candidatura"/></th>
			<th><spring:message code="label.applications.admissions.candidate" text="Candidato"/></th>
			<th></th>
		</tr>
	</thead>
	<tbody id="candidateList">
	</tbody>
</table>

<% if (Contest.canManageContests() || contest.isInPeriod()) { %>
	<div>
		<button class="btn btn-default" onclick="goToRegisterCandidate()">
			<spring:message code="label.applications.admissions.contest.candidate.register"/>
		</button>
		<% if (Contest.canManageContests()) {%>
			<button class="btn btn-default" onclick="goToExportContestCandidates()">
				<spring:message code="label.applications.admissions.contest.export.applications"/>
			</button>
 	  	<% } %>      
	</div>
<% } %>

<script type="text/javascript">
	var contextPath = '<%= contextPath %>';
	var contest = ${contest};
	var candidates = contest.candidates;
	var hashArg = '<%= request.getParameter("hash") == null ? "" : "?hash=" + request.getParameter("hash")%>';
	$(document).ready(function() {
		$('#contestName').html(contest.contestName);
		$('#contestBeginDate').html(contest.beginDate);
		$('#contestEndDate').html(contest.endDate);

		if (candidates.length == 0) {
			document.getElementById("NoResults").style.display = 'block';
		} else {
			document.getElementById("candidateTable").style.display = 'block';
		}
        $(candidates).each(function(i, c) {
            row = $('<tr/>').appendTo($('#candidateList'));
            row.append($('<td/>').html(c.candidateNumber));
            row.append($('<td/>').html('<a href="' + contextPath + '/admissions/candidate/' + c.id + hashArg + '">' + c.name + '</a>'));
            row.append($('<td/>').html('<a href="' + contextPath + '/admissions/candidate/' + c.id + '/download' + hashArg + '" class="btn btn-default">Download</a>'));
        });
	});
	function goToRegisterCandidate() {
		window.open(contextPath + '/admissions/contest/' + contest.id + '/registerCandidate', '_self');
	}
	
	function goToExportContestCandidates() {
		window.open(contextPath + '/admissions/contest/' + contest.id + '/exportContestCandidates', '_self');
	}
</script>
<% if (Contest.canManageContests()) { %>
	<script type="text/javascript">
		$(document).ready(function() {
			var contest = ${contest};
			var candidateLink = location.protocol + '//' + location.hostname + ':' + location.port + contextPath + '/admissions/contest/' + contest.id;
			$('#linkUndispose').attr("href", candidateLink + '/undispose');
			$('#linkGenerate').attr("href", candidateLink + '/generateLinks');
			var editLink = candidateLink + '?hash=' + contest.viewHash;
			if (contest.viewHash) {
				$('#editLink').html('<a href="' + editLink + '">' + editLink + '</a>');
				document.getElementById("noEditLink").style.visibility = "hidden";
			} else {
				document.getElementById("undispose").style.visibility = "hidden";
			}
			jQuery('#beginDate').datetimepicker({
				format: 'Y-m-d H:i'
				});
			jQuery('#endDate').datetimepicker({
				format: 'Y-m-d H:i'
				});
			$('#editForm').submit(function () {
				$.ajax({
					url: $(this).attr('action'),
					type: 'POST',
					data: JSON.stringify($(this).serializeArray()),
					contentType: 'application/json',
					success: function (data) {
						location.reload(true);
					},
					error: function (jqXHR, textStatus, errorThrown) {
						alert('An error has occured!! :-(' + errorThrown)
					}
				})
				return false
			});
		});
		function showDeleteContest() {
			var d = document.getElementById("deleteContest");
			if (d.style.display === 'none') {
		    	d.style.display = "block";
			} else {
				d.style.display = 'none';
			}
		};
		function showEditContest() {
			var d = document.getElementById("editContest");
			if (d.style.display === 'none') {
		    	d.style.display = "block";
			} else {
				d.style.display = 'none';
			}
		};
		function checkActivateButton() {
			var checkContestName = document.getElementById('checkContestName');
			var deleteButton = document.getElementById('deleteButton');
			if (checkContestName.value == contest.contestName) {
				deleteButton.disabled = false;
			} else {
				deleteButton.disabled = true;
			}
			return true;
		};
		function deleteContest() {
			var checkContestName = document.getElementById('checkContestName');
			return checkContestName.value == contest.contestName;
		};
	</script>
<% } %>
<style>
	.warning-border {
		border-color: #DE2C2C;
		border-width: thin;
		border-style: solid;
	}
</style>