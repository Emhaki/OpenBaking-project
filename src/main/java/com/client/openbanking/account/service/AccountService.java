package com.client.openbanking.account.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.client.openbanking.account.domain.UserAcc;
import com.client.openbanking.account.dto.Account;
import com.client.openbanking.account.dto.AccountHistory;
import com.client.openbanking.account.dto.AccountList;
import com.client.openbanking.account.repository.AccountRepository;
import com.client.openbanking.common.CommonUtils;
import com.client.openbanking.common.repository.LoginRepository;
import com.client.openbanking.user.domain.UserMaster;
import com.client.openbanking.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author : 김응경
 * @date   : 2023. 8. 9.
 * @description 등록계좌조회, 잔액조회, 거래내역조회, 계좌해지 API 
 */
@Service
@RequiredArgsConstructor
public class AccountService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final RestTemplate restTemplate;
	private final LoginRepository loginRepository;
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	
	@Value("${openbanking.clientId}")
	private String client_id;
	
	@Value("${openbanking.clientSecret}")
	private String client_secret;
	
	@Value("${openbanking.baseUrl}")
	private String base_url;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * UserAcc 테이블의 계좌 자동 채번
	 * @Method Name : insertAccSeq
	 * @author      : 김응경
	 * @date        : 2023. 8. 23.
	 * @param userAcc : 사용자계좌 엔티티
	 */
	@Transactional
	public String insertAccSeq(UserAcc userAcc) throws Exception {
		Query countQuery = entityManager.createQuery("SELECT COUNT(ua) FROM UserAcc ua");
		Long totalCount = (Long) countQuery.getSingleResult();
		
		String newAccSeq = String.valueOf(totalCount + 1);
		userAcc.setAccSeq(newAccSeq);
		
		return newAccSeq;
	}
	
	/**
	 * 등록계좌조회 API call
	 * @author 김응경
	 * @date 2023. 8. 14.
	 * @param userAccessToken : 사용자 액세스 토큰
	 * @param userLoginId : 현재 유저 로그인 Id
	 */	
	public Map<String, Object> requsetAccount(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String resultCode = "0000";
		String resultMessage = "정상";
		
		try {
			/* 요청전문 Header 세팅 */
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + paramMap.get("userAccessToken"));
			headers.set("scope", "login, sa");
			
			/* 요청전문 세팅 */
			HttpEntity<?> accountRequestEntity = new HttpEntity<Object>(headers);
			UserMaster user = loginRepository.findByUserLoginId((String)paramMap.get("userLoginId"));
			String urlWithParams = base_url + "v2.0/account/list" + "?user_seq_no=" + user.getUserSeqNo() + "&include_cancel_yn=N"+ "&sort_order=D";
			logger.info("[AccouontList API Request]" + accountRequestEntity + urlWithParams);
			
			
			/* API call */
			ResponseEntity<String> response = restTemplate.exchange(urlWithParams, HttpMethod.GET, accountRequestEntity, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				/* 응답전문 파싱 */
				ObjectMapper objectMapper = new ObjectMapper();
				Account account = objectMapper.readValue(response.getBody(), Account.class);
				
				logger.info("[AccouontList API Response] : " + account);
				
				if("A0000".equals(account.getRsp_code())) {
					resultMap.put("accountResult", account.getRes_list());
					
				} else {
					resultCode = "9999";
					resultMessage = (!"".equals(account.getRsp_message())) ? "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + account.getRsp_code() + "]" : "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
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
	 * 핀테크이용번호, 계좌번호 저장
	 * @author 김응경
	 * @date 2023. 8. 22.
	 * @param accountResponse : 등록계좌 API 응답 메세지
	 */
	public void insertFinUseNumAndAccNum(Map<String, Object> accountResponse, HttpSession session) throws Exception {
		String now = CommonUtils.getCurrentDate("yyyyMMddHHmmss");
		String userLoginId = (String) session.getAttribute("userLoginId");
		
		List<AccountList> accountList = (List<AccountList>) accountResponse.get("accountResult");
		String accountNumMaksed = null;
		String finUseNum = null;
		
		for (AccountList accountDetail : accountList) {
			accountNumMaksed = accountDetail.getAccount_num_masked();
			finUseNum = accountDetail.getFintech_use_num();
			
			if (accountNumMaksed != null && finUseNum != null) {
				
				break;
			}
		}
		
		UserAcc userAcc = new UserAcc();
		String accSeq = insertAccSeq(userAcc);
		userAcc.setAccSeq(accSeq);
		userAcc.setFinUseNum(finUseNum);
		userAcc.setAccNum(accountNumMaksed);
		userAcc.setUserLoginId(userLoginId);
		userAcc.setCreAt(now);
		userAcc.setCreBy(userLoginId);
		userAcc.setModAt(" ");
		userAcc.setModBy(" ");
		
		accountRepository.save(userAcc);
	}
	
	/**
	 * 거래내역조회 API 요청
	 * @author 김응경
	 * @date 2023. 8. 28.
	 * @param 계좌정보 및 거래내역 조회조건
	 */
	public Map<String, Object> requestAccountHistory(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String resultCode = "0000";
		String resultMessage = "정상";
		try {
			/* 요청전문 Header 세팅 */
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.set("Authorization", "Bearer " + paramMap.get("userAccessToken"));
			requestHeaders.set("scope", "inquiry, sa");
			
			/* 요청전문 세팅 */
			String urlWithParams = base_url + "v2.0/account/transaction_list/fin_num"
								+ "?bank_tran_id=" + CommonUtils.getBankTranId() 
								+ "&fintech_use_num=" + paramMap.get("finUseNum")
								+ "&inquiry_type=" + paramMap.get("queryType")
								+ "&inquiry_base=D"
								+ "&from_date=" + paramMap.get("startDate").toString().replace("-", "")
								+ "&to_date=" + paramMap.get("endDate").toString().replace("-", "")
								+ "&sort_order=" + paramMap.get("sortOrder")
								+ "&tran_dtime="+ CommonUtils.getCurrentDate("yyyyMMddHHmmss");
			
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(requestHeaders);
			
			logger.info("[AccountHistory API Request] : " + requestEntity + urlWithParams);
			
			/* API call */
			ResponseEntity<String> response = restTemplate.exchange(urlWithParams, HttpMethod.GET, requestEntity, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				/* 응답전문 파싱 */
				ObjectMapper objectMapper = new ObjectMapper();
				AccountHistory accountHistory = objectMapper.readValue(response.getBody(), AccountHistory.class);
				
				logger.info("[AccountHistory API Response] : " + accountHistory);
				
				if("A0000".equals(accountHistory.getRsp_code())) {
					resultMap.put("AccountHistory", accountHistory.getRes_list());
					resultMap.put("rsp_code", accountHistory.getRsp_code());
				} else {
					resultCode = "9999";
					resultMessage = (!"".equals(accountHistory.getRsp_message())) ? "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + accountHistory.getRsp_code() + "]" : "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
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
	 * 잔액조회 API 요청 및 응답
	 * @author      : 이명학
	 * @date        : 2023. 8. 29.
	 * @param userLoginId : 현재 유저 로그인 Id
	 * @param finUseNum : 조회 할 핀테크이용번호(계좌번호)
	 */
	public Map<String, Object> requestAccountCheck(String userLoginId, String finUseNum, String userAccessToken) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String url = base_url + "v2.0/account/balance/fin_num";
		String resultCode = "0000";
		String resultMessage = "정상";
		ObjectMapper objectMapper = new ObjectMapper();
		HttpHeaders requestHeaders = new HttpHeaders();
		
		try {
			requestHeaders.set("Authorization", "Bearer " + userAccessToken);
			requestHeaders.set("scope", "inquiry, sa");
			
			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url)
					.queryParam("bank_tran_id", CommonUtils.getBankTranId())
					.queryParam("fintech_use_num", finUseNum)
					.queryParam("tran_dtime", CommonUtils.getCurrentDate("yyyyMMddHHmmss"));
			
			URI uri = uriBuilder.build().toUri();
			HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
			
			logger.info("[AccountBalance API Request] : " + requestEntity);
			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				AccountList result = objectMapper.readValue(response.getBody(), AccountList.class);
				
				logger.info("[AccountBalance API Response] : " + result);
				
				if ("A0000".equals(result.getRsp_code())) {
					resultMap.put("accountCheckResult", result);
				} else {
					/* 요청은 성공했으나 API로부터 정상 응답이 오지 않을 때 */
					resultCode = "9999";
					resultMessage = (!"".equals(result.getRsp_message())) ? "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + result.getRsp_code() + "]" : "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
				}
			} else {
				/* 응답이 200으로 오지 않았을 때*/
				resultCode = "9999";
				resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			/* 요청보내는 부분에서 에러가 생길 때 */
			resultCode = "9999";
			resultMessage = "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
		} finally {
			resultMap.put("resultCode", resultCode);
			resultMap.put("resultMessage", resultMessage);
		}
		
		return resultMap;
	}
	
	/**
	 * 계좌 목록 조회(계좌목록조회 API & 잔액조회 API call)
	 * @author 이명학
	 * @date 2023. 8. 30.
	 */
	public List<AccountList> getAccountList(String userLoginId, String userSeqNo, String userAccessToken) throws Exception {
		List<AccountList> accountList = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("userLoginId", userLoginId);
		paramMap.put("userAccessToken", userAccessToken);
		
		
		if (userAccessToken != null && userSeqNo != null) {
			/* 계좌목록조회 API call */
			Map<String, Object> account = requsetAccount(paramMap);
			accountList = (List<AccountList>) account.get("accountResult");
					
			List<AccountList> tempAccountList = accountList; // 임시리스트 생성
			
			if (account != null) {
				/* 잔액조회 API call */
				
				for (AccountList tempAccount : tempAccountList) {
					String finUseNum = tempAccount.getFintech_use_num(); // 등록계좌 FinUseNum 가져오기
					
					tempAccount.setAccount_holder_name(CommonUtils.getNameMasked(tempAccount.getAccount_holder_name()));
					
					Map<String, Object> accountCheck = requestAccountCheck(userLoginId, finUseNum, userAccessToken); // 잔액조회 API 호출
					
					if ("0000".equals(accountCheck.get("resultCode"))) {
						AccountList accountCheckResult = (AccountList) accountCheck.get("accountCheckResult");
						
						String balanceAmt = "0";
						balanceAmt = accountCheckResult.getBalance_amt();
						
						tempAccount.setBalance_amt(balanceAmt);
						tempAccount.setProduct_name(accountCheckResult.getProduct_name());
						
						tempAccount.setAccount_issue_date(accountCheckResult.getAccount_issue_date());
						tempAccount.setLast_tran_date(accountCheckResult.getLast_tran_date());
					} 
				}
			}
			
			accountList = tempAccountList; // 임시값 할당
		}
		
		return accountList;
	}
	
	/**
	 * 계좌 해지 API call
	 * @author 김응경
	 * @date 2023. 9. 4.
	 * @param userLoginId : 현재 유저 로그인 Id
	 * @param userAccessToken : 유저 액세스토큰
	 * @param accountNumMasked : 해지 할 마스킹 된 계좌번호
	 * @param finUseNum : 해지 할 핀테크이용번호
	 */
	public Map<String, Object> requestAccountCancel(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String resultCode = "0000";
		String resultMessage = "정상";
		
		try {
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setContentType(MediaType.valueOf("application/json; charset=UTF-8"));
			requestHeaders.set("Authorization", "Bearer " + paramMap.get("userAccessToken"));
			requestHeaders.set("scope", "login, sa");
			
			Map<String, Object> requestBody = new HashMap<String, Object>();
			requestBody.put("bank_tran_id", CommonUtils.getBankTranId());
			requestBody.put("scope", paramMap.get("scope"));  // transfer ,inquiry -> 두번 호출해야 삭제
			requestBody.put("fintech_use_num", paramMap.get("finUseNum"));
			
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(requestBody, requestHeaders);
			logger.info("[AccountCancel API Request] : " + requestEntity);
			
			/* API call */
			ResponseEntity<String> response = restTemplate.exchange(base_url + "v2.0/account/cancel", HttpMethod.POST, requestEntity, String.class);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				/* 응답전문 파싱 */
				ObjectMapper objectMapper = new ObjectMapper();
				AccountList result = objectMapper.readValue(response.getBody(), AccountList.class);
				
				logger.info("[AccountCancel API Response] : " + result);
				
				if("A0000".equals(result.getRsp_code())) {
					//TODO
					resultMap.put("AccountCancle", result);
				} else {
					resultCode = "9999";
					resultMessage = (!"".equals(result.getRsp_message())) ? "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다. [응답코드 : " + result.getRsp_code() + "]" : "처리 중, 오류가 발생하였습니다. 담당자에게 문의하시기 바랍니다.";
				}
			} else {
				logger.error("AccountCancel API HTTP Error Occurs. [Code : " + response.getStatusCode());
				
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