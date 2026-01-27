package com.flyway.admin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.flyway.admin.dto.PromotionDto;
import com.flyway.admin.mapper.AdminPromotionMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminPromotionRepositoryImpl implements AdminPromotionRepository {

	private final AdminPromotionMapper adminPromotionMapper;

	@Override
	public List<PromotionDto> findPromotionList(int offset, int limit, String isActive, String searchKeyword) {
		return adminPromotionMapper.selectPromotionList(offset, limit, isActive, searchKeyword);
	}

	@Override
	public int countPromotions(String isActive, String searchKeyword) {
		return adminPromotionMapper.countPromotions(isActive, searchKeyword);
	}

	@Override
	public PromotionDto findById(String promotionId) {
		return adminPromotionMapper.selectPromotionById(promotionId);
	}

	@Override
	public int save(PromotionDto promotion) {
		return adminPromotionMapper.insertPromotion(promotion);
	}

	@Override
	public int update(PromotionDto promotion) {
		return adminPromotionMapper.updatePromotion(promotion);
	}

	@Override
	public int delete(String promotionId) {
		return adminPromotionMapper.deletePromotion(promotionId);
	}

	@Override
	public int updateStatus(String promotionId, String isActive) {
		return adminPromotionMapper.updatePromotionStatus(promotionId, isActive);
	}

	@Override
	public int updateDisplayOrder(String promotionId, int displayOrder) {
		return adminPromotionMapper.updateDisplayOrder(promotionId, displayOrder);
	}

	@Override
	public List<PromotionDto> findActivePromotions(int limit) {
		return adminPromotionMapper.selectActivePromotions(limit);
	}
}
