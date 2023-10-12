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
 * @date 2023. 7. 28.
 * @description USER_MASTER 테이블 생성
 */
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="USER_MASTER")
public class UserMaster {
	
	@Id
	@Column(name = "user_login_id", length=15)
	private String userLoginId;
	@Column(name = "user_password", length=20)
	private String password;
	@Column(name = "user_seq_no", length=10)
	private String userSeqNo;
	@Column(name = "cre_at", length=14)
	private String creAt;
	@Column(name = "cre_by", length=10)
	private String cre_by;
	@Column(name = "mod_at", length=14)
	private String modAt;
	@Column(name = "mod_by", length=10)
	private String modBy;
}
