package com.icia.thymeleafprj01.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class HomeController {
	@GetMapping("/")
	public String home(Model model) {
		log.info("home()");
		
		model.addAttribute("id", "user");
		
		return "index";
	}
	
	@GetMapping("second")
	public String second(Model model) {
		log.info("second()");
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일"); 
		String date = "<h2>" + sdf.format(now) + "</h2>	";
		
		model.addAttribute("date", date);
		
		//Map을 사용한 전송
		Map<String, String> rmap = new HashMap<>();
		rmap.put("pname", "치약");
		rmap.put("brand", "LG화학");
		rmap.put("price", "3000");
		model.addAttribute("product", rmap);
	
		
		return "second";
	}
	
	
	
}// class end
