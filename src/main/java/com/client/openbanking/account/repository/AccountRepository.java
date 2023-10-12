package com.client.openbanking.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.client.openbanking.account.domain.UserAcc;

/**
 * @author 김응경
 * @date 2023. 8. 2.
 * @description UserAcc 테이블에 있는 데이터를 조회 , 저장
 */
@Repository
public interface AccountRepository extends JpaRepository<UserAcc, String> {
	UserAcc findByUserLoginIdAndAccNum(String userLoginId, String accNum);
	UserAcc findByUserLoginId(String userLoginId);
}
