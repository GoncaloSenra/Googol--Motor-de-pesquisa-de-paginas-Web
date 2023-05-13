package com.googol.WebServer;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.DefaultTemplateResolver;

@WebServlet(
        name = "AnnotationExample",
        description = "Example Servlet Using Annotations",
        urlPatterns = {"/AnnotationExample"}
)
public class WebServer extends HttpServlet {



}
