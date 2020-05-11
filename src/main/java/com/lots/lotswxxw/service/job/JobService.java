package com.lots.lotswxxw.service.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lots.lotswxxw.dao.ListenHisoryDao;
import com.lots.lotswxxw.domain.po.ListenHisoryEntity;
import com.lots.lotswxxw.domain.vo.music.JsonRootBean;
import com.lots.lotswxxw.domain.vo.music.WeekData;
import com.lots.lotswxxw.util.CloudMusicApiUrl;
import com.lots.lotswxxw.util.CreateWebRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: lots
 * @date: 2020/4/26 09:38
 * @description:
 */

@Service
@Component
public class JobService {
    @Resource
    private  ListenHisoryDao listenHisoryDao;

    /**
     * 每5小时执行一次
     */
       @Scheduled(cron="0 0 0/1 * * ? ")
       // @Scheduled(cron="0/5 * * * * ? ")
   public void listenHisoryJob(){
       Map<String,Object> data=new HashMap<String,Object> ();
       String id =
                  "283135753" ;
           //     "128074624" ;

       String type = "1" ;
       data.put("uid",id);
       data.put("type",type);
       String dataPage = CreateWebRequest.createWebPostRequest(CloudMusicApiUrl.domain, data, new HashMap<String, String> ());
       JSONObject jsonResult = JSONUtil.parseObj(dataPage);
       JsonRootBean jsonRootBean = (JsonRootBean)JSONUtil.toBean(jsonResult, JsonRootBean.class);
       if(jsonRootBean!=null&&jsonRootBean.getWeekData()!=null&&jsonRootBean.getWeekData().size()>0){
           String finalId = id;
           List<WeekData> weekData = jsonRootBean.getWeekData();
           ListenHisoryEntity findUser=new ListenHisoryEntity();
           findUser.setUserId(finalId);
           if(CollUtil.isNotEmpty(weekData)){
               weekData.forEach(week->{
                   //分数
                   int score = week.getScore();
                   //歌曲名称
                   String name = week.getSong().getName();
                   //歌手
                   String singer=week.getSong().getAr().get(0).getName();
                   //歌曲id
                   Long songId=week.getSong().getId();
                   ListenHisoryEntity entity = new ListenHisoryEntity();
                   entity.setUserId(finalId);
                   entity.setCreatTime(new Date());
                   entity.setSongName(name);
                   entity.setSongScore(score);
                   entity.setSinger(singer);
                   entity.setSongId(songId);
                   try{
                       listenHisoryDao.insertListenHisory(entity);
                   }catch (Exception e ){

                   }

               });
           }
       }
    }
}
