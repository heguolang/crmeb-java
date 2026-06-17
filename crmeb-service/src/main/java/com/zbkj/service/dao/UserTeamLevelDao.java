package com.zbkj.service.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.zbkj.common.model.user.UserTeamLevel;
import com.zbkj.common.response.UserTeamLevelRecordResponse;
import com.zbkj.common.response.UserTeamLevelUserResponse;
import org.apache.ibatis.annotations.Param;

/**
 * 用户团队等级记录表 Mapper 接口
 */
public interface UserTeamLevelDao extends BaseMapper<UserTeamLevel> {
    /**
     * 团队关联用户分页
     */
    Page<UserTeamLevelUserResponse> getTeamUserPage(@Param("keywords") String keywords,
                                                    @Param("teamLevelId") Integer teamLevelId);

    /**
     * 团队等级变更记录分页
     */
    Page<UserTeamLevelRecordResponse> getTeamRecordPage(@Param("keywords") String keywords,
                                                        @Param("teamLevelId") Integer teamLevelId,
                                                        @Param("status") Integer status);
}

