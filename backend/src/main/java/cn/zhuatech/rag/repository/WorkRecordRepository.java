/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.rag.repository; import cn.zhuatech.rag.model.WorkRecord; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface WorkRecordRepository extends JpaRepository<WorkRecord,Long>{List<WorkRecord> findAllByOrderByDueDateAsc();List<WorkRecord> findByOperatingUnitCodeOrderByDueDateAsc(String code);long countByStatus(WorkRecord.Status status);}
