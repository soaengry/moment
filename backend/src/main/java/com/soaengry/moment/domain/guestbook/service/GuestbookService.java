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
        Long currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        return guestbookEntryRepository.findByWeddingIdOrderByCreatedAtDesc(weddingId, pageable)
                .map(entry -> {
                    if (entry.getIsSecret() && !isAdmin
                            && (currentUserId == null || !currentUserId.equals(
                            entry.getUser() != null ? entry.getUser().getId() : null))) {
                        return GuestbookResponse.secretFrom(entry);
                    }
                    return GuestbookResponse.from(entry);
                });
    }

    @Transactional
    public GuestbookResponse updateEntry(Long entryId, GuestbookRequest request) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        validateAccess(entry, request.password());
        entry.update(request.content(), request.isSecret());

        return GuestbookResponse.from(entry);
    }

    @Transactional
    public void deleteEntry(Long entryId, String password) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        validateAccess(entry, password);
        guestbookEntryRepository.delete(entry);
    }

    private void validateAccess(GuestbookEntry entry, String password) {
        if (isCurrentUserAdmin()) return;

        Long currentUserId = getCurrentUserId();
        if (entry.getUser() != null && currentUserId != null && currentUserId.equals(entry.getUser().getId())) {
            return;
        }

        if (entry.getPassword() != null && password != null && passwordEncoder.matches(password, entry.getPassword())) {
            return;
        }

        throw new GuestbookException(GuestbookErrorCode.UNAUTHORIZED_ACCESS);
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                return userRepository.findById(userId).orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long userId) {
                return userId;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isCurrentUserAdmin() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                return auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }
        } catch (Exception ignored) {}
        return false;
    }
}
