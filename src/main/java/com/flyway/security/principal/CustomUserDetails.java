package com.flyway.security.principal;

import com.flyway.user.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = Objects.requireNonNull(user, "user must not be null");
    }

    public String getUserId() {
        return user.getUserId();
    }

    /**
     * Spring Security의 username 필드로 무엇을 사용할지 정책에 따라 상이
     * 본 프로젝트는 JWT subject 등을 userId로 통일하기 위해 username=userId로 매핑
     */
    @Override
    public String getUsername() {
        return user.getUserId();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isStatus("BLOCKED");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isStatus("ACTIVE");
    }

    private boolean isStatus(String expected) {
        Object status = user.getStatus();
        return expected.equalsIgnoreCase(status == null ? null : status.toString());
    }
}
