package com.ecommerce.common.api;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {
    @Test
    void copiesPageMetadataAndContent() {
        PageImpl<String> page = new PageImpl<>(
                List.of("alpha", "beta"),
                PageRequest.of(1, 2),
                7);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.content()).containsExactly("alpha", "beta");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(7);
        assertThat(response.totalPages()).isEqualTo(4);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isFalse();
    }

    @Test
    void mapsContentWhilePreservingPageMetadata() {
        PageImpl<Integer> page = new PageImpl<>(
                List.of(2, 3),
                PageRequest.of(0, 2),
                2);

        PageResponse<String> response = PageResponse.from(page, value -> "item-" + value);

        assertThat(response.content()).containsExactly("item-2", "item-3");
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }
}
