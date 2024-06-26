package me.vrishab.auction.system;

import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.vrishab.auction.item.validation.ValidPageRequestParams;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@ValidPageRequestParams
public class PageRequestParams {

    @Positive(message = "Please provide positive value")
    private Integer pageNum;

    @Positive(message = "Please provide positive value")
    private Integer pageSize;

    public Pageable createPageRequest() {
        return PageRequest.of(pageNum - 1, pageSize);
    }

    public boolean isValid() {
        return getPageSize() != null && getPageNum() != null;
    }

}
