package nnu.wyz.systemMS.model.entity.codeModel.ModelRecommend;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "适用场景描述")
public class Scenario {
    private String coreScenario;
    private List<String> keywords;
    private List<String> commonQuestions;
}
