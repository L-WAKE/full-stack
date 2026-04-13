# house-service 后端快速上手指南

## 文档目的

这份文档是给有前端经验、但希望快速上手当前后端项目的同学准备的。

目标不是一次性讲完所有 Java 和 Spring 知识，而是帮助你尽快看懂并能开始改这个项目：

- 知道后端项目目录怎么分层
- 知道每一层分别负责什么
- 知道一个请求是怎么从前端走到数据库的
- 知道以后新增一个模块时要改哪些文件
- 知道哪些文件是必须理解的，哪些可以先放一放

当前项目路径：

- [backend/house-service](E:\project\study\all-project\backend\house-service)

---

## 1. 先建立一个整体认知

如果你是前端开发者，可以先把这个 Spring Boot 后端这样理解：

- `Controller`：接口入口层，接 HTTP 请求
- `Service`：业务逻辑层，处理业务规则
- `Mapper`：数据库访问层，负责查表和写表
- `Entity`：数据库表对应的 Java 对象
- `record / DTO`：接口入参和出参模型
- `Config`：全局配置
- `common`：通用工具和基础设施

你可以把它类比成前端项目里的这些角色：

- `Controller` 类似前端的路由页面入口
- `Service` 类似前端的 `hooks / composables / store` 里的业务逻辑
- `Mapper` 类似你单独封装的 API 请求层，只不过这里请求目标是数据库
- `Entity` 类似你的数据模型定义
- `DTO` 类似你给接口单独定义的 TypeScript 类型

核心思想和前端很像，都是分层，只是后端多了数据库和服务启动框架。

---

## 2. 项目目录结构

后端项目核心目录如下：

```text
backend/house-service
├─ pom.xml
├─ mvnw.cmd
├─ src
│  └─ main
│     ├─ java
│     │  └─ com/example/house
│     │     ├─ HouseServiceApplication.java
│     │     ├─ auth
│     │     ├─ common
│     │     ├─ config
│     │     └─ modules
│     └─ resources
│        ├─ application.yml
│        └─ schema.sql
└─ target
```

其中你最需要先理解的是：

- [`pom.xml`](E:\project\study\all-project\backend\house-service\pom.xml)
- [`src/main/resources/application.yml`](E:\project\study\all-project\backend\house-service\src\main\resources\application.yml)
- [`src/main/resources/schema.sql`](E:\project\study\all-project\backend\house-service\src\main\resources\schema.sql)
- [`src/main/java/com/example/house/HouseServiceApplication.java`](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\HouseServiceApplication.java)

---

## 3. 每个顶层文件分别是什么

### 3.1 `pom.xml`

文件：

- [pom.xml](E:\project\study\all-project\backend\house-service\pom.xml)

它相当于 Java 项目的 `package.json`。

主要作用：

- 声明依赖包
- 声明 Java 版本
- 声明如何构建和打包项目

当前项目里重要依赖有：

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-redis`
- `mybatis-plus-spring-boot3-starter`
- `mysql-connector-j`

你可以这样理解：

- `web`：让项目能提供 HTTP 接口
- `security`：做登录鉴权和拦截
- `validation`：做参数校验
- `redis`：做缓存和 token 会话
- `mybatis-plus`：操作数据库
- `mysql driver`：连接 MySQL

### 3.2 `application.yml`

文件：

- [application.yml](E:\project\study\all-project\backend\house-service\src\main\resources\application.yml)

它相当于后端的 `.env + 全局配置中心`。

主要配置项：

- 服务端口
- 数据库连接
- Redis 连接
- MyBatis-Plus 行为
- 项目自定义配置

当前项目最重要的几项：

```yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/house_management...
    username: house_admin
    password: House@123456

  data:
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

其中逻辑删除配置非常重要：

- `deleted = 0`：有效数据
- `deleted = 1`：逻辑删除数据

只要一个表用了逻辑删除，后端正常查询时会自动忽略 `deleted = 1` 的数据。

### 3.3 `schema.sql`

文件：

- [schema.sql](E:\project\study\all-project\backend\house-service\src\main\resources\schema.sql)

它是项目数据库建表脚本。

你要快速理解当前系统的业务模型，看这个文件最直观。

这里定义了项目里最核心的表：

- `house`
- `tenant`
- `landlord`
- `maintenance_order`
- `cleaning_order`
- `sys_user`
- `sys_role`
- `sys_menu`

如果你把前端的页面理解成“视图层”，那数据库表就是后端业务模型的“底层事实来源”。

### 3.4 `HouseServiceApplication.java`

文件：

- [HouseServiceApplication.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\HouseServiceApplication.java)

这是整个 Spring Boot 项目的启动入口。

它很像前端的：

```js
createApp(App).use(router).use(pinia).mount('#app')
```

只是后端这里启动的是：

- Controller
- Service
- Mapper
- Config
- Spring 容器

---

## 4. `src/main/java/com/example/house` 下面各目录的作用

### 4.1 `auth`

用于处理登录、鉴权、token 校验。

关键文件：

- [AuthController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\auth\AuthController.java)
- [AuthService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\auth\AuthService.java)
- [BearerTokenFilter.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\auth\BearerTokenFilter.java)
- [AuthUser.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\auth\AuthUser.java)

作用：

- 登录接口
- 登出接口
- 获取当前用户信息
- 获取当前用户菜单
- 校验 Bearer Token
- 从 Redis 中恢复登录态

你可以把它理解成后端版的：

- 登录 API
- 全局请求守卫
- 用户权限中心

### 4.2 `config`

用于全局配置。

关键文件：

- [SecurityConfig.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\config\SecurityConfig.java)
- [CorsConfig.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\config\CorsConfig.java)

作用：

- Spring Security 总配置
- 跨域配置

你可以把它理解成：

- 安全体系的总开关
- 前后端联调时的跨域许可

### 4.3 `common`

用于通用基础能力，是整个项目的“基础设施层”。

主要包括：

- `api`
- `config`
- `entity`
- `exception`

关键文件：

- [ApiResponse.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\api\ApiResponse.java)
- [PageResponse.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\api\PageResponse.java)
- [BaseEntity.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\entity\BaseEntity.java)
- [BusinessException.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\exception\BusinessException.java)
- [GlobalExceptionHandler.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\exception\GlobalExceptionHandler.java)
- [MybatisPlusConfig.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\config\MybatisPlusConfig.java)
- [DbMetaObjectHandler.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\config\DbMetaObjectHandler.java)
- [DataInitializer.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\config\DataInitializer.java)

### 4.4 `modules`

这是业务模块目录，是你以后最常改的地方。

当前主要模块：

- `modules/housing`
- `modules/customer`
- `modules/workorder`
- `modules/system`
- `modules/dashboard`

你可以把它理解成前端里的“业务页面目录”。

---

## 5. `common` 目录里的关键概念

### 5.1 `ApiResponse`

文件：

- [ApiResponse.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\api\ApiResponse.java)

作用：

统一接口返回格式。

前端收到的数据都是这种结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

这样前后端的数据协议是统一的。

### 5.2 `PageResponse`

文件：

- [PageResponse.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\api\PageResponse.java)

作用：

统一分页返回结构。

```json
{
  "list": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

### 5.3 `BaseEntity`

文件：

- [BaseEntity.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\entity\BaseEntity.java)

作用：

为多个表提供公共字段。

当前公共字段主要有：

- `orgId`
- `createdBy`
- `createdAt`
- `updatedBy`
- `updatedAt`
- `deleted`

这意味着很多业务表都会自动继承这些字段。

### 5.4 `BusinessException`

文件：

- [BusinessException.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\exception\BusinessException.java)

作用：

表示“业务异常”，比如：

- 房源不存在
- 工单不存在
- 登录失败

### 5.5 `GlobalExceptionHandler`

文件：

- [GlobalExceptionHandler.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\exception\GlobalExceptionHandler.java)

作用：

统一处理异常，把异常转换成标准 JSON，避免接口直接炸出一堆栈信息给前端。

### 5.6 `DbMetaObjectHandler`

文件：

- [DbMetaObjectHandler.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\config\DbMetaObjectHandler.java)

作用：

自动填充：

- `created_at`
- `updated_at`

新增和更新时不用每次手动写时间。

### 5.7 `DataInitializer`

文件：

- [DataInitializer.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\config\DataInitializer.java)

作用：

项目启动时初始化演示数据。

这个文件很有价值，因为你可以从中直接看出：

- 默认有哪些菜单
- 默认有哪些角色
- 默认有哪些示例房源、租客、房东

---

## 6. 一个业务模块通常有哪些文件

以 `housing` 模块为例：

- [HouseController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseController.java)
- [HouseService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseService.java)
- [HouseEntity.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\entity\HouseEntity.java)
- [HouseMapper.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\mapper\HouseMapper.java)
- [RentalMode.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\RentalMode.java)
- [HouseStatus.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseStatus.java)

一个典型模块最小需要这些：

1. `Controller`
2. `Service`
3. `Entity`
4. `Mapper`

如果有固定状态值，再加：

5. `enum`

如果入参出参比较复杂，再加：

6. DTO / record

在这个项目里，DTO 主要直接写在 `Service` 类里作为 `record`。

---

## 7. 每一层分别是干什么的

### 7.1 Controller

职责：

- 接收请求
- 接收参数
- 调用 Service
- 返回统一结果

不要在 Controller 里写复杂业务逻辑。

比如房源列表接口：

- `GET /api/houses`

定义在：

- [HouseController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseController.java)

### 7.2 Service

职责：

- 写业务规则
- 控制增删改查逻辑
- 做对象转换
- 组合多个 Mapper

你以后改需求时，最常改的就是 `Service`。

例如：

- 房源编码自动生成
- 工单状态更新
- 房东和房源联动
- 删除时做逻辑删除

都是 Service 里的事情。

### 7.3 Mapper

职责：

- 和数据库直接交互

当前项目使用 MyBatis-Plus，所以大多数 Mapper 都很薄。

例如：

- [HouseMapper.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\mapper\HouseMapper.java)

通常只需要：

```java
public interface HouseMapper extends BaseMapper<HouseEntity> {
}
```

因为 `BaseMapper` 已经内置很多基础方法：

- `selectById`
- `selectPage`
- `insert`
- `updateById`
- `deleteById`

### 7.4 Entity

职责：

- 映射数据库表结构

例如：

- [HouseEntity.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\entity\HouseEntity.java)

如果数据库表里有：

- `house_code`
- `house_name`
- `rent_price`

那么 Entity 里一般对应：

- `houseCode`
- `houseName`
- `rentPrice`

### 7.5 enum

职责：

- 限定固定值范围

例如：

- `RentalMode`
- `HouseStatus`

这和前端里用常量枚举一个道理，但后端会更强调类型安全。

---

## 8. 一个请求是怎么流转的

以“查询整租房源列表”为例：

前端请求：

```http
GET /api/houses?pageNum=1&pageSize=10&rentalMode=WHOLE
```

流转顺序：

1. `HouseController` 接收请求
2. `HouseController` 调用 `HouseService.list(...)`
3. `HouseService` 使用 `HouseMapper.selectPage(...)`
4. `HouseMapper` 通过 MyBatis-Plus 去查 MySQL
5. 返回 `HouseEntity`
6. `HouseService` 把 `HouseEntity` 转成 `HouseRecord`
7. 包装成 `PageResponse`
8. 再包装成 `ApiResponse.success(...)`
9. 返回给前端

你可以把这个流程记成一句话：

`前端 -> Controller -> Service -> Mapper -> 数据库 -> Service -> Controller -> 前端`

---

## 9. 当前项目已经有哪些业务模块

### 9.1 housing

作用：

- 房源管理
- 房源状态切换
- 房源分页查询

关键文件：

- [HouseController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseController.java)
- [HouseService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseService.java)

### 9.2 customer

作用：

- 租客管理
- 房东管理

关键文件：

- [TenantController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\customer\TenantController.java)
- [TenantService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\customer\TenantService.java)
- [LandlordController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\customer\LandlordController.java)
- [LandlordService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\customer\LandlordService.java)

### 9.3 workorder

作用：

- 维修工单
- 保洁工单

关键文件：

- [WorkOrderController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\workorder\WorkOrderController.java)
- [WorkOrderService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\workorder\WorkOrderService.java)

### 9.4 system

作用：

- 员工管理
- 角色管理
- 菜单管理

关键文件：

- [SystemController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\system\SystemController.java)
- [SystemService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\system\SystemService.java)

### 9.5 dashboard

作用：

- 首页看板统计
- 趋势图数据

关键文件：

- [DashboardController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\dashboard\DashboardController.java)

---

## 10. 为什么有 `record`

你会在 `Service` 里看到很多 `record`，例如：

- `HouseRecord`
- `HouseUpsertRequest`
- `TenantRecord`
- `EmployeeRecord`

这些本质上是轻量 DTO。

你可以这样区分：

### Entity

面向数据库。

职责：

- 与表结构保持一致

### record / DTO

面向接口。

职责：

- 定义前端入参
- 定义前端出参

这是很好的实践，因为：

- 数据库字段不一定适合直接暴露给前端
- 前端要的结构和数据库结构不一定一致
- 可以避免表结构变化时直接影响接口

---

## 11. 当前项目里必须优先看懂的文件

如果你想最快速上手，建议按这个顺序看：

1. [application.yml](E:\project\study\all-project\backend\house-service\src\main\resources\application.yml)
2. [HouseServiceApplication.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\HouseServiceApplication.java)
3. [ApiResponse.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\api\ApiResponse.java)
4. [BaseEntity.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\common\entity\BaseEntity.java)
5. [HouseController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseController.java)
6. [HouseService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\HouseService.java)
7. [HouseEntity.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\entity\HouseEntity.java)
8. [HouseMapper.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\modules\housing\mapper\HouseMapper.java)
9. [AuthController.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\auth\AuthController.java)
10. [AuthService.java](E:\project\study\all-project\backend\house-service\src\main\java\com\example\house\auth\AuthService.java)

只要把这 10 个文件看懂，这个后端你就能开始改功能了。

---

## 12. 如果你要新增一个模块，通常怎么做

假设你要新增“合同管理”模块 `contract`。

一般步骤如下：

### 第一步：建表

先在 MySQL 里有 `contract` 表。

### 第二步：建 Entity

例如：

- `ContractEntity.java`

### 第三步：建 Mapper

例如：

- `ContractMapper.java`

### 第四步：建 Service

编写：

- 分页查询
- 详情查询
- 新增
- 修改
- 删除

### 第五步：建 Controller

暴露接口：

- `GET /api/contracts`
- `POST /api/contracts`
- `PUT /api/contracts/{id}`
- `DELETE /api/contracts/{id}`

### 第六步：前端对接

前端新增：

- `src/api/contracts.js`
- 对应页面和弹窗

---

## 13. 你最应该先掌握哪些后端知识

如果目标是快速上手开发当前后端，我建议按以下顺序学：

### 第一阶段：先看懂项目

1. Spring Boot 项目结构
2. Controller / Service / Mapper / Entity 分层
3. 请求是怎么流转的
4. `application.yml` 看配置
5. MyBatis-Plus 基本 CRUD

### 第二阶段：能开始改需求

6. DTO / record
7. 参数校验
8. 异常处理
9. 分页
10. 逻辑删除

### 第三阶段：能独立开发模块

11. 登录认证
12. Redis token/session
13. 权限控制
14. SQL 调试
15. 数据库设计

---

## 14. 作为前端开发者，你最容易卡住的点

### 14.1 为什么数据库里有数据，页面没显示

常见原因：

- 逻辑删除字段 `deleted = 1`
- 有状态过滤
- 有租赁模式过滤
- 改的是别的数据库实例
- 修改没提交

### 14.2 为什么实体类和接口返回结构不一样

因为：

- Entity 面向数据库
- DTO / record 面向前端

这是一种刻意分层，不是多此一举。

### 14.3 为什么 Mapper 几乎没代码也能查数据库

因为项目用了 MyBatis-Plus，基础 CRUD 已经由 `BaseMapper` 提供。

### 14.4 为什么删了数据但数据库里还在

因为当前项目使用了逻辑删除。

删除时通常不是物理删除，而是：

```sql
deleted = 1
```

---

## 15. 对你最有帮助的学习方式

不要一上来就死磕 Java 语法或 Spring 原理。

最有效的方式是：

1. 先选一个模块，比如 `housing`
2. 顺着看 `Controller -> Service -> Mapper -> Entity`
3. 自己尝试加一个字段
4. 前后端联调一次
5. 再慢慢补框架原理

这是对有工程经验的人最快的路径。

---

## 16. 建议你下一步怎么学这个项目

建议按下面顺序上手：

1. 先读 `application.yml`
2. 再读 `HouseController` 和 `HouseService`
3. 看 `AuthService` 理解登录流程
4. 看 `SystemService` 理解角色和菜单
5. 亲手新增一个字段，比如房源的“朝向”
6. 再亲手新增一个最小模块，比如“公告管理”

---

## 17. 一句话总结

对于你来说，最快上手这个后端的关键不是先学全套后端理论，而是先掌握这件事：

`一个请求如何在 Controller、Service、Mapper、Entity 之间流转，并最终落到 MySQL。`

只要这件事你彻底懂了，这个项目你就能很快开始开发。

---

## 18. 后续你可以继续问我的方向

后面如果你想继续深入，可以直接问我这些问题：

- Controller、Service、Mapper 到底该怎么分职责
- MyBatis-Plus 常用查询怎么写
- Spring Security 登录流程怎么理解
- Redis token 会话怎么设计
- 这个项目怎么新增一个模块
- 当前这个后端怎么调试接口
- 如何从前端页面反推后端代码入口

也可以直接让我带你做实战，例如：

1. 手把手新增一个“公告管理”模块
2. 给房源模块新增字段并联调前端
3. 画出当前项目的请求流转图

