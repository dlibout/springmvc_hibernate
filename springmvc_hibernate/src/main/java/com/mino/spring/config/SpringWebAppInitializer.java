package com.mino.spring.config;

import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;

public class SpringWebAppInitializer implements WebApplicationInitializer {

	private static final Logger logger = LoggerFactory.getLogger(SpringWebAppInitializer.class);

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		// Create the root appcontext with the ApplicationContextConfig class
		AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
		appContext.register(ApplicationContextConfig.class);

		// since we registered ApplicationContextConfig instead of passing it to the constructor
		appContext.refresh(); 

		// Manage the lifecycle of the root appcontext
		servletContext.addListener(new ContextLoaderListener(appContext));
		servletContext.setInitParameter("defaultHtmlEscape", "true");

		// Now the config for the Dispatcher servlet
		//AnnotationConfigWebApplicationContext mvcContext = new AnnotationConfigWebApplicationContext();
		//mvcContext.register(WebMvcConfig.class);

		ServletRegistration.Dynamic dispatcher = servletContext.addServlet(
				"SpringDispatcher", new DispatcherServlet(appContext));
		dispatcher.setLoadOnStartup(1);
		Set<String> mappingConflicts = dispatcher.addMapping("/");

		if (!mappingConflicts.isEmpty()) {
			for (String s : mappingConflicts) {
				logger.error("Mapping conflict: " + s);
			}
			throw new IllegalStateException(
					"'SpringDispatcher' cannot be mapped to '/' under Tomcat.");
		}
		
		// Character encoding filter
		FilterRegistration.Dynamic fr = servletContext.addFilter("encodingFilter",  
				new CharacterEncodingFilter());
		fr.setInitParameter("encoding", "UTF-8");
		fr.setInitParameter("forceEncoding", "true");
		fr.addMappingForUrlPatterns(null, true, "/*");

	}
}
