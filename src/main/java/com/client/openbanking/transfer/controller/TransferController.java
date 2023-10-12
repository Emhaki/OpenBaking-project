package com.client.openbanking.transfer.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.client.openbanking.common.CommonUtils;
import com.client.openbanking.transfer.dto.WithdrawResponse;
import com.client.openbanking.transfer.service.TransferService;

import lombok.RequiredArgsConstructor;

/**
 * @author 이명학
 * @date 2023. 7. 26.
 * @description 입금, 출금이체 Controller
 */
@RequiredArgsConstructor
@RestController
public class TransferController {
	private final TransferService transferService;
	
	@Value("${openbanking.clientId}")
	private String client_id;
	
	/**
	 * 입금이체 페이지
	 * @author 이명학
	 * @date 2023. 7. 26.
	 * @param : 은행명
	 * @param : 마스킹된 계좌번호
	 * @param : 핀테크이용번호
	 */
	@PostMapping("/deposit")
	public ModelAndView showDepositPage(@RequestParam(value = "bankName", required = true) String bankName,
										@RequestParam(value = "accountNumMasked", required = true) String accountNumMasked,
										@RequestParam(value = "finUseNum", required = true) String finUseNum,
										@RequestParam(value = "accountHolderName", required = true) String accountHolderName,
										HttpServletRequest request, Model model, HttpSession session) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("deposit");
		modelAndView.addObject("clientId", client_id);
		modelAndView.addObject("redirectUrl", CommonUtils.getRequestDomain(request) + "/openbanking");
		
		model.addAttribute("bankName", bankName);
		model.addAttribute("accountNumMasked", accountNumMasked);
		model.addAttribute("finUseNum", finUseNum);
		model.addAttribute("accountHolderName", accountHolderName);
		
		return modelAndView;
	}
	
	/**
	 * 입금이체 결과 페이지
	 * @Method Name : showDepositResultPage
	 * @author      : 이명학
	 * @date        : 2023. 8. 30.
	 */
	@PostMapping("/depositResult")
	public ModelAndView showDepositResultPage(@RequestParam Map<String, Object> returnMap, HttpServletRequest request, Model model) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("depositResult");
		String redirectUrl = CommonUtils.getRequestDomain(request) + "/openbanking";
		modelAndView.addObject("clientId", client_id);
		modelAndView.addObject("redirectUrl", redirectUrl);
		
		model.addAttribute("bank_tran_date", returnMap.get("bankTranDate"));
		model.addAttribute("fintech_use_num", returnMap.get("finUseNum"));
		model.addAttribute("bank_name", returnMap.get("bankName"));
		model.addAttribute("print_content", returnMap.get("printContent"));
		model.addAttribute("account_num_masked", returnMap.get("accountNumMasked"));
		model.addAttribute("account_holder_name", returnMap.get("accountHolderName"));
		model.addAttribute("tran_amt", returnMap.get("tranAmt"));
		model.addAttribute("wd_print_content", returnMap.get("wdPrintContent"));
		model.addAttribute("success", "success");
		
		return modelAndView;
	}
	
	/**
	 * 출금이체 페이지
	 * @author 김응경
	 * @date 2023. 7. 26.
	 * @param bankName : 은행명
	 * @param accountNumMasked : 계좌번호
	 */
	@PostMapping("/withdraw")
	public ModelAndView showWithdrawPage(@RequestParam Map<String, Object> paramMap, Model model, HttpSession session) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("withdraw");
		modelAndView.addObject("clientId", client_id);
		
		model.addAttribute("accountNumMasked", paramMap.get("accountNumMasked"));
		model.addAttribute("bankName", paramMap.get("bankName"));
		model.addAttribute("finUseNum", paramMap.get("finUseNum"));
		model.addAttribute("accountHolderName", paramMap.get("accountHolderName"));
		
		return modelAndView;
	}
	
	/**
	 * 출금이체 결과 페이지
	 * @author 김응경
	 * @date 2023. 8. 30.
	 * @param withdrawResponse : 출금이체 api 응답 메세지
	 */
	@PostMapping("/withdrawResult")
	@ResponseBody
	public ModelAndView showWithdrawResultPage(@RequestParam Map<String, Object> paramMap, @ModelAttribute WithdrawResponse withdrawResponse, HttpServletRequest request, Model model, HttpSession session) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("withdrawResult");
		modelAndView.addObject("clientId", client_id);
		modelAndView.addObject("redirectUrl", CommonUtils.getRequestDomain(request) + "/openbanking");
		
		model.addAttribute("accountHolderName", paramMap.get("accountHolderName"));
		model.addAttribute("finUseNum", paramMap.get("finUseNum"));
		model.addAttribute("bankName", paramMap.get("bankName"));
		model.addAttribute("accountNumMasked", paramMap.get("accountNumMasked"));
		model.addAttribute("tranAmt", paramMap.get("tranAmt"));
		model.addAttribute("bankTranDate", paramMap.get("bankTranDate"));
		model.addAttribute("withdrawAmount", paramMap.get("withdrawAmount"));
		model.addAttribute("printContent", paramMap.get("printContent"));
		model.addAttribute("dpsPrintContent", paramMap.get("dpsPrintContent"));
		
		return modelAndView;
	}
	
	/**
	 * 입금이체 API 호출
	 * @author 이명학
	 * @date 2023. 8. 24.
	 */
	@PostMapping("/requestDeposit")
	@ResponseBody
	public Map<String, Object> requestDeposit(@RequestParam Map<String, Object> paramMap, HttpSession session, Model model) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		try {
			/* 수취조회 API call */
			paramMap.put("userLoginId", (String) session.getAttribute("userLoginId"));
			Map<String, Object> receptionResult = transferService.requestReception(paramMap);
			
			if ("0000".equals(receptionResult.get("resultCode"))) {
				/* 입금이체 API call */
				paramMap.put("userLoginId", (String) session.getAttribute("userLoginId"));
				returnMap = transferService.requestDeposit(paramMap);
				
				if ("0000".equals(returnMap.get("resultCode"))) {
					returnMap.put("resultCode", returnMap.get("resultCode"));
				} else {
					returnMap.put("resultMessage", paramMap.get("resultMessage"));
					returnMap.put("resultCode", paramMap.get("resultCode"));
				}
			} else {
				returnMap.put("resultMessage", receptionResult.get("resultMessage"));
				returnMap.put("resultCode", receptionResult.get("resultCode"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			returnMap.put("resultMessage", paramMap.get("resultMessage"));
			returnMap.put("resultCode", paramMap.get("resultCode"));
		}
		
		return returnMap;
	}
	
	/**
	 * 출금이체 API 호출
	 * @author 김응경
	 * @date 2023. 8. 29.
	 * @param withdrawAmount : 출금액
	 * @param printContent : 내 통장 표시 (츨금이체)
	 * @param depositPrintContent : 입금통장표시
	 */
	@PostMapping("/requestWithdraw")
	public Map<String, Object> requestWithdraw(@RequestParam Map<String, Object> paramMap, HttpSession session, Model model) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		try {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			
			paramMap.put("userLoginId", (String) session.getAttribute("userLoginId"));
			paramMap.put("userAccessToken", (String) session.getAttribute("userAccessToken"));
			
			resultMap = transferService.requestWithdraw(paramMap);
			
			if ("0000".equals(resultMap.get("resultCode"))) {
				returnMap.put("Data", resultMap.get("withdrawResult"));
				returnMap.put("Code", "0000");
			} else {
				returnMap.put("Code", "9999");
				returnMap.put("Message", resultMap.get("resultMessage"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			model.addAttribute("fail", "fail");
		}
		
		model.addAttribute("accountHolderName", paramMap.get("accountHolderName"));
		
		return returnMap;
	}

}