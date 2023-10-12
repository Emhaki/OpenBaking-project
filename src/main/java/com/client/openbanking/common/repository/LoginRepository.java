package com.client.openbanking.common.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.client.openbanking.user.domain.UserMaster;

/**
 * @author 이명학
 * @date 2023. 7. 28.
 * @description UserMaster 테이블에 있는 데이터를 조회 , 저장, 변경, 삭제
 */
public interface LoginRepository extends JpaRepository<UserMaster, String> {
	
	UserMaster findByUserLoginIdAndPassword(String userLoginId, String password);
	UserMaster findByUserLoginId(String userLoginId);
}
