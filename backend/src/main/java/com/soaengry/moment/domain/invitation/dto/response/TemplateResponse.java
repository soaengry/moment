package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Template;
import com.soaengry.moment.domain.invitation.entity.TemplateType;

public record TemplateResponse(
        Long id,
        TemplateType type,
        String displayName,
        String description,
        String previewImageUrl
) {
    public static TemplateResponse from(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getType(),
                template.getDisplayName(),
                template.getDescription(),
                template.getPreviewImageUrl()
        );
    }
}
