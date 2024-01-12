package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscGeoAnalysisDAO extends MongoRepository<DscGeoAnalysisTool, String> {
}
