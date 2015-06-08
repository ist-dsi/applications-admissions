<%--

    Copyright © 2014 Instituto Superior Técnico

    This file is part of MGP Viewer.

    MGP Viewer is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MGP Viewer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MGP Viewer.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<% final String contextPath = request.getContextPath(); %>
<script src='<%= contextPath + "/bennu-portal/js/angular.min.js" %>'></script>
<script src='<%= contextPath + "/bennu-scheduler-ui/js/libs/moment/moment.min.js" %>'></script>
<script src='<%= contextPath + "/webjars/highcharts/4.0.4/highcharts.js" %>'></script>
<script src='<%= contextPath + "/webjars/highcharts/4.0.4/highcharts-more.js" %>'></script>
<script src='<%= contextPath + "/webjars/jquery-ui/1.11.1/jquery-ui.js" %>'></script>
<script src='<%= contextPath + "/webjars/angular-ui-bootstrap/0.9.0/ui-bootstrap-tpls.min.js" %>'></script>
<script src='<%= contextPath + "/mgp-viewer/js/numbers.js" %>'></script>
<link rel="stylesheet" href="<%= contextPath%>/mgp-viewer/css/fontello.css"></link>