package com.client.openbanking.user.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.client.openbanking.account.service.AccountService;
import com.client.openbanking.user.dto.UserTokenResponse;
import com.client.openbanking.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * @author 이명학
 * @date 2023. 8. 2.
 * @description 사용자 토큰 관리 Controller
 */
@RequiredArgsConstructor
@Controller
public class UserController {
	private final AccountService accountService;
	private final UserService userService;
	
	@Value("${openbanking.clientId}")
	private String client_id;
	
	@Value("${openbanking.clientSecret}")
	private String client_secret;
	
	/**
	 * 사용자 토큰 API call, 토큰 저장, 등록계좌 API call
	 * @author 이명학, 김응경
	 * @date 2023. 8. 2.
	 * @param code : 사용자 인증 성공 시 반환되는 코드
	 * @param scope : Access Token 권한범위
	 * @param session : 세션
	 */
	@GetMapping("/openbanking")
	public String requestUserTokenButton(@RequestParam Map<String, Object> paramMap, HttpSession session, Model model, HttpServletRequest request) throws Exception {
		/* 사용자토큰 발급 API call */
		ResponseEntity<String> response = userService.requestUserToken((String)paramMap.get("code"), request);
		
		/* 사용자토큰 DB 및 Session 저장 */
		UserTokenResponse userTokenResponse = userService.insertUserToken(response, session);
		
		if (userTokenResponse.getAccess_token() != null) {
			session.setAttribute("userAccessToken", userTokenResponse.getAccess_token());
		}
		
		/* 등록계좌 조회 API call */
		paramMap.put("userLoginId", (String) session.getAttribute("userLoginId"));
		paramMap.put("userAccessToken", (String) session.getAttribute("userAccessToken"));
		
		Map<String, Object> accountResponse = accountService.requsetAccount(paramMap);
		accountService.insertFinUseNumAndAccNum(accountResponse, session);
		
		return "redirect:/account";
	}
	
}