package com.flyway.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.admin.dto.PromotionDto;
import com.flyway.admin.service.AdminPromotionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class AdminPromotionControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private AdminPromotionService promotionService;

	@InjectMocks
	private AdminPromotionController adminPromotionController;

	private PromotionDto samplePromotionDto;
	private String samplePromotionId;

	@Before
	public void setUp() {
		objectMapper = new ObjectMapper();
		// 컨트롤러와 Mockito 초기화
		mockMvc = MockMvcBuilders.standaloneSetup(adminPromotionController).build();

		samplePromotionId = UUID.randomUUID().toString();
		samplePromotionDto = PromotionDto.builder()
			.promotionId(samplePromotionId)
			.title("테스트 특가")
			.flightId(UUID.randomUUID().toString())
			.passengerCount(2)
			.discountPercentage(15)
			.build();
	}

	@Test
	public void createPromotion_Success() throws Exception {
		// given
		given(promotionService.createPromotion(any(PromotionDto.class), anyString())).willReturn(samplePromotionId);

		// when & then
		mockMvc.perform(post("/admin/promotions/api")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(samplePromotionDto))
				.sessionAttr("adminId", "test-admin")) // Simulate logged-in admin
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").value(samplePromotionId));
	}

	@Test
	public void getPromotionById_Success() throws Exception {
		// given
		given(promotionService.getPromotionById(samplePromotionId)).willReturn(samplePromotionDto);

		// when & then
		mockMvc.perform(get("/admin/promotions/api/{id}", samplePromotionId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.promotionId").value(samplePromotionId));
	}

	@Test
	public void getPromotionList_Success() throws Exception {
		// given
		given(promotionService.getPromotionList(1, 10, null, null)).willReturn(Collections.singletonList(samplePromotionDto));
		given(promotionService.getPromotionCount(null, null)).willReturn(1);

		// when & then
		mockMvc.perform(get("/admin/promotions/api/list")
				.param("page", "1")
				.param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.list[0].promotionId").value(samplePromotionId));
	}

	@Test
	public void updatePromotion_Success() throws Exception {
		// given
		given(promotionService.updatePromotion(any(PromotionDto.class))).willReturn(true);
		samplePromotionDto.setTitle("수정된 테스트 특가");

		// when & then
		mockMvc.perform(put("/admin/promotions/api/{id}", samplePromotionId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(samplePromotionDto))
				.sessionAttr("adminId", "test-admin")) // Simulate logged-in admin
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("프로모션이 수정되었습니다."));
	}

	@Test
	public void deletePromotion_Success() throws Exception {
		// given
		given(promotionService.deletePromotion(samplePromotionId)).willReturn(true);

		// when & then
		mockMvc.perform(delete("/admin/promotions/api/{id}", samplePromotionId)
				.sessionAttr("adminId", "test-admin")) // Simulate logged-in admin
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("프로모션이 삭제되었습니다."));
	}

	@Test
	public void togglePromotionStatus_Success() throws Exception {
		// given
		given(promotionService.togglePromotionStatus(samplePromotionId)).willReturn("N"); // Y -> N 으로 변경

		// when & then
		mockMvc.perform(post("/admin/promotions/api/{id}/toggle", samplePromotionId)
				.sessionAttr("adminId", "test-admin")) // Simulate logged-in admin
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").value("N"))
			.andExpect(jsonPath("$.message").value("상태가 변경되었습니다."));
	}
}