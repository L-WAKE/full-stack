# 房源管理系统自动化测试

这个目录用于存放房源管理系统的自动化测试脚本、测试报告和后续扩展内容。

## 当前已完成内容

- API 自动化测试脚本：`api/Run-House-Automation.ps1`
- 测试报告目录：`reports/`
- 公共辅助脚本：`../shared/lib/TestHelpers.ps1`

## 当前覆盖范围

- 登录与匿名访问拦截
- 当前用户信息与菜单加载
- 看板概览与趋势接口
- 业主新增、查询、更新、删除
- 租客新增、查询、更新、删除
- 房源新增、查询、详情、更新、状态更新、删除
- 房源非法参数校验
- 维修工单新增、查询、更新、删除
- 保洁工单新增、查询、更新、删除
- 自动清理测试数据与退出登录

## 执行命令

```powershell
cd E:\project\study\all-project
powershell -ExecutionPolicy Bypass -File .\automated-testing\house-management\api\Run-House-Automation.ps1
```

## 执行结果

每次运行后，脚本会在 `reports/` 下生成两份报告：

- `*.json`：适合程序读取或接入 CI
- `*.md`：适合人工查看

## 后续建议

- 在 `ui/` 下增加管理端页面自动化测试
- 把当前脚本进一步拆成登录测试、房源测试、工单测试
- 接入持续集成，在提交代码后自动执行
