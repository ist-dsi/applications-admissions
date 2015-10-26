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
<%@page import="org.fenixedu.commons.i18n.I18N"%>
<%@page import="org.glassfish.jersey.media.multipart.file.StreamDataBodyPart"%>
<%@page import="pt.ist.applications.admissions.domain.Contest"%>
<%@page import="pt.ist.applications.admissions.domain.Candidate"%>
<%@page import="pt.ist.fenixframework.FenixFramework"%>
<%@page import="com.google.gson.JsonElement"%>
<%@page import="com.google.gson.JsonArray"%>
<%@page import="org.glassfish.jersey.media.multipart.file.FileDataBodyPart"%>
<%@page import="java.io.File"%>
<%@page import="org.glassfish.jersey.media.multipart.MultiPart"%>
<%@page import="org.glassfish.jersey.media.multipart.MultiPartFeature"%>
<%@page import="javax.ws.rs.client.Entity"%>
<%@page import="javax.ws.rs.core.Form"%>
<%@page import="javax.ws.rs.client.Client"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.InputStream"%>
<%@page import="org.glassfish.jersey.media.multipart.FormDataMultiPart"%>
<%@page import="javax.ws.rs.core.MediaType"%>
<%@page import="com.google.gson.JsonObject"%>
<%@page import="com.google.gson.JsonParser"%>
<%@page import="pt.ist.applications.admissions.util.Utils"%>
<%@page import="javax.ws.rs.client.ClientBuilder"%>
<jsp:directive.include file="headers.jsp" />

<% if (request.getParameter("hash") != null && !"en".equals(I18N.getLocale().getLanguage())) { %>
	<script type="text/javascript">
    	window.onload=function() {
    		var contextPath = '<%= contextPath %>';
			$.post(contextPath + '/api/bennu-core/profile/locale/en-GB').always(function() { location.reload(); });
    	}
	</script>
<% } else { %>

<%
final JsonObject candidateJson = (JsonObject) request.getAttribute("candidate");
final Candidate candidate = FenixFramework.getDomainObject(candidateJson.get("id").getAsString());
final String hash = request.getParameter("hash");
final String hashArg = hash == null ? "" : "?hash=" + hash;
final JsonArray items = candidateJson.get("items").getAsJsonArray();
%>

<div class="page-header">
	<h1>
		<span id="candidateNumber"></span>
		<spring:message code="label.applications.admissions.candidate"/>
		<span id="candidateName"></span>
	</h1>
	<h4>
		<spring:message code="label.applications.admissions.contest"/>
		<a href="<%= contextPath + "/admissions/contest/" + candidate.getContest().getExternalId() + hashArg %>" class="secondaryLink">
			<span id="contestName" style="font-weight: bold;"></span>
		</a>
		<span id="beginDate" style="color: gray;"></span>
		-
		<span id="endDate" style="color: gray;"></span>
	</h4>
<% if (Contest.canManageContests()) { %>
	<table class="table" style="margin-top: 10px; margin-bottom: 0px;">
		<tr>
			<td>
				<label class="control-label" for="editLink" style="margin-right: 10px;">
					<spring:message code="label.applications.admissions.contest.candidate.link" text="Candidate Link"/>
				</label>
				<span id="editLink"></span>
				<span id="noEditLink">
					<spring:message code="label.link.not.available" text="Not Available"/>
				</span>
			</td>
			<td>
				<div id="undispose">
					<form method="POST" action="<%= contextPath + "/admissions/candidate/" + candidate.getExternalId() + "/undispose" %>">
						<button class="btn btn-default">
							<spring:message code="label.undispose" text="Undispose"/>
						</button>
					</form>
				</div>
			</td>
			<td>
				<form method="POST" action="<%= contextPath + "/admissions/candidate/" + candidate.getExternalId() + "/generateLink" %>">
					<button class="btn btn-default">
						<spring:message code="label.link.generate.new" text="Generate New Link"/>
					</button>
				</form>
			</td>
			<td>
				<button class="btn btn-default" onclick="showDeleteCandidate();">
					<spring:message code="label.link.delete.candidate" text="Delete Candidate"/>
				</button>
			</td>
		</tr>
	</table>
	<div id="deleteCandidate" style="display: none; padding-left: 20px; padding-right: 20px;">
		<div class="warning-border">
			<h3 style="background-color: #DE2C2C; color: white; margin: 10px; padding: 10px;">
				<spring:message code="label.link.delete.candidate" text="Delete Candidate"/>
			</h3>
			<p style="margin-left: 50px; margin-right: 50px; font-size: medium;">
				<spring:message code="label.link.delete.candidate.warning" text="Beware this operation cannot be reversed"/>
				<br/>
				<spring:message code="label.link.delete.candidate.warning.input" text="To delete the candidate input the candidates name into the following box."/>
				<br/>
				<form method="POST" action="<%= contextPath + "/admissions/candidate/" + candidate.getExternalId() + "/delete" %>"
						style="margin-left: 50px;">
					<input id="checkCandidateName" type="text" name="candidateName" size="50" onchange="checkActivateButton();"/>
					<button id="deleteButton" class="btn btn-default warning-border" onclick="return deleteCandidate();" disabled="disabled" style="background-color: #DE2C2C; color: white;">
						<spring:message code="label.link.delete.candidate" text="Delete Candidate"/>
					</button>
				</form>
			</p>
		</div>
	</div>
<% } %>
</div>

<h4>
	<spring:message code="label.applications.admissions.candidate.documents" text="Candidate Documents"/>
</h4>
<table class="table">
	<thead>
		<tr>
			<th><spring:message code="label.file.name" text="File"/></th>
			<th><spring:message code="label.file.size" text="Size"/></th>
			<th><spring:message code="label.file.created" text="Created"/></th>
			<th><spring:message code="label.file.modified" text="Modified"/></th>
			<th></th>
			<th></th>
		</tr>
	</thead>
	<tbody id="dirContents">
		<% if (candidate.verifyHashForEdit(hash) && candidate.getSealDate() == null) { %>
			<tr>
				<td colspan="6">
					<form method="POST" enctype="multipart/form-data" class="form-horizontal" style="margin-left: 50px;"
							action="<%= contextPath + "/admissions/candidate/" + candidate.getExternalId() + "/upload" %>">
						<input type="hidden" name="hash" value="<%= hash %>"/>
						<div class="form-group">
							<table>
								<tr>
									<td>
										<label class="control-label" for="contestName">
											<spring:message code="label.file.add" text="Add File" />
										</label>
									</td>
									<td>
										<input class="form-control" type="text" name="name" required="required"/>
									</td>
									<td>
										<input class="form-control" type="file" name="file" required="required"/>
									</td>
									<td>
										<button class="btn btn-default">
											<spring:message code="label.file.upload" text="Upload"/>
										</button>
									</td>
								</tr>
							</table>
						</div>
					</form>
				</td>
			</tr>
            <tr>
               <td colspan="6">
                    <div style="margin-left: 50px;">
                        <div class="infobox_warning">
                            <spring:message code="label.link.seal.instructions" text="Seal Instructions"/>
                        </div>
                        <button id="firstSubmitButton" class="btn btn-default" onclick="toggleConfirmSubmit();">
                            <spring:message code="label.link.seal" text="Seal"/>
                        </button>
                        <spring:message var="confirmSubmitApplicationMessage" code="label.link.seal.confirm" text="Confirm Seal"/>                            </button>
                        <div id="confirmSubmit" style="display: none;">
                            <div class="warning-border">
                                <h3 style="background-color: #DE2C2C; color: white; margin: 10px; padding: 10px;">
                                    <spring:message code="label.link.seal.confirm" text="Confirm Seal"/>
                                </h3>
                                <p style="margin-left: 50px; margin-right: 50px; font-size: medium;">
                                    <form method="POST" action="<%= contextPath + "/admissions/candidate/" + candidate.getExternalId() + "/submitApplication" %>">
                                        <input type="hidden" name="hash" value="<%= hash %>"/>
                                        <spring:message var="confirmSubmitApplicationMessage" code="label.link.seal.confirm" text="Confirm Seal"/>
                                        <button id="firstSubmitButton" class="btn btn-default" onclick="toggleConfirmSubmit(); return false;"
                                                style="margin-left: 10px;">
                                            <spring:message code="label.cancel" text="Cancel"/>
                                        </button>
                                        <spring:message var="confirmSubmitApplicationMessage" code="label.link.seal.confirm" text="Confirm Seal"/>                            </button>
                                        <button class="btn btn-default">
                                            <spring:message code="label.yes" text="Seal"/>
                                        </button>
                                    </form>
                                </p>
                            </div>
                        </div>
                    </div>
                </td>
            </tr>
		<% } %>
		<% if (candidate.getSealDate() != null) { %>
                <tr>
                   <td colspan="6">
                        <div style="margin-left: 50px;">
                            <div id="sealBox">
                                <spring:message code="label.application.sealed" text="This application is sealed"/>
                                <ul>
                                    <li>
                                        <span id="sealDate"></span>
                                    </li>
                                    <li>
                                        <span id="seal"></span>
                                    </li>
                                </ul>
                                <p class="sealHasBeenBroken">
                                    <spring:message var="sealHasBeenBrokenMessage" code="label.application.seal.broken" text="Seal Broken"/>                                        
                                    <span id="sealHasBeenBroken"></span>
                                </p>
                            </div>                            
                        </div>
                    </td>
                </tr>
		<% } %>
	</tbody>
</table>

<div id="lettersPart" style="visibility: hidden;">
	<h4>
		<spring:message code="label.applications.admissions.candidate.lettersOfRecommendation" text="Letters of Recommendation"/>
	</h4>
	<table class="table">
		<thead>
			<tr>
				<th><spring:message code="label.file.name" text="File"/></th>
				<th><spring:message code="label.file.size" text="Size"/></th>
				<th><spring:message code="label.file.created" text="Created"/></th>
				<th><spring:message code="label.file.modified" text="Modified"/></th>
				<th></th>
			</tr>
		</thead>
		<tbody id="lettersOfRecommendation">
		</tbody>
	</table>
	<a href="<%= contextPath + "/admissions/candidate/" + candidate.getExternalId() + "/download" + hashArg %>"
			class="btn btn-default">
		<spring:message code="label.download.everything" text="Download Everything"/>
	</a>
	<% if (Contest.canManageContests()) { %>
        <a href="<%= contextPath + "/admissions/candidate/" + candidate.getExternalId() + "/logs" %>"
                class="btn btn-default">
            <spring:message code="label.logs.view" text="View Logs"/>
        </a>
    <% } %>
</div>

<script type="text/javascript">
	var contextPath = '<%= contextPath %>';
	var candidate = ${candidate};
	var contest = candidate.contest;
	var items = candidate.items;
	var letterItems = candidate.letterItems;
	var hashArg = '<%= request.getParameter("hash") == null ? "" : "?hash=" + request.getParameter("hash") %>';
	$(document).ready(function() {
		$('#candidateName').html(candidate.name);
		$('#candidateNumber').html(candidate.candidateNumber);
		$('#contestName').html(contest.contestName);
		$('#beginDate').html(contest.beginDate);
		$('#endDate').html(contest.endDate);
		$('#sealDate').html(candidate.sealDate);
		$('#seal').html(candidate.seal);
		if (typeof candidate.seal !== 'undefined' && candidate.seal.localeCompare(candidate.calculatedDigest) != 0) {
			document.getElementById('sealBox').className = 'infobox_warning';
			$('#sealHasBeenBroken').html('${sealHasBeenBrokenMessage}');
		} else if (typeof candidate.seal !== 'undefined' && candidate.seal.localeCompare(candidate.calculatedDigest) == 0) {
			document.getElementById('sealBox').className = 'infobox5';
		}
        $(items).each(function(i, item) {
        	var created = moment(item.created);
        	var modified = moment(item.modified);
            row = $('<tr/>').prependTo($('#dirContents'));
            row.append($('<td/>').html(item.name));
            row.append($('<td/>').html(item.size));
            row.append($('<td/>').html(created.format("YYYY-MM-DD HH:mm")));
            row.append($('<td/>').html(modified.format("YYYY-MM-DD HH:mm")));
            row.append($('<td/>').html(
            		'<a href="' + contextPath + '/admissions/candidate/' + candidate.id + '/download/' + item.id
            			+ hashArg +'" class="btn btn-default">Download</a>'
            		<% if (candidate.verifyHashForEdit(hash) && candidate.getSealDate() == null) { %>
            		  + '<a href="#" onclick="deleteItem(' + candidate.id + ', ' + item.id + ')" class="btn btn-default" style="margin-left: 15px;">Delete</a>'
            		<% } %>
            		));
        });
        $(letterItems).each(function(i, item) {
        	var created = moment(item.created);
        	var modified = moment(item.modified);
            row = $('<tr/>').prependTo($('#lettersOfRecommendation'));
            row.append($('<td/>').html(item.name));
            row.append($('<td/>').html(item.size));
            row.append($('<td/>').html(created.format("YYYY-MM-DD HH:mm")));
            row.append($('<td/>').html(modified.format("YYYY-MM-DD HH:mm")));
            row.append($('<td/>').html('<a href="' + contextPath + '/admissions/candidate/' + candidate.id + '/download/' + item.id
            		+ hashArg + '" class="btn btn-default">Download</a>'));
        });
        if (letterItems) {
        	document.getElementById("lettersPart").style.visibility = "visible";
        }
	});
	function deleteItem(candidateId, itemId) {
		$.ajax({
			url: contextPath + '/admissions/candidate/' + candidateId + '/delete/' + itemId + hashArg,
			type: 'POST',
			contentType: 'application/json',
			success: function (data) {
				location.reload(true);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				alert('An error has occured!! :-(' + errorThrown)
			}
		});
	};
    function toggleConfirmSubmit() {
        var d = document.getElementById("confirmSubmit");
        var b = document.getElementById("firstSubmitButton");
        if (d.style.display === 'none') {
            d.style.display = "block";
            b.style.display = 'none';
        } else {
            d.style.display = 'none';
            b.style.display = "block";
        }
    };
</script>
<% if (Contest.canManageContests()) { %>
	<script type="text/javascript">
		var contextPath = '<%= contextPath %>';
		$(document).ready(function() {
			var candidateLink = location.protocol + '//' + location.hostname + ':' + location.port + contextPath + '/admissions/candidate/' + candidate.id;
			$('#linkUndispose').attr("href", candidateLink + '/undispose');
			$('#linkGenerate').attr("href", candidateLink + '/generateLinks');
			var editLink = candidateLink + '?hash=' + candidate.editHash;
			if (candidate.editHash) {
				$('#editLink').html('<a href="' + editLink + '">' + editLink + '</a>');
				document.getElementById("noEditLink").style.visibility = "hidden";
			} else {
				document.getElementById("undispose").style.visibility = "hidden";
			}
		});
		function showDeleteCandidate() {
			var d = document.getElementById("deleteCandidate");
			if (d.style.display === 'none') {
		    	d.style.display = "block";
			} else {
				d.style.display = 'none';
			}
		};
		function checkActivateButton() {
			var checkCandidateName = document.getElementById('checkCandidateName');
			var deleteButton = document.getElementById('deleteButton');
			if (checkCandidateName.value == candidate.name) {
				deleteButton.disabled = false;
			} else {
				deleteButton.disabled = true;
			}
			return true;
		};
		function deleteCandidate() {
			var checkCandidateName = document.getElementById('checkCandidateName');
			return checkCandidateName.value == candidate.name;
		};
	</script>
<% } %>
<style>
	.warning-border {
		border-color: #DE2C2C;
		border-width: thin;
		border-style: solid;
	}
	div .infobox5 {
	   border-color: #33CC00;
	}
	.sealHasBeenBroken {
	   font-size: large;
	}
</style>
<% } %>