package com.flyway.admin.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.dto.PromotionDto;
import com.flyway.admin.repository.AdminPromotionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPromotionServiceImpl implements AdminPromotionService {

	private final AdminPromotionRepository promotionRepository;

	@Override
	@Transactional(readOnly = true)
	public List<PromotionDto> getPromotionList(int page, int size, String isActive, String searchKeyword) {
		try {
			int safePage = Math.max(1, page);
			int safeSize = Math.max(1, Math.min(size, 100));
			int offset = (safePage - 1) * safeSize;
			List<PromotionDto> promotions = promotionRepository.findPromotionList(offset, safeSize, isActive,
				searchKeyword);
			return promotions.stream()
				.peek(p -> p.calculatePrices(p.getOriginalPrice()))
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Failed to get promotion list", e);
			return Collections.emptyList();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int getPromotionCount(String isActive, String searchKeyword) {
		try {
			return promotionRepository.countPromotions(isActive, searchKeyword);
		} catch (Exception e) {
			log.error("Failed to count promotions", e);
			return 0;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public PromotionDto getPromotionById(String promotionId) {
		try {
			PromotionDto promotion = promotionRepository.findById(promotionId);
			if (promotion != null) {
				promotion.calculatePrices(promotion.getOriginalPrice());
			}
			return promotion;
		} catch (Exception e) {
			log.error("Failed to get promotion: {}", promotionId, e);
			return null;
		}
	}

	@Override
	@Transactional
	public String createPromotion(PromotionDto promotion, String adminId) {
		try {
			String promotionId = UUID.randomUUID().toString();
			promotion.setPromotionId(promotionId);
			promotion.setCreatedBy(adminId);
			promotion.setIsActive("Y");
			promotion.setCabinClassCode("ECO"); // 고정

			if (promotion.getDisplayOrder() == 0) {
				promotion.setDisplayOrder(99);
			}

			int result = promotionRepository.save(promotion);
			if (result > 0) {
				log.info("Promotion created: {} by admin: {}", promotionId, adminId);
				return promotionId;
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to create promotion", e);
			return null;
		}
	}

	@Override
	@Transactional
	public boolean updatePromotion(PromotionDto promotion) {
		try {
			int result = promotionRepository.update(promotion);
			if (result > 0) {
				log.info("Promotion updated: {}", promotion.getPromotionId());
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Failed to update promotion: {}", promotion.getPromotionId(), e);
			return false;
		}
	}

	@Override
	@Transactional
	public boolean deletePromotion(String promotionId) {
		try {
			int result = promotionRepository.delete(promotionId);
			if (result > 0) {
				log.info("Promotion deleted: {}", promotionId);
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Failed to delete promotion: {}", promotionId, e);
			return false;
		}
	}

	@Override
	@Transactional
	public String togglePromotionStatus(String promotionId) {
		try {
			PromotionDto promotion = promotionRepository.findById(promotionId);
			if (promotion == null) {
				return null;
			}

			String newStatus = "Y".equals(promotion.getIsActive()) ? "N" : "Y";
			int result = promotionRepository.updateStatus(promotionId, newStatus);

			if (result > 0) {
				log.info("Promotion status changed: {} -> {}", promotionId, newStatus);
				return newStatus;
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to toggle promotion status: {}", promotionId, e);
			return null;
		}
	}

	@Override
	@Transactional
	public boolean updateDisplayOrder(String promotionId, int displayOrder) {
		try {
			int result = promotionRepository.updateDisplayOrder(promotionId, displayOrder);
			if (result > 0) {
				log.info("Promotion display order changed: {} -> {}", promotionId, displayOrder);
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Failed to update display order for promotion: {}", promotionId, e);
			return false;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<PromotionDto> getActivePromotions(int limit) {
		try {
			List<PromotionDto> promotions = promotionRepository.findActivePromotions(limit);
			return promotions.stream()
				.peek(p -> p.calculatePrices(p.getOriginalPrice()))
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Failed to get active promotions", e);
			return Collections.emptyList();
		}
	}
}
