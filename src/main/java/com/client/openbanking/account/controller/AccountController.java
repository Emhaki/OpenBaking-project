package com.client.openbanking.account.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.client.openbanking.account.dto.AccountHistory;
import com.client.openbanking.account.dto.AccountList;
import com.client.openbanking.account.service.AccountService;
import com.client.openbanking.common.CommonUtils;

import lombok.RequiredArgsConstructor;

/**
 * @author 김응경
 * @date 2023. 8. 2.
 * @description 등록계좌 페이지, 거래내역조회 페이지 불러오기
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class AccountController {
	private final AccountService accountService;
	
	@Value("${openbanking.clientId}")
	private String client_id;
	
	/**
	 * 등록계좌 조회 페이지
	 * @author 김응경
	 * @date 2023. 8. 2.
	 */
	@GetMapping("account")
	public String showAccountPage(HttpServletRequest request, HttpSession session, Model model) throws Exception {
		model.addAttribute("clientId", client_id);
		model.addAttribute("redirectUrl", CommonUtils.getRequestDomain(request) + "/openbanking");
		
		return "account";
	}
	
	/**
	 * 등록계좌 조회 API 호출
	 * @author 김응경
	 * @date 2023. 8. 2.
	 */
	@PostMapping("requestAccountList")
	@ResponseBody
	public Map<String, Object> requestAccountList(Model model, HttpSession session) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		/* 등록계좌 조회 API call */
		String userLoginId = (String) session.getAttribute("userLoginId");
		String userSeqNo = (String) session.getAttribute("userSeqNo");
		String userAccessToken = (String) session.getAttribute("userAccessToken");
		/* 잔액 조회 API call */
		List<AccountList> accResponse = accountService.getAccountList(userLoginId, userSeqNo, userAccessToken);
		
		if (accResponse != null) {
			returnMap.put("Code", "0000");
			returnMap.put("Message", "정상");
			returnMap.put("Data", accResponse);
		} else {
			returnMap.put("Code", "9999");
			returnMap.put("Message", "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.");
		}
		
		return returnMap;
	}
	
	/**
	 * 거래내역조회 페이지
	 * @author 김응경
	 * @date 2023. 8. 2.
	 * @param 등록계좌목록에서 선택 된 계좌정보
	 */
	@PostMapping("accountHistory")
	public String showAccountHistoryPage(@RequestParam Map<String, Object> paramMap, HttpSession session, Model model) throws Exception {
		model.addAttribute("params", paramMap);
		
		return "accountHistory";
	}
	
	/**
	 * 거래내역조회 API 호출
	 * @author 김응경
	 * @date 2023. 8. 25.
	 * @param 계좌정보 및 거래내역 조회조건
	 */
	@PostMapping("requestAccountHistory")
	@ResponseBody
	public Map<String, Object> requestAccountHistory(@RequestParam Map<String, Object> paramMap, HttpSession session) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<AccountList> accountHistory = null;
		paramMap.put("userAccessToken", (String) session.getAttribute("userAccessToken"));
		
		returnMap = accountService.requestAccountHistory(paramMap);
		
		accountHistory = (List<AccountList>) returnMap.get("AccountHistory");
//		Map<String, Object> accountHistory = accountService.requestAccountHistory(accountHistoryList);
		
		if ("0000".equals(returnMap.get("resultCode"))) {
			returnMap.put("Code", "0000");
			returnMap.put("Message", "정상");
			returnMap.put("Data", accountHistory);
		} else {
			returnMap.put("Code", "9999");
			returnMap.put("Message", "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + returnMap.get("rsp_code") + "]");
		}
		
		return returnMap;
	}
	
	/**
	 * 계좌 해지 API 호출 버튼
	 * @author 김응경
	 * @date 2023. 9. 1.
	 */
	@PostMapping("requestAccountCancel")
	@ResponseBody
	public Map<String, Object> requestAccountCancel(@RequestParam Map<String, Object> paramMap, HttpSession session) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		paramMap.put("userLoginId", (String) session.getAttribute("userLoginId"));
		paramMap.put("userAccessToken", (String) session.getAttribute("userAccessToken"));
		
		paramMap.put("scope", "transfer");
		resultMap = accountService.requestAccountCancel(paramMap);
		
		if ("0000".equals(resultMap.get("resultCode"))) {
			paramMap.put("scope", "inquiry");
			returnMap = accountService.requestAccountCancel(paramMap);
			
			if ("0000".equals(resultMap.get("resultCode"))) {
				returnMap.put("Code", "0000");
				returnMap.put("Message", "정상");
			} else {
				returnMap.put("Code", "9999");
				returnMap.put("Message", resultMap.get("resultMessage"));
			}
		} else {
			returnMap.put("Code", "9999");
			returnMap.put("Message", resultMap.get("resultMessage"));
		}
		
		return returnMap;
	}
}