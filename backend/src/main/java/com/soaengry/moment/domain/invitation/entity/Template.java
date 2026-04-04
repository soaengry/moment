package com.soaengry.moment.domain.invitation.entity;

import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "templates")
public class Template extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TemplateType type;

    @Column(nullable = false)
    private String displayName;

    private String description;

    private String previewImageUrl;

    private Template(TemplateType type, String displayName, String description, String previewImageUrl) {
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.previewImageUrl = previewImageUrl;
    }

    public static Template create(TemplateType type, String displayName, String description, String previewImageUrl) {
        return new Template(type, displayName, description, previewImageUrl);
    }

    public void update(String displayName, String description, String previewImageUrl) {
        this.displayName = displayName;
        this.description = description;
        this.previewImageUrl = previewImageUrl;
    }
}
