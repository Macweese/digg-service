/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomErrorController implements ErrorController
{
	private final ErrorAttributes errorAttributes;

	public CustomErrorController(ErrorAttributes errorAttributes)
	{
		this.errorAttributes = errorAttributes;
	}

	// HTML for browsers (served from '../resources/template/')
	@RequestMapping(value = "/error", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response)
	{
		var webRequest = new ServletWebRequest(request);
		var opts = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE);
		Map<String, Object> attrs = this.errorAttributes.getErrorAttributes(webRequest, opts);

		HttpStatus status = resolveStatus(request, attrs);
		response.setStatus(status.value());

		ModelAndView mav = new ModelAndView("error");
		mav.addAllObjects(attrs); // exposes: status, error, message, path, timestamp, etc.
		mav.addObject("status", status.value()); // ensure numeric status is present
		mav.addObject("reason", status.getReasonPhrase());
		return mav;
	}

	// JSON for API clients
	@RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Map<String, Object>> errorJson(HttpServletRequest request)
	{
		var webRequest = new ServletWebRequest(request);
		var opts = ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE);
		Map<String, Object> attrs = this.errorAttributes.getErrorAttributes(webRequest, opts);
		HttpStatus status = resolveStatus(request, attrs);
		return ResponseEntity.status(status).body(attrs);
	}

	// Fallback → JSON
	@RequestMapping("/error")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> errorDefault(HttpServletRequest request)
	{
		return errorJson(request);
	}

	private HttpStatus resolveStatus(HttpServletRequest request, Map<String, Object> attrs)
	{
		Object sc = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if (sc instanceof Integer)
		{
			try
			{
				return HttpStatus.valueOf(((Integer) sc));
			}
			catch (Exception ignored)
			{
			}
		}
		Object fromMap = attrs.get("status");
		if (fromMap instanceof Integer)
		{
			try
			{
				return HttpStatus.valueOf((Integer) fromMap);
			}
			catch (Exception ignored)
			{
			}
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public String getErrorPath()
	{
		return "/error";
	}
}