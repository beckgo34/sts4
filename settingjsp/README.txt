spring boot jsp 셋팅

pom.xml에 
<dependency>
	<groupId>org.apache.tomcat.embed</groupId>
	<artifactId>tomcat-embed-jasper</artifactId>
</dependency>
<dependency>
	<groupId>org.glassfish.web</groupId>
	<artifactId>jakarta.servlet.jsp.jstl</artifactId>
	<version>2.0.0</version>
</dependency>
추가

application.properties에 
# jsp setting
spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp
추가
scr - main - webapp - WEB-INF(추가) - views(추가)

jsp 파일에 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 추가
