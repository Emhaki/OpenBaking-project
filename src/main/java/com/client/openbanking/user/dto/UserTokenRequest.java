package com.client.openbanking.user.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 이명학
 * @date 2023. 8. 2.
 */

@Data
@Getter
@Setter
public class UserTokenRequest {
	private String code;
	private String client_id;
	private String client_secret;
	private String redirect_uri;
	private String grant_type;

}
