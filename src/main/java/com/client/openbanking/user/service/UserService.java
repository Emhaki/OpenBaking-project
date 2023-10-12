package com.client.openbanking.user.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.client.openbanking.common.CommonUtils;
import com.client.openbanking.common.repository.LoginRepository;
import com.client.openbanking.user.domain.TokenInfo;
import com.client.openbanking.user.domain.UserMaster;
import com.client.openbanking.user.dto.UserTokenResponse;
import com.client.openbanking.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * @author 이명학
 * @date 2023. 8. 2.
 * @description 사용자 토큰 관리 Service
 */
@Service
@RequiredArgsConstructor
public class UserService {
	
	public String companyAcessToken;
	private final RestTemplate restTemplate;
	private final UserRepository userRepository;
	private final LoginRepository loginRepository;
	
	@Value("${openbanking.clientId}")
	private String client_id;
	
	@Value("${openbanking.clientSecret}")
	private String client_secret;
	
	@Value("${openbanking.baseUrl}")
	private String base_url;
	
	/**
	 * 사용자 토큰 발급 요청
	 * @author 이명학
	 * @date 2023. 8. 3.
	 * @param code: 오픈뱅킹으로부터 받은 Authorization code
	 */
	public ResponseEntity<String> requestUserToken(String code, HttpServletRequest request) throws Exception {
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(MediaType.valueOf("application/x-www-form-urlencoded; charset=UTF-8"));
		
		String redirectUrl = CommonUtils.getRequestDomain(request) + "/openbanking";
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
		requestBody.add("code", code);
		requestBody.add("client_id", client_id);
		requestBody.add("client_secret", client_secret);
		requestBody.add("redirect_uri", redirectUrl);
		requestBody.add("grant_type", "authorization_code");
		
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(requestBody, requestHeaders);
		
		return restTemplate.exchange(base_url+"oauth/2.0/token", HttpMethod.POST, requestEntity, String.class);
	}
	
	/**
	 * 사용자 토큰 저장
	 * @author 김응경
	 * @date 2023. 8. 9.
	 * @param response : 사용자 토큰 발급 api의 응답 메세지
	 * @param session : 현재 세션에 있는 userId
	 */
	public UserTokenResponse insertUserToken(ResponseEntity<String> response, HttpSession session) throws Exception {
		String now = CommonUtils.getCurrentDate("yyyyMMddHHmmss");
		String userLoginId = (String) session.getAttribute("userLoginId");
		ObjectMapper objectMapper = new ObjectMapper();
		UserTokenResponse userTokenResponse = objectMapper.readValue(response.getBody(), UserTokenResponse.class);
		
		UserMaster user = loginRepository.findByUserLoginId(userLoginId);
		if(userLoginId != null) {
			user.setUserSeqNo(userTokenResponse.getUser_seq_no());
			loginRepository.save(user);
		}
		
		TokenInfo userToken = new TokenInfo();
		userToken.setAccessToken(userTokenResponse.getAccess_token());
		userToken.setRefreshToken(userTokenResponse.getRefresh_token());
		userToken.setUserLoginId(userLoginId);
		userToken.setCreAt(now);
		userToken.setCreBy(userLoginId);
		userToken.setModAt(" ");
		userToken.setModBy(" ");
		userToken.setTokenCreAt(now);
		userToken.setTokenExpAt(userTokenResponse.getExpires_in());
		
		userRepository.save(userToken);
		
		return userTokenResponse;
	}
	
	/**
	 * 사용자 AccessToken 조회
	 * @author 이명학
	 * @date 2023. 8. 22.
	 * @param userLoginId : 로그인한 유저의 userLoginId
	 */
	public String findUserAccessToken(String userLoginId) throws Exception {
		String userAccessToken = null;
		TokenInfo userToken = userRepository.findByUserLoginId(userLoginId);
		
		if (userToken != null) {
			userAccessToken = userToken.getAccessToken();
		}
		
		return userAccessToken;
	}
	
	/**
	 * 사용자 RefreshToken 조회
	 * @author 이명학
	 * @date 2023. 8. 24.
	 * @param userLoginId : 로그인한 유저의 userLoginId
	 */
	public String findUserRefreshToken(String userLoginId) throws Exception {
		String userRefreshToken = null;
		TokenInfo userToken = userRepository.findByUserLoginId(userLoginId);
		
		if (userToken != null) {
			userRefreshToken = userToken.getRefreshToken();
		}
		
		return userRefreshToken;
	}
	
	/**
	 * 사용자 UserSeqNo 조회
	 * @author 김응경
	 * @date 2023. 8. 24.
	 * @param userLoginId : 로그인한 유저의 userLoginId
	 */
	public String findUserSeqNo(String userLoginId) {
		String userSeqNo = null;
		UserMaster user = loginRepository.findByUserLoginId(userLoginId);
		if (user != null) {
			userSeqNo = user.getUserSeqNo();
		}
		
		return userSeqNo;
	}
}