package org.chomookun.fintics.core.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.chomookun.fintics.core.report.vo.PerformanceVo;

import java.util.List;

@Mapper
public interface PerformanceMapper {

    List<PerformanceVo> selectPerformances(List<String> brokerIds);

}
