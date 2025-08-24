package se.digg.application.api;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class PageResponse<T>
{
	private List<T> content;
	private int number;
	private int size;
	private long totalElements;
	private int totalPages;

	public PageResponse()
	{
	}

	public PageResponse(List<T> content, int number, int size, long totalElements, int totalPages)
	{
		this.content = content;
		this.number = number;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
	}

	public static <T> PageResponse<T> fromPage(Page<T> page)
	{
		return new PageResponse<>(
			List.copyOf(page.getContent()),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}