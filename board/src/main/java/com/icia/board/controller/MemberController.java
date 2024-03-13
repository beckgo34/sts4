package com.icia.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MemberController {

	
	@GetMapping("/")
	public String home() {
		log.info("home()");
		
		return "index";
	}

	@GetMapping("loginForm")
	public String loginForm() {
		log.info("loginForm()");
		
		return "loginForm";
	}
}
