package zw.miccs.shopos.common.api;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponse<T>(List<T> content, PageMeta page) {

	public static <T> PageResponse<T> from(Page<T> springPage) {
		return new PageResponse<>(
			springPage.getContent(),
			new PageMeta(springPage.getNumber(), springPage.getSize(), springPage.getTotalElements()));
	}

	public record PageMeta(int page, int size, long totalElements) {
	}
}
