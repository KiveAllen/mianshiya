# Monkey-Question 猿题刷题平台

## 项目介绍

Monkey-Question 是一个 基于 Next,js 服务端渲染 + Spring Boot + Redis + MySQL+ Elasticsearch 的刷题平台。

## 技术栈

### 后端

- **核心框架**: Spring Boot + MyBatis-Plus
- **数据库**: MySQL
- **缓存体系**: 
  - Redis（分布式缓存）
  - Caffeine（本地缓存）
  - Redisson（分布式锁 + BitMap + BloomFilter）
- **搜索引擎**: Elasticsearch （题目检索）
- **热点数据识别**: HotKey热点检索
- **流量治理**: 
  - Sentinel（限流、降级、系统保护）
  - Nacos（统一配置中心与服务发现）
- **监控**: Druid 数据库监控
- **权限控制**: Sa-Token权限控制

### 前端

- **前端框架**: Next.js + React 18
- **前端库**: Ant Design组件库
- **前端工程化**: ESLint + Prettier + TypeScript
- **代码生成器**: OpenAPI前端代码生成

## 核心特性

### 🔥 高性能设计
- **多级缓存策略**：Caffeine（本地缓存） + Redis（分布式缓存）
- **热点数据优化**：通过 HotKey 识别高频访问数据，提升响应速度
- **复杂检索支持**：Elasticsearch 实现题目全文检索与高级过滤功能
- **系统稳定性保障**：基于 Sentinel 的限流、降级与熔断机制

### 🔄 高可用架构
- **微服务基础**：基于 Spring Cloud Alibaba 构建分布式系统
- **服务治理**：使用 Nacos 实现服务发现与统一配置管理
- **并发安全控制**：Redisson 提供分布式锁支持，确保数据一致性
- **事务管理**：结合本地事务与最终一致性补偿机制

### 🛡️ 企业级特性
- **权限体系**：Sa-Token 实现 RBAC 模型的权限控制
- **告警机制**：集成 Prometheus + AlertManager 实现完整监控告警体系
- **系统容错能力**：支持熔断、重试、降级等多种高可用策略
- **数据可靠性**：提供数据持久化及定时对账机制，保证业务连续性