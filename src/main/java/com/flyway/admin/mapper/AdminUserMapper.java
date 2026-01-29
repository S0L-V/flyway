package com.flyway.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.admin.dto.AdminUserDto;

@Mapper
public interface AdminUserMapper {

	List<AdminUserDto> selectUserList(
		@Param("status") String status,
		@Param("searchKeyword") String searchKeyword,
		@Param("offset") int offset,
		@Param("limit") int limit
	);

	int countUsers(
		@Param("status") String status,
		@Param("searchKeyword") String searchKeyword
	);

	AdminUserDto selectUserById(@Param("userId") String userId);

	int updateUserStatus(
		@Param("userId") String userId,
		@Param("status") String status
	);

	int countUsersByStatus(@Param("status") String status);

}
