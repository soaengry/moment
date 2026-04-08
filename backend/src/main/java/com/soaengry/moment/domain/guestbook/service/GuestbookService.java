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
    public GuestbookResponse createEntry(Long weddingId, GuestbookRequest request, Long currentUserId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        User user = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        String encodedPassword = request.password() != null ? passwordEncoder.encode(request.password()) : null;

        GuestbookEntry entry = GuestbookEntry.create(
                wedding, user, request.authorName(), request.content(),
                encodedPassword, request.isSecret()
        );

        return GuestbookResponse.from(guestbookEntryRepository.save(entry));
    }

    public Page<GuestbookResponse> getEntries(Long weddingId, Long currentUserId, boolean isAdmin, Pageable pageable) {
        if (isAdmin || isHostOfWedding(weddingId, currentUserId)) {
            return guestbookEntryRepository.findByWeddingIdOrderByCreatedAtDesc(weddingId, pageable)
                    .map(GuestbookResponse::from);
        }
        return guestbookEntryRepository.findVisibleEntriesForUser(weddingId, currentUserId, pageable)
                .map(GuestbookResponse::from);
    }

    @Transactional
    public GuestbookResponse updateEntry(Long weddingId, Long entryId, GuestbookRequest request,
                                         Long currentUserId, boolean isAdmin) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        validateUpdateAccess(entry, request.password(), currentUserId, isAdmin);
        entry.update(request.content(), request.isSecret());

        return GuestbookResponse.from(entry);
    }

    @Transactional
    public void deleteEntry(Long weddingId, Long entryId, String password, Long currentUserId, boolean isAdmin) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        validateDeleteAccess(entry, weddingId, password, currentUserId, isAdmin);
        guestbookEntryRepository.delete(entry);
    }

    public void verifyPassword(Long entryId, String password) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new GuestbookException(GuestbookErrorCode.GUESTBOOK_ENTRY_NOT_FOUND));

        if (entry.getPassword() == null || password == null
                || !passwordEncoder.matches(password, entry.getPassword())) {
            throw new GuestbookException(GuestbookErrorCode.INVALID_PASSWORD);
        }
    }

    // ─── Private helpers ───

    private void validateUpdateAccess(GuestbookEntry entry, String password, Long currentUserId, boolean isAdmin) {
        if (isAdmin) return;
        if (entry.getUser() != null && currentUserId != null && currentUserId.equals(entry.getUser().getId())) return;
        if (entry.getPassword() != null && password != null
                && passwordEncoder.matches(password, entry.getPassword())) return;
        if (entry.getPassword() != null) throw new GuestbookException(GuestbookErrorCode.INVALID_PASSWORD);
        throw new GuestbookException(GuestbookErrorCode.UNAUTHORIZED_ACCESS);
    }

    private void validateDeleteAccess(GuestbookEntry entry, Long weddingId, String password,
                                      Long currentUserId, boolean isAdmin) {
        if (isAdmin) return;
        if (isHostOfWedding(weddingId, currentUserId)) return;
        if (entry.getUser() != null && currentUserId != null && currentUserId.equals(entry.getUser().getId())) return;
        if (entry.getPassword() != null && password != null
                && passwordEncoder.matches(password, entry.getPassword())) return;
        if (entry.getPassword() != null) throw new GuestbookException(GuestbookErrorCode.INVALID_PASSWORD);
        throw new GuestbookException(GuestbookErrorCode.UNAUTHORIZED_ACCESS);
    }

    private boolean isHostOfWedding(Long weddingId, Long userId) {
        if (userId == null) return false;
        return weddingRepository.findById(weddingId)
                .map(w -> w.getEvent().getUserId().equals(userId))
                .orElse(false);
    }
}
