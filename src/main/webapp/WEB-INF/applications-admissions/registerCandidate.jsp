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
<%@ page import="pt.ist.applications.admissions.domain.Contest"%>
<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>
<%@ page import="org.fenixedu.bennu.ApplicationsAdmissionsConfiguration" %>
<%@ page import="org.fenixedu.bennu.core.security.Authenticate"%>

<jsp:directive.include file="headers.jsp" />

<div class="page-header">
	<h1>
		<span id="contestName"></span>
		<spring:message code="label.applications.admissions.contest.candidate.register" text="Register New Candidate"/>
	</h1>
</div>

 <script type="text/javascript">
 var RecaptchaOptions = {
    theme : 'white'
 };
 </script>
 
<div class="page-body">
	<form id="registerForm" class="form-horizontal" action="#" method="POST">
		<div class="form-group">
			<label class="control-label col-sm-2" for="contestName">
				<spring:message code="label.applications.admissions.candidate" text="Candidate" />
			</label>
			<div class="col-sm-10">
				<input name="name" type="text" class="form-control" id="name" required="required" value=""/>
			</div>
		</div>
		<div class="form-group">
			<label class="control-label col-sm-2" for="contestName">
				<spring:message code="label.applications.admissions.candidate.email" text="CandidateEmail" />
			</label>
			<div class="col-sm-10">
				<input name="email" type="text" class="form-control" id="email" required="required" value="" onchange="validateEmail(this)"/>
			</div>
		</div>
		<% if (Authenticate.getUser() == null) {  %>
			<div class="form-group">
				<div class="col-sm-push-2 col-sm-10">
				<%
		          ReCaptcha c = ReCaptchaFactory.newSecureReCaptcha(ApplicationsAdmissionsConfiguration.getConfiguration().recaptchaSiteKey(), ApplicationsAdmissionsConfiguration.getConfiguration().recaptchaSecretKey(), true);
		          out.print(c.createRecaptchaHtml(null, null));
		        %>
		        </div>
			</div>
		<% } %>
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
	<% if (Contest.canManageContests()) { %>
		var action = contextPath + '/admissions/contest/' + contest.id;
	<% } else { %>
		var action =  contextPath + '/admissions/candidateRegistrationConfirmation/' + contest.id;
	<% } %>
	
	$('#contestName').html(contest.contestName);
	$('form').submit(function () {
		if (validateEmail('#email')) {
			$.ajax({
				url: contextPath + '/admissions/contest/' + contest.id + '/registerCandidate',
				type: 'POST',
				data: JSON.stringify($(this).serializeArray()),
				contentType: 'application/json',
				success: function (data) {
					window.open(action, "_self");
				},
				error: function (jqXHR, textStatus, errorThrown) {
					document.getElementById('recaptcha_reload_btn').click();
					alert('An error has occured!! :-(' + errorThrown)
				}
			})
		}
		return false
	})
	
	function validateEmail(input) {
	    var filter = /^([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;
	    if (filter.test($(input).val())) {
	    	$(input).closest("div").removeClass("has-error");
	        return true;
	    }
	    else {
	    	$(input).closest("div").addClass("has-error");
	        return false;
	    }
	}
</script>