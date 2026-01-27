package com.flyway.admin.repository;

import java.util.List;

import com.flyway.admin.dto.PromotionDto;

public interface AdminPromotionRepository {

	List<PromotionDto> findPromotionList(int offset, int limit, String isActive, String searchKeyword);

	int countPromotions(String isActive, String searchKeyword);

	PromotionDto findById(String promotionId);

	int save(PromotionDto promotion);

	int update(PromotionDto promotion);

	int delete(String promotionId);

	int updateStatus(String promotionId, String isActive);

	int updateDisplayOrder(String promotionId, int displayOrder);

	List<PromotionDto> findActivePromotions(int limit);
}
