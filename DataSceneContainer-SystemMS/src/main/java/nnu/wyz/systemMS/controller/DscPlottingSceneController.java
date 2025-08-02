package nnu.wyz.systemMS.controller;


import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreatePlottingSceneDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscPlottingScene;
import nnu.wyz.systemMS.model.entity.PageInfo;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import nnu.wyz.systemMS.service.DscPlottingSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author tjk
 * @date 2025/8/1
 * @Description
 */
@RestController
@RequestMapping(value = "/dsc-plotting-scene")
public class DscPlottingSceneController {

    @Autowired
    private DscPlottingSceneService dscPlottingSceneService;

    @PostMapping("/create")
    public CommonResult<DscPlottingScene> createScene(@ModelAttribute CreatePlottingSceneDTO dto) {
        return dscPlottingSceneService.createScene(dto);
    }

    @PutMapping("/update")
    public CommonResult<DscPlottingScene> updateScene(@RequestParam String sceneId,@ModelAttribute CreatePlottingSceneDTO dto) {
        return dscPlottingSceneService.updateScene(sceneId,dto);
    }

    @DeleteMapping("/delete")
    public CommonResult<String> deleteScene(@RequestParam String userId, @RequestParam String sceneId) {
        return dscPlottingSceneService.deleteScene(userId, sceneId);
    }

    @GetMapping("/list")
    public CommonResult<PageInfo<DscPlottingScene>> getSceneList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "1") Integer pageIndex,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        PageableDTO pageableDTO = new PageableDTO();
        pageableDTO.setKeyword(keyword);
        pageableDTO.setCriteria(userId);
        pageableDTO.setPageIndex(pageIndex);
        pageableDTO.setPageSize(pageSize);

        return dscPlottingSceneService.getSceneList(pageableDTO);
    }
}
