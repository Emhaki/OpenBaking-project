package com.client.openbanking.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.client.openbanking.user.domain.TokenInfo;
/**
 * @author 이명학
 * @date 2023. 8. 2.
 * @description TokenInfo 테이블에 있는 데이터를 조회 , 저장, 변경, 삭제
 */
@Repository
public interface UserRepository extends JpaRepository<TokenInfo, String>{
	TokenInfo findByUserLoginId(String userLoginId);
}
