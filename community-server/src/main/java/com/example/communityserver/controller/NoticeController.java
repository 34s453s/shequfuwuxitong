package com.example.communityserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.communityserver.common.result.Result;
import com.example.communityserver.entity.Notice;
import com.example.communityserver.entity.NoticeRead;
import com.example.communityserver.entity.User;
import com.example.communityserver.service.NoticeReadService;
import com.example.communityserver.service.NoticeService;
import com.example.communityserver.service.RedisService;
import com.example.communityserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private static final String NOTICE_LIST_CACHE_PREFIX = "notice:list:";
    private static final long NOTICE_CACHE_EXPIRE_MINUTES = 30;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeReadService noticeReadService;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private RedisService redisService;

    /**
     * 分页查询公告列表（居民端：按置顶、时间排序）
     */
    @GetMapping("/list")
    @SuppressWarnings("unchecked")
    public Result<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        String cacheKey = NOTICE_LIST_CACHE_PREFIX + page + ":" + size + ":" + (keyword == null ? "" : keyword);

        if (redisService != null) {
            Object cached = redisService.get(cacheKey);
            if (cached != null && cached instanceof Page) {
                System.out.println("[NoticeCache] ✅ 命中缓存, key=" + cacheKey);
                return Result.success((Page<Map<String, Object>>) cached);
            }
            System.out.println("[NoticeCache] ❌ 未命中缓存, key=" + cacheKey);
        }

        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Notice::getTitle, keyword);
        }
        wrapper.orderByDesc(Notice::getIsTop)
               .orderByDesc(Notice::getCreateTime);
        Page<Notice> result = noticeService.page(pageParam, wrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page, size, result.getTotal());
        List<Map<String, Object>> records = result.getRecords().stream().map(notice -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notice.getId());
            map.put("title", notice.getTitle());
            map.put("content", notice.getContent());
            map.put("images", notice.getImages());
            map.put("publisherId", notice.getPublisherId());
            map.put("isTop", notice.getIsTop());
            map.put("createTime", notice.getCreateTime());
            if (notice.getPublisherId() != null) {
                User publisher = userService.getById(notice.getPublisherId());
                if (publisher != null) {
                    map.put("publisherName", publisher.getRealName());
                }
            }
            return map;
        }).toList();
        resultPage.setRecords(records);

        if (redisService != null) {
            redisService.set(cacheKey, resultPage, NOTICE_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return Result.success(resultPage);
    }

    /**
     * 公告详情（自动标记已读）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id,
                                  @RequestAttribute(value = "userId", required = false) Long userId) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error(404, "公告不存在");
        }
        // 居民端访问时自动标记已读
        if (userId != null) {
            noticeReadService.markAsRead(id, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", notice.getId());
        map.put("title", notice.getTitle());
        map.put("content", notice.getContent());
        map.put("images", notice.getImages());
        map.put("publisherId", notice.getPublisherId());
        map.put("isTop", notice.getIsTop());
        map.put("createTime", notice.getCreateTime());
        // 添加发布人名字
        if (notice.getPublisherId() != null) {
            User publisher = userService.getById(notice.getPublisherId());
            if (publisher != null) {
                map.put("publisherName", publisher.getRealName());
            }
        }
        return Result.success(map);
    }

    /**
     * 查询公告已读人数
     */
    @GetMapping("/{id}/read-count")
    public Result<Map<String, Object>> readCount(@PathVariable Long id) {
        long count = noticeReadService.countReadByNoticeId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("noticeId", id);
        data.put("readCount", count);
        return Result.success(data);
    }

    /**
     * 查询公告已读/未读明细（管理员端）
     */
    @GetMapping("/{id}/read-list")
    public Result<List<Map<String, Object>>> readList(@PathVariable Long id) {
        LambdaQueryWrapper<NoticeRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeRead::getNoticeId, id)
               .orderByDesc(NoticeRead::getReadTime);
        List<NoticeRead> reads = noticeReadService.list(wrapper);

        List<Map<String, Object>> result = reads.stream().map(read -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", read.getId());
            map.put("noticeId", read.getNoticeId());
            map.put("userId", read.getUserId());
            map.put("isRead", read.getIsRead());
            map.put("readTime", read.getReadTime());
            // 添加用户名
            if (read.getUserId() != null) {
                User user = userService.getById(read.getUserId());
                if (user != null) {
                    map.put("userName", user.getRealName());
                }
            }
            return map;
        }).toList();
        return Result.success(result);
    }

    /**
     * 管理员新增公告
     */
    @PostMapping
    public Result<Notice> add(@RequestBody Notice notice,
                               @RequestAttribute("userId") Long userId) {
        notice.setPublisherId(userId);
        noticeService.save(notice);
        clearNoticeCache();
        return Result.success(notice);
    }

    /**
     * 管理员编辑公告
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, 
                           @RequestBody Notice notice,
                           @RequestAttribute("role") String role) {
        if (!"manager".equals(role) && !"admin".equals(role)) {
            return Result.error(403, "权限不足");
        }
        Notice existing = noticeService.getById(id);
        if (existing == null) {
            return Result.error(404, "公告不存在");
        }
        notice.setId(id);
        notice.setPublisherId(existing.getPublisherId());
        noticeService.updateById(notice);
        clearNoticeCache();
        return Result.success(notice);
    }

    /**
     * 管理员置顶/取消置顶
     */
    @PutMapping("/{id}/top")
    public Result<?> toggleTop(@PathVariable Long id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error(404, "公告不存在");
        }
        notice.setIsTop(notice.getIsTop() == 1 ? 0 : 1);
        noticeService.updateById(notice);
        clearNoticeCache();
        return Result.success(notice);
    }

    /**
     * 管理员删除公告（软删除）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, @RequestAttribute("role") String role) {
        if (!"manager".equals(role) && !"admin".equals(role)) {
            return Result.error(403, "权限不足");
        }
        noticeService.removeById(id);
        clearNoticeCache();
        return Result.success(null);
    }

    private void clearNoticeCache() {
        if (redisService != null) {
            redisService.deleteByPattern(NOTICE_LIST_CACHE_PREFIX + "*");
        }
    }
}
