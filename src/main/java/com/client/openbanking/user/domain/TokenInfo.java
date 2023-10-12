package com.client.openbanking.user.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 이명학
 * @date 2023. 8. 2.
 * @description TOKEN_INFO 테이블 생성
 */

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TOKEN_INFO")
public class TokenInfo {
	@Id
	@Column(name = "user_login_id", length=15)
	private String userLoginId;
	@Column(name = "access_token", length=400)
	private String accessToken;
	@Column(name = "refresh_token", length=400)
	private String refreshToken;
	@Column(name = "access_token_cre_at", length=14, nullable=false)
	private String TokenCreAt;
	@Column(name = "access_token_exp_by", length=14, nullable=false)
	private int TokenExpAt;
	
	@Column(name = "cre_at", length=14, nullable=false)
	private String CreAt;
	@Column(name = "cre_by", length=10, nullable=false)
	private String CreBy;
	@Column(name = "mod_at", length=14)
	private String modAt;
	@Column(name = "mod_by", length=10)
	private String modBy;
}
