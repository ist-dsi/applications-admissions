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

<%
final JsonObject candidateJson = (JsonObject) request.getAttribute("candidate");
final Candidate candidate = FenixFramework.getDomainObject(candidateJson.get("id").getAsString());
%>

<div class="page-header">
    <h1>
        <span id="candidateNumber"></span>
        <spring:message code="label.applications.admissions.candidate"/>
        <span id="candidateName"></span>
    </h1>
    <h4>
        <spring:message code="label.applications.admissions.contest"/>
        <a href="<%= contextPath + "/admissions/contest/" + candidate.getContest().getExternalId() %>" class="secondaryLink">
            <span id="contestName" style="font-weight: bold;"></span>
        </a>
        <span id="beginDate" style="color: gray;"></span>
        -
        <span id="endDate" style="color: gray;"></span>
    </h4>
</div>

<h4>
    <spring:message code="label.logs.view.for.candidate.documents" text="Candidate Document Logs"/>
</h4>
<table class="table">
    <thead>
        <tr>
            <th><spring:message code="label.logs.when" text="When"/></th>
            <th><spring:message code="label.logs.operation" text="Operation"/></th>
            <th><spring:message code="label.logs.fileName" text="Document"/></th>
            <th><spring:message code="label.logs.fileSize" text="Size"/></th>
            <th><spring:message code="label.logs.nodeId" text="Document ID"/></th>            
        </tr>
    </thead>
    <tbody id="candidateLogs">
    </tbody>
</table>

<br/>

<h4>
    <spring:message code="label.logs.view.for.recommendation.letters" text="Recommendation Letter Logs"/>
</h4>
<table class="table">
    <thead>
        <tr>
            <th><spring:message code="label.logs.when" text="When"/></th>
            <th><spring:message code="label.logs.operation" text="Operation"/></th>
            <th><spring:message code="label.logs.fileName" text="Document"/></th>
            <th><spring:message code="label.logs.fileSize" text="Size"/></th>
            <th><spring:message code="label.logs.nodeId" text="Document ID"/></th>            
        </tr>
    </thead>
    <tbody id="letterLogs">
    </tbody>
</table>


<% if (Contest.canManageContests()) { %>
    <script type="text/javascript">
        var contextPath = '<%= contextPath %>';
        var candidate = ${candidate};
        var contest = candidate.contest;
        var logs = candidate.logs.logs;
        var recommendationLogs = candidate.recommendationLogs.logs;

        $(document).ready(function() {
            $('#candidateName').html(candidate.name);
            $('#candidateNumber').html(candidate.candidateNumber);
            $('#contestName').html(contest.contestName);
            $('#beginDate').html(contest.beginDate);
            $('#endDate').html(contest.endDate);

            $(logs).each(function(i, log) {
                var timestamp = moment(log.timestamp);
                row = $('<tr/>').appendTo($('#candidateLogs'));
                row.append($('<td/>').html(timestamp.format("YYYY-MM-DD HH:mm")));
                row.append($('<td/>').html(log.type));
                row.append($('<td/>').html(log.filename));
                row.append($('<td/>').html(log.size));
                row.append($('<td/>').html(log.fileId));
            });

            $(recommendationLogs).each(function(i, log) {
                var timestamp = moment(log.timestamp);
                row = $('<tr/>').appendTo($('#candidateLogs'));
                row.append($('<td/>').html(timestamp.format("YYYY-MM-DD HH:mm")));
                row.append($('<td/>').html(log.type));
                row.append($('<td/>').html(log.filename));
                row.append($('<td/>').html(log.size));
                row.append($('<td/>').html(log.fileId));
            });
        });
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
