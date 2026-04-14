# 自动化测试工作区

`automated-testing` 是整个仓库统一的自动化测试工作区，但会按“项目维度”拆分，而不是把所有脚本混在一起。

当前仓库里有两个独立项目需要逐步建立自动化测试：

- 房源管理系统
- AI Agent / AI 知识库项目

因此这里推荐的目录结构是：

```text
automated-testing
├─ house-management
│  ├─ api
│  ├─ ui
│  ├─ reports
│  └─ README.md
├─ ai-agent
│  ├─ api
│  ├─ ui
│  ├─ reports
│  └─ README.md
└─ shared
   └─ lib
```

## 当前状态

目前已经落地完成的是房源管理系统的 API 自动化测试流程，位置在：

- `automated-testing/house-management/api/Run-House-Automation.ps1`

共享辅助方法放在：

- `automated-testing/shared/lib/TestHelpers.ps1`

房源管理系统测试报告输出到：

- `automated-testing/house-management/reports`

AI Agent 项目的自动化测试目录已经预留，后续可以在不影响房源管理系统的前提下继续扩展。

## 当前推荐执行方式

房源管理系统自动化测试：

```powershell
cd E:\project\study\all-project
powershell -ExecutionPolicy Bypass -File .\automated-testing\house-management\api\Run-House-Automation.ps1
```
