/*
 *   Copyright (c) HAN, 2025
 *   Licensed under the EUPL-1.2-or-later, with extension of article 5
 *   (compatibility clause) to any licence for distributing derivative works
 *   that have been produced by the normal use of the Work as a library.
 *   See the LICENSE file for the full details of EUPL-1.2
 */
package se.digg.application.controller;

import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

@Controller
public class CustomErrorController implements ErrorController
{
	private final ErrorAttributes errorAttributes;

	public CustomErrorController(ErrorAttributes errorAttributes)
	{
		this.errorAttributes = errorAttributes;
	}

	@RequestMapping("/error")
	public ResponseEntity<Map<String, Object>> handleError(WebRequest webRequest)
	{
		Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(
			webRequest,
			ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)
		);

		HttpStatus status = HttpStatus.valueOf((Integer) errorAttributes.get("status"));

		return new ResponseEntity<>(errorAttributes, status);
	}

	public String getErrorPath()
	{
		return "/error";
	}
}