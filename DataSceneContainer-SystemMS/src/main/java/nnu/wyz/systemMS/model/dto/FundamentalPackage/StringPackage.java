package nnu.wyz.systemMS.model.dto.FundamentalPackage;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StringPackage {
    private String content;

    public StringPackage(String content) {
        this.content = content;
    }
}
