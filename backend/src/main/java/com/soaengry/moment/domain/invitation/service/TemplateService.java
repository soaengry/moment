package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.response.TemplateResponse;
import com.soaengry.moment.domain.invitation.entity.Template;
import com.soaengry.moment.domain.invitation.entity.TemplateType;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.TemplateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateService {

    private final TemplateRepository templateRepository;

    @PostConstruct
    @Transactional
    public void seedTemplates() {
        for (TemplateType type : TemplateType.values()) {
            if (!templateRepository.existsByType(type)) {
                String displayName = switch (type) {
                    case WEDDING -> "웨딩";
                    case REUNION -> "동창회";
                    case GATHERING -> "가벼운 모임";
                };
                templateRepository.save(Template.create(type, displayName, null, null));
            }
        }
    }

    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(TemplateResponse::from)
                .collect(Collectors.toList());
    }

    public TemplateResponse getTemplateByType(TemplateType type) {
        Template template = templateRepository.findByType(type)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.TEMPLATE_NOT_FOUND));
        return TemplateResponse.from(template);
    }
}
