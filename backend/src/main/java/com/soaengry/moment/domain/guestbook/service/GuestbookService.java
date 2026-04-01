package com.soaengry.moment.domain.guestbook.service;

import com.soaengry.moment.domain.guestbook.dto.request.GuestbookRequest;
import com.soaengry.moment.domain.guestbook.dto.response.GuestbookResponse;
import com.soaengry.moment.domain.guestbook.entity.GuestbookEntry;
import com.soaengry.moment.domain.guestbook.exception.GuestbookErrorCode;
import com.soaengry.moment.domain.guestbook.exception.GuestbookException;
import com.soaengry.moment.domain.guestbook.repository.GuestbookEntryRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.CoupleRepository;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestbookService {

    private final GuestbookEntryRepository guestbookEntryRepository;
    private final WeddingRepository weddingRepository;
    private final CoupleRepository coupleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public GuestbookResponse createEntry(Long weddingId, GuestbookRequest request) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        User user = getCurrentUser();
        String encodedPassword = request.password() != null ? passwordEncoder.encode(request.password()) : null;

        GuestbookEntry entry = GuestbookEntry.create(
                wedding, user, request.authorName(), request.content(),
                encodedPassword, request.isSecret()
        );

        return GuestbookResponse.from(guestbookEntryRepository.save(entry));
    }

    public Page<GuestbookResponse> getEntries(Long weddingId, Pageable pageable) {
        // ADMIN 또는 웨딩 호스트: 모든 비밀 글 열람 가능
        if (isCurrentUserAdmin() || isCurrentUserHostOfWedding(weddingId)) {
            return guestbookEntryRepository.findByWeddingIdOrderByCreatedAtDesc(weddingId, pageable)
                    .map(GuestbookResponse::from);
        }
        // 그 외: DB에서 본인 비밀 글만 포함하여 조회 — 타인의 비밀 글은 로드 자체를 차단
        Long currentUserId = getCurrentUserId();
        return guestbookEntryRepository.findVisibleEntriesForUser(weddingId, currentUserId, pageable)
                .map(GuestbookResponse::from);
    }

    @Transactional
    public GuestbookResponse updateEntry(Long weddingId, Long entryId, GuestbookRequest request) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        validateUpdateAccess(entry, weddingId, request.password());
        entry.update(request.content(), request.isSecret());

        return GuestbookResponse.from(entry);
    }

    @Transactional
    public void deleteEntry(Long weddingId, Long entryId, String password) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        validateDeleteAccess(entry, weddingId, password);
        guestbookEntryRepository.delete(entry);
    }

    // 비밀번호 검증 (비로그인 방명록 수정/삭제 전 확인용)
    public void verifyPassword(Long entryId, String password) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        if (entry.getPassword() == null || password == null
                || !passwordEncoder.matches(password, entry.getPassword())) {
            throw new GuestbookException(GuestbookErrorCode.INVALID_PASSWORD);
        }
    }

    // 수정: 로그인 본인 또는 비밀번호 일치 시 허용
    private void validateUpdateAccess(GuestbookEntry entry, Long weddingId, String password) {
        if (isCurrentUserAdmin()) return;

        Long currentUserId = getCurrentUserId();
        if (entry.getUser() != null && currentUserId != null
                && currentUserId.equals(entry.getUser().getId())) {
            return;
        }

        // 비로그인: 비밀번호 일치 시 허용
        if (entry.getPassword() != null && password != null
                && passwordEncoder.matches(password, entry.getPassword())) {
            return;
        }

        // 비밀번호가 있는 항목인데 비밀번호가 틀린 경우
        if (entry.getPassword() != null) {
            throw new GuestbookException(GuestbookErrorCode.INVALID_PASSWORD);
        }

        throw new GuestbookException(GuestbookErrorCode.UNAUTHORIZED_ACCESS);
    }

    // 삭제: 호스트/ADMIN은 비밀번호 없이, 로그인 본인도 비밀번호 없이, 비로그인은 비밀번호 필요
    private void validateDeleteAccess(GuestbookEntry entry, Long weddingId, String password) {
        if (isCurrentUserAdmin()) return;

        Long currentUserId = getCurrentUserId();

        // 호스트(해당 웨딩의 커플)는 비밀번호 없이 삭제 가능
        if (currentUserId != null && isCurrentUserHostOfWedding(weddingId)) {
            return;
        }

        // 로그인 본인: 비밀번호 없이 삭제 허용
        if (entry.getUser() != null && currentUserId != null
                && currentUserId.equals(entry.getUser().getId())) {
            return;
        }

        // 그 외: 비밀번호가 맞으면 삭제 가능
        if (entry.getPassword() != null && password != null
                && passwordEncoder.matches(password, entry.getPassword())) {
            return;
        }

        // 비밀번호가 있는 항목인데 비밀번호가 틀린 경우
        if (entry.getPassword() != null) {
            throw new GuestbookException(GuestbookErrorCode.INVALID_PASSWORD);
        }

        throw new GuestbookException(GuestbookErrorCode.UNAUTHORIZED_ACCESS);
    }

    private boolean isCurrentUserHostOfWedding(Long weddingId) {
        User user = getCurrentUser();
        if (user == null) return false;
        String email = user.getEmail();
        if (email == null) return false;
        return coupleRepository.existsByWeddingIdAndEmail(weddingId, email);
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                return userRepository.findById(userId).orElse(null);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                return userId;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isCurrentUserAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
