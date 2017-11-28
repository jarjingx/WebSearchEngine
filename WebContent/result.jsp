<%@ page import = "Index.SearchIndex, java.util.Map, java.util.ArrayList" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>ResultOfSearch</title>
	</head>
	
	<body>
		<form method = "POST" action = "result.jsp">
		<%! String Searchtext = null;%>
		<%
			request.setCharacterEncoding("UTF-8");
			Searchtext = request.getParameter("User");
		%>

		<p align = "left">
		<font size = "3">
		<input type = "text" name = "User" style = "width:400px;height:40px" value=<%= Searchtext %>>
		<input type = "submit" value = "Search" style = "width:80px;height:40px"><br><br>
		</font>
		</form>
		<%
			SearchIndex searcher = new SearchIndex();
			ArrayList<Map<String,String>> rs = new ArrayList<Map<String, String>>();
	
		if(Searchtext != null){
			rs = searcher.search(Searchtext);
			if(rs != null && rs.size() != 0 ){
				String strBody,strTitle,strUrl;
				for(int i = 0 ; i < rs.size() ; i ++){
					Map<String,String> map = (Map<String,String>)rs.get(i);
				
					strTitle = map.get("title").toString();
					strBody = map.get("body").toString();
					strUrl = map.get("url").toString();

					//out.println("<br>" + "<href=" + strUrl + "target=\"_blank\">"+strTitle+"</a>" + "<br>");				
					out.println("<font color = \"blue\" size = \"4\">");
					out.print("<a href=\"" + strUrl + "\">" + strTitle + "</a>");
					out.println("</font>" + "<br>" + "<br>");
					out.println("<font color = \"black\" size = \"3\">" + strBody + "<br>" + "<br>");
				}
				out.println("<br>");
			}
			else {}
		}
		%>

	</body>
</html>