package nnu.wyz.fileMS.service;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.fileMS.config.MongoTransactional;
import nnu.wyz.fileMS.dao.DscMvtServiceInfoDAO;
import nnu.wyz.fileMS.dao.DscUserMvtSDAO;
import nnu.wyz.fileMS.dao.MvtDao;
import nnu.wyz.fileMS.dao.ShpProcessDAO;
import nnu.wyz.fileMS.model.entity.DscMvtServiceInfo;
import nnu.wyz.fileMS.model.entity.DscUserMvtS;
import nnu.wyz.fileMS.utils.MvtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * @Description
 * @Author wyjq
 * @Date 2022/3/10
 */

@Slf4j
@Service
public class DscMvtService {

    @Autowired
    MvtDao mvtDao;

    @Autowired
    private DscUserMvtSDAO dscUserMvtSDAO;

    @Autowired
    private DscMvtServiceInfoDAO dscMvtServiceInfoDAO;

    @Autowired
    private ShpProcessDAO shpProcessDAO;

    public void getMvt(int zoom, int x, int y, String tableName, HttpServletResponse response) {
        try {
            String sql = getMvtSql(zoom, x, y, tableName);
            if (sql == null) {
                return;
            }
            log.info("DefaultPgSource: " + zoom + ", " + x + ", " + y + ":" + sql);

            byte[] mvtByte = mvtDao.getMvtFromDefaultPg(sql);
            returnMvtByte(mvtByte, zoom, x, y, response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public String getMvtSql(int zoom, int x, int y, String tableName) {
        if (!MvtUtils.tileIsValid(zoom, x, y)) {
            return null;
        }
        HashMap<String, Double> envelope = MvtUtils.tileToEnvelope(zoom, x, y);
        return MvtUtils.envelopeToSQL(envelope, tableName);
    }

    public void returnMvtByte(byte[] mvtByte, int zoom, int x, int y, HttpServletResponse response) throws IOException {
//        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-type", "application/vnd.mapbox-vector-tile");
        String mtvFileName = String.format("%d_%d_%d.mvt", zoom, x, y);
        response.setHeader("Content-Disposition", "attachment;filename=" + new String(mtvFileName.getBytes("UTF-8"), "iso-8859-1"));
        OutputStream os = response.getOutputStream();
        os.write(mvtByte);
    }

    public CommonResult<List<DscMvtServiceInfo>> getMvtServiceList(String userId) {
        List<DscUserMvtS> allByUserId = dscUserMvtSDAO.findAllByUserId(userId);
        Iterator<DscUserMvtS> iterator = allByUserId.iterator();
        ArrayList<DscMvtServiceInfo> dscMvtServiceInfos = new ArrayList<>();
        while (iterator.hasNext()) {
            DscUserMvtS next = iterator.next();
            String mvtId = next.getMvtId();
            Optional<DscMvtServiceInfo> byId = dscMvtServiceInfoDAO.findById(mvtId);
            DscMvtServiceInfo dscMvtServiceInfo = byId.get();
            dscMvtServiceInfos.add(dscMvtServiceInfo);
        }
        return CommonResult.success(dscMvtServiceInfos, "获取成功！");
    }

    @MongoTransactional
    public CommonResult<String> deleteMvtService(String userId, String mvtSId) {
        Optional<DscMvtServiceInfo> byId = dscMvtServiceInfoDAO.findById(mvtSId);
        if(!byId.isPresent()) {
            return CommonResult.failed("服务不存在！");
        }
        DscUserMvtS byUserIdAndMvtId = dscUserMvtSDAO.findByUserIdAndMvtId(userId, mvtSId);
        if(Objects.isNull(byUserIdAndMvtId)) {
            return CommonResult.failed("用户未拥有该服务！");
        }
        dscUserMvtSDAO.delete(byUserIdAndMvtId);
        DscMvtServiceInfo dscMvtServiceInfo = byId.get();
        dscMvtServiceInfo.setOwnerCount(dscMvtServiceInfo.getOwnerCount() - 1);
        dscMvtServiceInfoDAO.save(dscMvtServiceInfo);
        if(dscMvtServiceInfo.getOwnerCount() == 0) {
            //删除源服务
            Boolean isDelete = shpProcessDAO.deletePgTable(dscMvtServiceInfo.getPtName());
            if(!isDelete) {
                throw new RuntimeException("pg表删除失败");
            }
            dscMvtServiceInfoDAO.delete(dscMvtServiceInfo);
        }
        return CommonResult.success("删除成功！");
    }

    public CommonResult<List<DscMvtServiceInfo>> getMvtByFileId(String fileId) {
        List<DscMvtServiceInfo> mvtInfos = dscMvtServiceInfoDAO.findAllByFileId(fileId);
        return CommonResult.success(mvtInfos, "获取成功！");
    }
}
