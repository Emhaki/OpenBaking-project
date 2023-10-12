package com.client.openbanking.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 이명학
 * @date 2023. 8. 2.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenResponse {
	private String access_token;
	private String token_type;
	private int expires_in;
	private String refresh_token;
	private String scope;
	private String user_seq_no;
	private String client_use_code;
}
