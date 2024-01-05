package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscGeoAnalysisExecTaskDAO extends MongoRepository<DscGeoAnalysisExecTask, String> {

}
