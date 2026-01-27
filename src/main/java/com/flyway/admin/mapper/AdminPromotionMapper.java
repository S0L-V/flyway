package com.flyway.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import com.flyway.admin.dto.PromotionDto;

@Mapper
public interface AdminPromotionMapper {

	/**
	 * 특가 항공권 목록 조회
	 * @param offset 페이징 offset
	 * @param limit 페이징 limit
	 * @param isActive
	 * @param searchKeyword 검색어
	 */
	List<PromotionDto> selectPromotionList(
		@Param("offset") int offset,
		@Param("limit") int limit,
		@Param("isActive") String isActive,
		@Param("searchKeyword") String searchKeyword
	);

	/**
	 * 특가 항공권 총 개수 조회
	 */
	int countPromotions(
		@Param("isActive") String isActive,
		@Param("searchKeyword") String searchKeyword
	);

	/**
	 * ID로 특정 특가 항공권 조회
	 */
	PromotionDto selectPromotionById(@Param("promotionId") String promotionId);

	/**
	 * 특가 항공권 생성
	 */
	int insertPromotion(PromotionDto promotion);

	/**
	 * 특가 항공권 수정
	 */
	int updatePromotion(PromotionDto promotion);

	/**
	 * 특가 항공권 삭제
	 * @param promotionId
	 */
	int deletePromotion(@Param("promotionId") String promotionId);

	/**
	 * 특가 항공권 활성화 상태 변경
	 */
	int updatePromotionStatus(
		@Param("promotionId") String promotionId,
		@Param("isActive") String isActive
	);

	/**
	 * 특가 항공권 표시 순서 변경
	 */
	int updateDisplayOrder(
		@Param("promotionId") String promotionId,
		@Param("displayOrder") int displayOrder
	);

	/**
	 * 활성화된 특가 항공권 목록 조회 (메인 페이지용)
	 */
	List<PromotionDto> selectActivePromotions(@Param("limit") int limit);
}
