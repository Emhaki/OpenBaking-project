package com.client.openbanking.transfer.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.client.openbanking.common.CommonUtils;
import com.client.openbanking.transfer.dto.DepositResponse;
import com.client.openbanking.transfer.dto.ReceptionResponse;
import com.client.openbanking.transfer.dto.WithdrawResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author 이명학
 * @date 2023. 8. 21.
 * @description 입금, 출금이체 Service
 */
@Service
@RequiredArgsConstructor
@PropertySource(value = "classpath:application.properties", encoding="UTF-8")
public class TransferService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final RestTemplate restTemplate;
	
	@Value("${openbanking.baseUrl}")
	private String base_url;
	
	@Value("${openbanking.company.accountNum}")
	private String company_account_num;
	
	@Value("${openbanking.company.recvAccountName}")
	private String recv_account_name;
	
	@Value("${openbanking.company.recvAccountBankCode}")
	private String recv_account_bank_code;
	
	@Value("${openbanking.company.recvAccountNum}")
	private String recv_account_num;
	
	/**
	 * 입금이체 API call
	 * @author 이명학
	 * @date 2023. 8. 25.
	 * @param userLoginId : 사용자 ID
	 */
	public Map<String, Object> requestDeposit(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String resultCode = "0000";
		String resultMessage = "정상";
		
		try {
			/* 요청전문 Header 세팅 */
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.valueOf("application/json; charset=UTF-8"));
			requestHeaders.set("Authorization", "Bearer " + CommonUtils.getCompanyToken());
			requestHeaders.set("scope", "oob, sa");
			
			/* 요청전문 세팅 */
			Map<String, Object> requestBody = new HashMap<String, Object>();
			requestBody.put("cntr_account_type", "N"); // 계좌
			if (CommonUtils.getSystemEnv() == CommonUtils.REAL_SERVER) {
				requestBody.put("cntr_account_num", ""); //TODO 계약신청 완료 후 생성 예정
				requestBody.put("wd_pass_phrase", ""); //TODO 계약신청 완료 후 생성 예정
			} else {
				requestBody.put("cntr_account_num", company_account_num);
				requestBody.put("wd_pass_phrase", "NONE");
			}
			requestBody.put("wd_print_content", paramMap.get("wdPrintContent").toString().trim());
			requestBody.put("name_check_option", "on"); // 수취조회여부
			requestBody.put("tran_dtime", CommonUtils.getCurrentDate("yyyyMMddHHmmss"));
			requestBody.put("req_cnt", "1");
			
			List<Map<String, Object>> depositList = new ArrayList<Map<String, Object>>();
			Map<String, Object> deposit = new HashMap<String, Object>();
			deposit.put("tran_no", "1");
			deposit.put("bank_tran_id", CommonUtils.getBankTranId());
			deposit.put("fintech_use_num", paramMap.get("finUseNum"));
			deposit.put("print_content", paramMap.get("printContent"));
			deposit.put("tran_amt", paramMap.get("tranAmt"));
			deposit.put("req_client_name", paramMap.get("userLoginId").toString().toUpperCase());
			deposit.put("req_client_fintech_use_num", paramMap.get("finUseNum"));
			deposit.put("req_client_num", paramMap.get("userLoginId").toString().toUpperCase());
			deposit.put("transfer_purpose", "ST");
			deposit.put("withdraw_bank_tran_id", CommonUtils.generateRandomCode(9));
			depositList.add(deposit);
			
			requestBody.put("req_list", depositList);
			
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(requestBody, requestHeaders);
			
			logger.info("[Deposit API Request] : " + requestEntity);
			
			/* API call */
			ResponseEntity<String> response = restTemplate.exchange(base_url + "v2.0/transfer/deposit/fin_num", HttpMethod.POST, requestEntity, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				/* 응답전문 파싱 */
				ObjectMapper objectMapper = new ObjectMapper();
				DepositResponse result = objectMapper.readValue(response.getBody(), DepositResponse.class);
				
				logger.info("[Deposit API Response] : " + result);
				
				if("A0000".equals(result.getRsp_code())) {
					resultMap.put("depositResult", result);
				} else {
					resultCode = "9999";
					resultMessage = (!"".equals(result.getRsp_message())) ? "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + result.getRsp_code() + "]" : "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";;
				}
			}
			else {
				logger.error("Deposit API HTTP Error Occurs. [Code : " + response.getStatusCode());
				
				resultCode = "9999";
				resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			resultCode = "9999";
			resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
		} finally {
			resultMap.put("resultCode", resultCode);
			resultMap.put("resultMessage", resultMessage);
		}
		
		return resultMap;
	}
	/**
	 * 출금이체 API call
	 * @author 김응경
	 * @date 2023. 8. 29.
	 * @param userLoginId : 사용자 ID
	 * @param userAccessToken : 사용자  액세스토큰
	 * @param withdrawAmount : 출금액
	 * @param printContent : 내 통장 표시 (츨금이체)
	 * @param depositPrintContent : 입금통장표시
	 */
	public Map<String, Object> requestWithdraw(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String resultCode = "0000";
		String resultMessage = "정상";
		
		try {
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.valueOf("application/json; charset=UTF-8"));
			requestHeaders.set("Authorization", "Bearer " + (String) paramMap.get("userAccessToken"));
			requestHeaders.set("scope", "transfer, sa");
			
			Map<String, Object> requestBody = new HashMap<String, Object>();
			requestBody.put("bank_tran_id", CommonUtils.getBankTranId());
			requestBody.put("cntr_account_type", "N"); //약정계좌, 계정 구분
			requestBody.put("cntr_account_num", company_account_num); //약정계좌, 계정 번호
			requestBody.put("dps_print_content", paramMap.get("depositPrintContent")); // 입금계좌 인자 내역 
			requestBody.put("fintech_use_num", paramMap.get("finUseNum")); // 출금계좌 핀테크 이용번호
			requestBody.put("tran_amt", paramMap.get("withdrawAmount").toString().replace(",", "")); //거래 금액
			requestBody.put("tran_dtime", CommonUtils.getCurrentDate("yyyyMMddHHmmss")); // 요청일시
			requestBody.put("req_client_name", paramMap.get("userLoginId").toString().toUpperCase()); //요청 고객 성명
			requestBody.put("req_client_fintech_use_num",  paramMap.get("finUseNum")); //요청 고객 핀테크 이용번호
			requestBody.put("req_client_num", paramMap.get("userLoginId").toString().toUpperCase()); //요청 고객 회원번호
			requestBody.put("transfer_purpose", "ST"); // 이체용도
			requestBody.put("recv_client_name", recv_account_name); // 최종수취고객성명
			requestBody.put("recv_client_bank_code", recv_account_bank_code); // 최종 수취 고객 계좌 개설기관.표준코드
			requestBody.put("recv_client_account_num", recv_account_num); // 최종 수취 고객 계좌 번호
			
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(requestBody, requestHeaders);
			
			logger.info("[Withdraw API Request] : " + requestEntity);
			
			/* API call */
			ResponseEntity<String> response = restTemplate.exchange(base_url + "v2.0/transfer/withdraw/fin_num", HttpMethod.POST, requestEntity, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				/* 응답전문 파싱 */
				ObjectMapper objectMapper = new ObjectMapper();
				WithdrawResponse result = objectMapper.readValue(response.getBody(), WithdrawResponse.class);
				
				logger.info("[Withdraw API Response] : " + result);
				
				if("A0000".equals(result.getRsp_code())) {
					resultMap.put("withdrawResult", result);
				} else {
					resultCode = "9999";
					resultMessage = (!"".equals(result.getRsp_message())) ? "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + result.getRsp_code() + "]" : "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
				}
			}
			else {
				logger.error("Withdraw API HTTP Error Occurs. [Code : " + response.getStatusCode());
				
				resultCode = "9999";
				resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			resultCode = "9999";
			resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
		} finally {
			resultMap.put("resultCode", resultCode);
			resultMap.put("resultMessage", resultMessage);
		}
		
		return resultMap;
	}
	
	/**
	 * 수취조회 API 요청 및 응답
	 * @author 이명학
	 * @date 2023. 8. 30.
	 * @param userLoginId : 현재 유저 로그인 Id
	 * @param finUseNum : 조회 할 핀테크이용번호(계좌번호)
	 * @param tranAmt : 이체금액
	 * @param bankName : 은행명
	 * @param printContent : 내 통장표시
	 */
	public Map<String, Object> requestReception(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String resultCode = "0000";
		String resultMessage = "정상";
		
		HttpHeaders requestHeaders = new HttpHeaders();
		
		try {
			requestHeaders.setContentType(MediaType.valueOf("application/json; charset=UTF-8"));
			requestHeaders.set("Authorization", "Bearer " + CommonUtils.getCompanyToken());
			requestHeaders.set("scope", "oob, sa");
			
			Map<String, Object> requestBody = new HashMap<String, Object>();
			requestBody.put("bank_tran_id", CommonUtils.getBankTranId());
			requestBody.put("cntr_account_type", "N");
			requestBody.put("cntr_account_num", company_account_num);
			requestBody.put("fintech_use_num", paramMap.get("finUseNum"));
			requestBody.put("req_client_fintech_use_num", paramMap.get("finUseNum"));
			requestBody.put("print_content", paramMap.get("printContent"));
			requestBody.put("tran_amt", paramMap.get("tranAmt"));
			requestBody.put("req_client_name", paramMap.get("accountHolderName"));
			requestBody.put("req_client_num", paramMap.get("userLoginId").toString().toUpperCase());
			requestBody.put("transfer_purpose", "ST");
			
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(requestBody, requestHeaders);
			
			logger.info("[Reception API Request] : " + requestEntity);
			
			// API 요청
			ResponseEntity<String> response = restTemplate.exchange(base_url + "v2.0/inquiry/receive", HttpMethod.POST, requestEntity, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				ObjectMapper objectMapper = new ObjectMapper();
				ReceptionResponse result = objectMapper.readValue(response.getBody(), ReceptionResponse.class);
				
				logger.info("[Reception API Response] : " + result);
				
				if ("A0000".equals(result.getRsp_code())) {
					resultMap.put("receptionResult", result);
				} else {
					resultCode = "9999";
					resultMessage = (!"".equals(result.getRsp_message())) ? "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + result.getRsp_code() + "]" : "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
				}
			} else {
				logger.error("Deposit API HTTP Error Occurs. [Code : " + response.getStatusCode());
				
				resultCode = "9999";
				resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			resultCode = "9999";
			resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
		} finally {
			resultMap.put("resultCode", resultCode);
			resultMap.put("resultMessage", resultMessage);
		}
		
		return resultMap;
	}
}
