package org.iitm.course;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class CourseController {

	@GetMapping("/")
	public String index(@AuthenticationPrincipal Jwt jwt) {
		return String.format("Hello, %s!", jwt.getClaimAsString("preferred_username"));
	}

	@GetMapping("/protected/premium")
	public String premium(@AuthenticationPrincipal Jwt jwt) {
		return String.format("Hello, %s!", jwt.getClaimAsString("preferred_username"));
	}
}
