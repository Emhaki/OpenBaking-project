package com.client.openbanking.user.dto;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 이명학
 * @date 2023. 8. 16.
 */

@Data
@Getter
@Setter
@Component
public class CompanyToken {
	
	private String access_token;
	private String client_use_code;
	private String expires_in;
	private String scope;
	private String token_type;
	private String get_token_date;
	
}
