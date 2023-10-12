package com.client.openbanking.common;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.client.openbanking.user.dto.CompanyToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author 이명학
 * @date 2023. 8. 16.
 * @description 공통함수
 */
@Component
@Getter
@Setter
@RequiredArgsConstructor
public class CommonUtils {
	public static String companyAccessToken; // 이용기관 토큰
	public static String companyAccessTokenExpiredDate; // 이용기관 토큰 만료일
	
	public static String company_code;
	private static String client_id;
	private static String client_secret;
	private static String base_url;
	
	public static final int REAL_SERVER = 0; // 운영환경
	public static final int DEV_SERVER = 1; // 테스트환경
	public static final int DEV_LOCAL = 2; // 로컬환경
	private static String sServer = null;
	private static int currentSystem = DEV_LOCAL;
	
	@Value("${openbanking.companycode}")
	private void setCompanyCode(String value) {
		company_code = value;
	}
	
	@Value("${openbanking.clientId}")
	private void setClientId(String value) {
		client_id = value;
	}
	
	@Value("${openbanking.clientSecret}")
	private void setClientSecret(String value) {
		client_secret = value;
	}
	
	@Value("${openbanking.baseUrl}")
	private void setBaseUrl(String value) {
		base_url = value;
	}
	
	/**
	 * 현재 날짜 호출
	 * @author 김응경
	 * @date 2023. 8. 11.
	 */
	public static String getCurrentDate(String dateFormat) throws Exception {
		LocalDateTime now = LocalDateTime.now();
		
		return now.format(DateTimeFormatter.ofPattern(dateFormat));
	}
	
	/**
	 * 토큰 만료 날짜 지정(3달)
	 * @author 이명학
	 * @date 2023. 8. 17.
	 */
	public static String getExpiresDate(String dateFormat, int number) throws Exception {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiresDate = now.plusMonths(number);
		return expiresDate.format(DateTimeFormatter.ofPattern(dateFormat));
	}
	
	/**
	 * 이용기관 토큰 조회
	 * @author 이명학
	 * @date 2023. 8. 16.
	 */
	public static String getCompanyToken() {
		try {
			if (companyAccessToken == null || companyAccessTokenExpiredDate == null) { //토큰 정보가 존재하지 않는 경우
				requestCompanyToken();
			} else if (Long.parseLong((CommonUtils.getCurrentDate("yyyyMMddHHmmss"))) >= Long.parseLong(companyAccessTokenExpiredDate)) { //토큰 유효기간이 만료된 경우
				requestCompanyToken();
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			companyAccessToken = null;
			companyAccessTokenExpiredDate = null;
		}
		
		return companyAccessToken;
	}
	
	/**
	 * 이용기관 토큰 발급 요청 및 저장
	 * @author 이명학
	 * @date 2023. 8. 16.
	 */
	public static void requestCompanyToken() throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.valueOf("application/x-www-form-urlencoded; charset=UTF-8"));
		
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("client_id", client_id);
		requestBody.add("client_secret", client_secret);
		requestBody.add("scope", "oob");
		requestBody.add("grant_type", "client_credentials");
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, requestHeaders);
		
		ResponseEntity<String> centerResponse = restTemplate.exchange(base_url + "oauth/2.0/token", HttpMethod.POST, requestEntity, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		CompanyToken companytoken = null;
		
		try {
			companytoken = objectMapper.readValue(centerResponse.getBody(), CompanyToken.class);
			
			companyAccessToken = companytoken.getAccess_token();
			companyAccessTokenExpiredDate = CommonUtils.getExpiresDate("yyyyMMddHHmmss", 3);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			
			companyAccessToken = null;
			companyAccessTokenExpiredDate = null;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			
			companyAccessToken = null;
			companyAccessTokenExpiredDate = null;
		}
	}
	
	/**
	 * 도메인 조회
	 * @author 김응경
	 * @date 2023. 8. 29.
	 */
	public static String getRequestDomain(HttpServletRequest request) throws Exception {
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
	}
	
	/**
	 * 서버 환경 확인
	 * @author 박지영
	 * @date 2023. 9. 7.
	 */
	public static int getSystemEnv() {
		try {
			sServer = System.getProperty("SERVERINFO");
			
			if ("DEV".equals(sServer)) {
				currentSystem = DEV_SERVER;
			} else if ("PROD".equals(sServer)) {
				currentSystem = REAL_SERVER;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return currentSystem;
	}
	
	/**
	 * 난수생성 함수
	 * @author 이명학
	 * @date 2023. 8. 29.
	 * @param 난수생성 길이
	 */
	public static String generateRandomCode(int length) {
		final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder code = new StringBuilder(length);
		
		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(characters.length());
			char randomChar = characters.charAt(randomIndex);
			code.append(randomChar);
		}
		return code.toString();
	}
	
	/**
	 * 거래고유번호(참가기관) 생성
	 * @author 박지영
	 * @date 2023. 9. 7.
	 */
	public static String getBankTranId() {
		return company_code + "U" + CommonUtils.generateRandomCode(9);
	}
	
	/**
	 * 이름 마스킹 처리
	 * @author 이명학
	 * @date 2023. 9. 8.
	 * @param 성명
	 */
	public static String getNameMasked(String name) {
		String validation = "(^[가-힣]+)$";
		final Matcher matcher = Pattern.compile(validation).matcher(name);
		
		try { 
			// True라면
			if(matcher.find()) {
				int length = name.length();
				String maskingPosition = "";
				
				if(length > 2) {
					maskingPosition = name.substring(1, length - 1);
				} else {
					maskingPosition = name.substring(1, length);
				}
				
				String dot = "";
				for(int i = 0; i < maskingPosition.length(); i++) {
					dot += "*";
				}
				
				if(length > 2) {
					name = name.substring(0, 1) + maskingPosition.replace(maskingPosition, dot) + name.substring(length-1, length);
				} else {
					name = name.substring(0, 1) + maskingPosition.replace(maskingPosition, dot);
				}
			}
		}
		catch (Exception e) {
			e.getStackTrace();
			
			name = "";
		}
		
		return name;
	}

}