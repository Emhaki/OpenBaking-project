package com.client.openbanking.common.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.client.openbanking.common.service.LoginService;
import com.client.openbanking.user.domain.UserMaster;
import com.client.openbanking.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * @author 이명학
 * @date 2023. 7. 26.
 * @description 로그인 Controller
 */
@Controller
@RequiredArgsConstructor
public class LoginController {
	
	private final LoginService loginService;
	private final UserService userSerivce;
	
	private static final Pattern UserLoginIdPattern = Pattern.compile("^[a-zA-Z\\d]{5,15}$"); // 5 ~ 15자 이내의 영문 대소문자&숫자 조합(특수문자 불가)
	private static final Pattern UserPasswordPattern = Pattern.compile("^(?=.*[a-zA-Z\\\\d])(.{5,20})$"); // 5 ~ 20자 이내의 영문 대소문자&숫자&특수문자 조합
	
	/**
	 * INDEX 페이지
	 * @author 박지영
	 * @date 2023. 8. 31.
	 */
	@GetMapping("/")
	public ModelAndView showIndexPage() throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		
		modelAndView.setViewName("index");
		
		return modelAndView;
	}
	
	/**
	 * 로그인 페이지
	 * @author 김응경
	 * @date 2023. 7. 26.
	 */
	@GetMapping("/login")
	public ModelAndView showLoginPage(HttpServletRequest request, HttpSession session) throws Exception {
		ModelAndView modelAndView = new ModelAndView();
		
		modelAndView.setViewName("login");
		
		return modelAndView;
	}
	
	/**
	 * 로그아웃 처리
	 * @author 김응경
	 * @date 2023. 9. 6.
	 */
	@PostMapping("/logout")
	public String logout(HttpSession session) throws Exception {
		session.invalidate();
		
		return "redirect:/login";
	}
	
	/**
	* 로그인 요청 처리
	* @author 이명학
	* @date 2023. 8. 9.
	* @param userLoginId : 사용자 ID
	* @param userPassword : 사용자 비밀번호
	*/
	@PostMapping("/login")
	@ResponseBody
	public Map<String, Object> checkLogin(@RequestParam("userLoginId") String userLoginId, @RequestParam("userPassword") String userPassword, HttpServletResponse response, RedirectAttributes redirectAttributes, Model model, HttpSession session) throws Exception {
		Map<String, Object> checkLoginMap = new HashMap<String, Object>();
		String returnMessage = "";
		
		try {
			if(!UserLoginIdPattern.matcher(userLoginId).matches() || !UserPasswordPattern.matcher(userPassword).matches()) { // 아이디 및 비밀번호 입력값 유효성 체크
				returnMessage = "아이디 또는 비밀번호의 형식이 올바르지 않습니다.";
				checkLoginMap.put("returnMessage", returnMessage);
				checkLoginMap.put("resultCode", "9999");
			}
			else { // 사용자 정보 존재 여부 확인
				checkLoginMap = loginService.checkUserLoginId(userLoginId, userPassword);
				
				if (checkLoginMap == null) {
					returnMessage = "로그인 처리 중 오류가 발생하였습니다. 관리자에게 문의해 주시기 바랍니다.";
					checkLoginMap.put("returnMessage", returnMessage);
					checkLoginMap.put("resultCode", "9999");
				} else {
					if ("Y".equals(checkLoginMap.get("isLoginValid"))) {
						UserMaster userMaster = (UserMaster) checkLoginMap.get("userMaster");
						
						session.setAttribute("userLoginId", userMaster.getUserLoginId());
						session.setAttribute("userSeqNo", userMaster.getUserSeqNo());
						
						String userAccessToken = userSerivce.findUserAccessToken(userLoginId);
						if (userAccessToken != null) {
							session.setAttribute("userAccessToken", userAccessToken);
						}
						checkLoginMap.put("resultCode", "0000");
						
					} else {
						returnMessage = (String) checkLoginMap.get("returnMessage");
						checkLoginMap.put("returnMessage", returnMessage);
						checkLoginMap.put("resultCode", "9999");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			returnMessage = "로그인 처리 중 오류가 발생하였습니다. 관리자에게 문의해 주시기 바랍니다.";
			checkLoginMap.put("returnMessage", returnMessage);
			checkLoginMap.put("resultCode", "9999");
		}
		
		return checkLoginMap;
	}
}