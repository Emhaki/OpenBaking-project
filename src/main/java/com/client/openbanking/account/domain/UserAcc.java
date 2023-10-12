package com.client.openbanking.account.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 김응경
 * @date 2023. 8. 2.
 * @description user_acc entity
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_acc")
public class UserAcc {
	@Id
	@Column(name = "acc_seq", length = 3)
	private String accSeq;
	@Column(name = "user_login_id", length = 15, nullable = false)
	private String userLoginId;
	@Column(name = "fin_use_num", length = 24, nullable = false)
	private String finUseNum;
	@Column(name = "acc_num", length = 20)
	private String accNum;
	@Column(name = "cre_at", length = 14, nullable = false)
	private String creAt;
	@Column(name = "cre_by", length = 10, nullable = false)
	private String creBy;
	@Column(name = "mod_at", length = 14)
	private String modAt;
	@Column(name = "mod_by", length = 10)
	private String modBy;
}
