package com.mihealth.controller.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ExceptionController {
	
	@RequestMapping(value = {"/400", "/404", "/500"}, method = RequestMethod.GET)
    public String exception() {
		return "default/home.html";
    }	
}
