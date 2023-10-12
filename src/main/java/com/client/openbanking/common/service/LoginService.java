package com.client.openbanking.common.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.client.openbanking.common.repository.LoginRepository;
import com.client.openbanking.user.domain.UserMaster;

import lombok.RequiredArgsConstructor;

/**
 * @author 이명학 
 * @date 2023. 7. 28.
 * @description 로그인 기능, 아이디 및 비밀번호 형식 체크
 */
@Service
@RequiredArgsConstructor
public class LoginService {
	
	private final LoginRepository userRepository;
	
	/**
	 * 사용자 정보 존재 여부 확인
	 * @author : 이명학
	 * @date : 2023. 8. 9.
	 * @param : 사용자 ID
	 * @param : 사용자 비밀번호
	 */
	public Map<String, Object> checkUserLoginId(String userLoginId, String userPassword) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String isLoginValid = "Y";
		String returnMessage = "";
		UserMaster userMaster = null;
		
		try {
			userMaster = userRepository.findByUserLoginIdAndPassword(userLoginId, userPassword);
			
			if(userMaster == null) {
				isLoginValid = "N";
				returnMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			isLoginValid = "N";
			returnMessage = "로그인 처리 중 오류가 발생하였습니다. 관리자에게 문의해 주시기 바랍니다.";
		}
		
		returnMap.put("isLoginValid", isLoginValid);
		returnMap.put("returnMessage", returnMessage);
		returnMap.put("userMaster", userMaster);
		returnMap.put("userLoginId", userLoginId);
		
		return returnMap;
	}

}