<%@ page contentType="text/html; charset=UTF-8"
	%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
	%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
	page import="org.springframework.security.core.AuthenticationException,
org.springframework.security.web.WebAttributes,
org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices,
org.springframework.security.core.AuthenticationException" %>
<html>
<head>
	<title>Login</title>
</head>
<body onload="findFormFocus()">

<form name="f" method="post" action='<c:url value="/j_spring_security_check"/>'>
	<table cellpadding="0" cellspacing="0" border="0">
		<tbody>
		<tr>
			<td class="key">Username</td>
			<td class="input"><input id="username" name="j_username" type="text"
									 value="<%=(session.getAttribute(WebAttributes.LAST_USERNAME) != null ? session.getAttribute(WebAttributes.LAST_USERNAME) : "")%>"/></td>
		</tr>
		<tr>
			<td class="key">Password</td>
			<td class="input"><input name="j_password" type="password" /></td>
		</tr>
		<tr>
			<td class="key">&nbsp;</td>
			<td>
				<input type="checkbox" name="<%=AbstractRememberMeServices.DEFAULT_PARAMETER%>"
					   id="<%=AbstractRememberMeServices.DEFAULT_PARAMETER%>"/>
				<label for="<%=AbstractRememberMeServices.DEFAULT_PARAMETER%>">
					Remember me
				</label>
			</td>
		</tr>
		<%
			if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) != null) {
				AuthenticationException authenticationException =  (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		%>
		<tr>
			<td>&nbsp;</td>
			<td class="loginFailure">Login failed</td>
		</tr>
		<%
			}
		%>
		</tbody>
	</table>
	<div class="submit">
		<input type="submit" value="Login" name="login_button" class="blueButton"/>
	</div>
</form>
<script type="text/javascript">
	function findFormFocus() {
		document.getElementById('username').focus();
	}
</script>
</body>
</html>
